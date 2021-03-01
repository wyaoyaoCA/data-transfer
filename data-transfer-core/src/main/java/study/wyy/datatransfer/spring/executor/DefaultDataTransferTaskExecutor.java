package study.wyy.datatransfer.spring.executor;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.metadata.ExcelColumnProperty;
import com.alibaba.excel.metadata.ExcelHeadProperty;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.util.TypeUtil;
import com.alibaba.excel.util.WorkBookUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import net.sf.cglib.beans.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import study.wyy.datatransfer.api.exception.DataTransferException;
import study.wyy.datatransfer.api.exception.InvalidRowException;
import study.wyy.datatransfer.api.enums.DataTransferTaskType;
import study.wyy.datatransfer.api.enums.TaskStatus;
import study.wyy.datatransfer.api.model.DataTransferTask;
import study.wyy.datatransfer.api.model.PageInfo;
import study.wyy.datatransfer.api.model.PageResult;
import study.wyy.datatransfer.api.model.TaskResult;
import study.wyy.datatransfer.api.service.TaskStorageService;
import study.wyy.datatransfer.spring.annotations.Exporter;
import study.wyy.datatransfer.spring.annotations.Importer;
import study.wyy.datatransfer.spring.formater.XlsxDataWriter;
import study.wyy.datatransfer.spring.formater.XlsxDataWriterFactory;
import study.wyy.datatransfer.spring.model.BatchDataImportResult;
import study.wyy.datatransfer.spring.model.DataTransferContext;
import study.wyy.datatransfer.spring.model.RowData;
import study.wyy.datatransfer.spring.spi.FileManager;
import study.wyy.datatransfer.spring.task.DataExporter;
import study.wyy.datatransfer.spring.task.DataImporter;
import study.wyy.datatransfer.spring.task.XlsxDataExporter;
import study.wyy.datatransfer.spring.task.XlsxDataImporter;
import study.wyy.datatransfer.spring.utils.ClassUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.lang.Integer.max;

/**
 * @author wyaoyao
 * @date 2021/2/23 16:19
 */
@Slf4j
public class DefaultDataTransferTaskExecutor implements DataTransferTaskExecutor {

    private final Map<String, DataImporter> importers = new ConcurrentHashMap<>();
    private final Map<String, DataExporter> exporters = new ConcurrentHashMap<>();
    private final ExecutorService executorService;
    private final FileManager fileManager;
    private final XlsxDataWriterFactory xlsxDataWriterFactory;
    private final TaskStorageService taskStorageService;
    private final File tempDir;

    public DefaultDataTransferTaskExecutor(FileManager fileManager, XlsxDataWriterFactory xlsxDataWriterFactory, TaskStorageService taskStorageService) throws IOException {
        this.fileManager = fileManager;
        this.xlsxDataWriterFactory = xlsxDataWriterFactory;
        this.taskStorageService = taskStorageService;
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("DataTaskerExecutorThread-%d")
                .setDaemon(false).build();
        this.executorService = Executors.newFixedThreadPool(5, threadFactory);
        this.tempDir = Files.createTempDirectory("data_transfer_temp").toFile();
    }

    @Override
    public Boolean executeAsync(DataTransferTask task) {
        try {
            checkTask(task);
            runTaskAsync(task);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("execute task error task name is {}, taskType: {}, fileType: {}", task.getName(), task.getType(), task.getFileExt(), e);
            return Boolean.FALSE;
        }
    }

    private Future<TaskResult> runTaskAsync(DataTransferTask task) {
        return executorService.submit(() -> runTask(task));
    }

    private TaskResult runTask(DataTransferTask task) {
        TaskResult result = null;
        // 获取任务类型
        DataTransferTaskType taskType = DataTransferTaskType.from(task.getType());
        try {
            switch (taskType) {
                case EXPORT:
                    result = doExport(task);
                    break;
                case IMPORT:
                    result = doImport(task);
                    break;
                default:
                    log.error("current taskType [{}] is not support", taskType);
            }
            log.info("execute task success task name is {}, taskType: {}, fileType: {}", task.getName(), task.getType(), task.getFileExt());
        } catch (Exception e) {
            log.error("execute task error task name is {}, taskType: {}, fileType: {}", task.getName(), task.getType(), task.getFileExt(), e);
            result = new TaskResult();
            result.setTransferTask(task);
            result.setFinishTime(new Date());
            result.setStatus(TaskStatus.ERROR.getCode());
            result.setError(e.getClass().getName() + ":" + e.getMessage());
        }
        taskStorageService.update(result);
        return result;
    }

    private TaskResult doImport(DataTransferTask task) throws Exception {
        TaskResult result = importXlsx(task);
        // 删除导入时的临时文件
        if (StringUtils.isNotBlank(task.getImportFilePath()) && task.getImportFilePath().contains("data_transfer_import_temp")) {
            fileManager.deleteFile(task.getImportFilePath());
        }
        return result;
    }

    private TaskResult importXlsx(DataTransferTask task) throws IllegalAccessException, InstantiationException, IOException {
        XlsxDataImporter importer = (XlsxDataImporter) importers.get(task.getName());
        AtomicLong successCount = new AtomicLong(0);

        Class modelClazz = importer.modelClass();
        if ((!BaseRowModel.class.isAssignableFrom(modelClazz)) && (!List.class.isAssignableFrom(modelClazz))) {
            throw new IllegalArgumentException("model class must be List<String> or BaseRowModel ");
        }
        ExcelHeadProperty excelHeadProperty =
                BaseRowModel.class.isAssignableFrom(modelClazz) ? new ExcelHeadProperty(modelClazz, new ArrayList<>()) : null;

        Class<? extends DataTransferContext> contextClazz = importer.contextType();

        DataTransferContext dataImportContext = contextClazz.newInstance();
        dataImportContext.setDataTransferTask(task);

        List<Pair<RowData, String>> errorRows = new ArrayList<>();
        TaskResult taskResult = new TaskResult();
        taskResult.setTransferTask(task);
        try (InputStream inputStream = new BufferedInputStream(openStream(task.getImportFilePath()))) {
            ExcelReader excelReader = new ExcelReader(inputStream, task.getId(), new AnalysisEventListener<List<String>>() {

                List<List<String>> headLines = new ArrayList<>();
                Integer batchSize = ObjectUtils.defaultIfNull(importer.batchSize(), 0);
                List<RowData> batchRows = new ArrayList<>();

                @Override
                @SuppressWarnings("unchecked")
                public void invoke(List<String> originRowData, AnalysisContext analysisContext) {
                    List<String> rowDataClean = originRowData.stream().map(s -> null == s ? null : s.trim())
                            .collect(Collectors.toList());

                    if ((analysisContext.getCurrentRowNum() < importer.sheetProperty().getHeadLineMun())
                            && (analysisContext.getCurrentRowNum() <= importer.sheetProperty().getHeadLineMun() - 1)) {
                        headLines.add(rowDataClean);
                    } else {
                        RowData rowData = new RowData();
                        rowData.setCurrentRowNumber(analysisContext.getCurrentRowNum());
                        rowData.setOriginCellDatas(rowDataClean);
                        try {
                            rowData.setData(buildUserModel(analysisContext, rowDataClean));
                            doImport(rowData);
                        } catch (InvalidRowException ire) {
                            errorRows.add(Pair.of(rowData, ire.getMessage()));
                        } catch (Exception e) {
                            errorRows.add(Pair.of(rowData, e.getClass().getSimpleName() + ": " + e.getMessage()));
                        }
                    }
                }

                @SuppressWarnings("unchecked")
                private void doImport(RowData rowData) throws InvalidRowException {
                    if (batchSize > 0) {
                        batchRows.add(rowData);
                        if (batchRows.size() >= batchSize) {
                            doBatchImport();
                        }
                    } else {
                        importer.importRowData(rowData, dataImportContext);
                        successCount.incrementAndGet();
                    }
                }

                @SuppressWarnings("unchecked")
                private void doBatchImport() {
                    try {
                        BatchDataImportResult batchResult = importer.batchImportRowData(batchRows, dataImportContext);
                        if (batchResult != null) {
                            if (CollectionUtils.isNotEmpty(batchResult.getErrorRows())) {
                                errorRows.addAll(batchResult.getErrorRows());
                            }
                            successCount.addAndGet(batchResult.getSuccessRows().size());
                        } else {
                            // null result means all success
                            successCount.addAndGet(batchRows.size());
                        }
                    } catch (Exception e) {
                        log.error("[DataTransferTask] doBatchImportError", e);
                        String errorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                        batchRows.forEach(rowData -> errorRows.add(Pair.of(rowData, errorMessage)));
                    }
                    batchRows = new ArrayList<>();
                }

                @Override
                @SuppressWarnings("unchecked")
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                    try {
                        if (CollectionUtils.isNotEmpty(batchRows)) {
                            doBatchImport();
                        }
                        taskResult.setSuccessCount(successCount.get());
                        taskResult.setErrorCount((long) errorRows.size());
                        taskResult.setStatus(TaskStatus.SUCCESS.getCode());

                        if (CollectionUtils.isNotEmpty(errorRows)) {
                            // 错误文件名字
                            String failLogFileName = importer.generateFailLogFileName(dataImportContext);
                            // 错误文件路径
                            String failLogFilePath = importer.generateFailLogFilePath(dataImportContext);
                            String importFailRecordsPath = fileManager.generateFilePath(task, failLogFileName, failLogFilePath);
                            saveFailRecordsToFile(importFailRecordsPath, headLines, errorRows, excelHeadProperty);
                            taskResult.setErrorRecordsFilePath(importFailRecordsPath);
                            taskResult.setErrorRecordsUrl(fileManager.getFileUrl(importFailRecordsPath));
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        taskResult.setStatus(TaskStatus.ERROR.getCode());
                        taskResult.setError(e.getClass().getSimpleName() + ": " + e.getMessage());
                    }
                    try {
                        importer.importFinished(taskResult, dataImportContext);
                    } catch (Exception e) {
                        taskResult.setStatus(TaskStatus.FINISH_ERROR.getCode());
                        taskResult.setError(e.getClass().getSimpleName() + ": " + e.getMessage());
                    }
                }

                private Object buildUserModel(AnalysisContext context, List<String> stringList) throws Exception {
                    if (null != excelHeadProperty) {
                        Object resultModel = modelClazz.newInstance();
                        BeanMap.create(resultModel).putAll(
                                TypeUtil.getFieldValues(stringList, excelHeadProperty, context.use1904WindowDate()));
                        return resultModel;
                    } else {
                        return stringList;
                    }
                }

            }, false);

            Sheet modelLessSheet = new Sheet(importer.sheetProperty().getSheetNo(), 0);
            importer.importStarted(dataImportContext);
            excelReader.read(modelLessSheet);
            return taskResult;
        }
    }

    private void saveFailRecordsToFile(String importFailRecordsPath, List<List<String>> headLines, List<Pair<RowData, String>> errorRows, ExcelHeadProperty excelHeadProperty) throws IOException {
        File tmpFile = createTempFile(importFailRecordsPath);

        try (SXSSFWorkbook xlsBook = new SXSSFWorkbook(10000)) {
            org.apache.poi.ss.usermodel.Sheet xlsBookSheet = xlsBook.createSheet();
            try (OutputStream outputStream = new FileOutputStream(tmpFile)) {
                int rowNum = 0;
                int errColNum = 0;

                if (CollectionUtils.isNotEmpty(headLines)) {
                    errColNum = headLines.stream().mapToInt(List::size).max().orElse(0);
                    for (List<String> headLine : headLines) {
                        Row row = xlsBookSheet.createRow(rowNum++);
                        int colNum = 0;
                        for (; colNum < headLine.size(); ++colNum) {
                            WorkBookUtil.createCell(row, colNum, null, headLine.get(colNum));
                        }
                        WorkBookUtil.createCell(row, colNum, null, "ERROR");
                    }
                }

                for (var errorRow : errorRows) {
                    Row row = xlsBookSheet.createRow(rowNum++);
                    //noinspection unchecked
                    List<String> errorRowData = errorRow.getLeft().getOriginCellDatas();
                    String errMsg = errorRow.getRight();

                    int colNum = 0;
                    for (String cellValue : errorRowData) {
                        Cell cell = WorkBookUtil.createCell(row, colNum, null, cellValue);
                        if (null != excelHeadProperty) {
                            ExcelColumnProperty columnProperty = excelHeadProperty.getExcelColumnProperty(colNum);
                            if (null != columnProperty && Date.class.isAssignableFrom(columnProperty.getField().getType())) {
                                try {
                                    String cellValueFormatted = TypeUtil.formatDate(DateUtil.getJavaDate(
                                            Double.parseDouble(cellValue)), columnProperty.getFormat());
                                    cell.setCellValue(cellValueFormatted);
                                } catch (Exception e) {
                                    // skip
                                }
                            }
                        }
                        ++colNum;
                    }
                    WorkBookUtil.createCell(row, max(colNum, errColNum), null, errMsg);
                }
                xlsBook.write(outputStream);
            }
        }
    }

    private InputStream openStream(String filePath) throws IOException {
        try {
            return fileManager.openStream(filePath);
        } catch (Exception e) {
            throw new IOException(MessageFormat.format("fail to connection url, url:({0})", filePath), e);
        }
    }

    private TaskResult doExport(DataTransferTask task) throws Exception {
        TaskResult taskExecuteResult = exportXlsx(task);
        return taskExecuteResult;
    }

    protected TaskResult exportXlsx(DataTransferTask task) throws IllegalAccessException, InstantiationException, DataTransferException, IOException {
        XlsxDataExporter exporter = (XlsxDataExporter) exporters.get(task.getName());
        // 构造上下文
        DataTransferContext context = (DataTransferContext) exporter.contextType().newInstance();
        context.setDataTransferTask(task);
        PageInfo pageInfo = new PageInfo(1, exporter.batchSize());
        // 前置处理
        exporter.exportStarted(context);
        XlsxDataWriter xlsxDataWriter = xlsxDataWriterFactory.from(exporter.modelClass());
        // 优先级：触发任务指定的文件name > 接口中generateFileName方法返回的的文件名字
        String fileName = StringUtils.isNotBlank(task.getExportFileName()) ? task.getExportFileName() : exporter.generateFileName(context);
        // 优先级: 触发任务时指定的文件路径 > 接口返回的文件路径
        String exportPath = StringUtils.isNotBlank(task.getExportPath()) ? task.getExportPath() : exporter.exportPath(context);
        // 生成文件全路径
        String filePath = fileManager.generateFilePath(task, fileName, exportPath);
        // 创建临时文件
        File tempFile = createTempFile(filePath);
        AtomicLong exportedCount = new AtomicLong(0);
        Long total = 0L;
        try (OutputStream fileOutputStream = new FileOutputStream(tempFile)) {
            while (true) {
                PageResult page = exporter.exportData(pageInfo, context);
                // TODO: 如何获取总数量
                if (page.getTotal() != null) total = page.getTotal();
                List dataList = page.getData();
                if (null == page || CollectionUtils.isEmpty(dataList)) {
                    // there are no data
                    break;
                }
                dataList.forEach(data -> {
                    exportedCount.getAndIncrement();
                    xlsxDataWriter.writeLine(data, fileOutputStream);
                });
                // 下一页
                pageInfo.nextPage();
            }
            xlsxDataWriter.flush(fileOutputStream);
        }
        // 保存文件
        fileManager.saveFile(filePath, tempFile);
        // 删除临时文件
        deleteTemp(tempFile);
        TaskResult result = new TaskResult();
        result.setTransferTask(task);
        result.setExportFilePath(filePath);
        result.setStatus(TaskStatus.SUCCESS.getCode());
        result.setSuccessCount(exportedCount.get());
        result.setErrorCount(total - result.getSuccessCount());
        result.setFileUrl(fileManager.getFileUrl(filePath));
        // 执行导出结束回调
        exporter.exportFinished(result, context);
        result.setFinishTime(new Date());
        return result;

    }

    private void deleteTemp(File tempFile) {
        try {
            Files.delete(tempFile.toPath());
        } catch (Exception e) {
            log.error("temp file [{}]delete error", tempFile.toPath(), e);
        }
    }

    private void checkTask(DataTransferTask task) throws DataTransferException {
        DataTransferTaskType taskType = DataTransferTaskType.from(task.getType());
        if (Objects.isNull(taskType)) {
            log.error("current task type not support; taskName: {}; taskType:{}", task.getName(), task.getType());
            throw new DataTransferException("current task type not support");
        }
    }

    @Override
    public void registerImporter(DataImporter dataImporter) {
        Importer importer = ClassUtils.getAnnotation(dataImporter, Importer.class);
        if (Objects.isNull(importer)) {
            String simpleName = dataImporter.getClass().getSimpleName();
            throw new IllegalArgumentException(simpleName + " class should with Importer Annotation");
        }
        String name = importer.name();
        if (importers.containsKey(name)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Except only one importer for name {1}, but found two.", name
            ));
        }
        importers.put(name, dataImporter);
    }

    @Override
    public void registerExporter(DataExporter dataExporter) {
        Exporter exporter = ClassUtils.getAnnotation(dataExporter, Exporter.class);
        if (Objects.isNull(exporter)) {
            String simpleName = dataExporter.getClass().getSimpleName();
            throw new IllegalArgumentException(simpleName + " class should with Exporter Annotation");
        }
        String name = exporter.name();
        if (exporters.containsKey(name)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Except only one exporter for name {1}, but found two.", name
            ));
        }
        exporters.put(name, dataExporter);
    }

    /**
     * 创建临时文件
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    private File createTempFile(String filePath) throws IOException {
        String tempFileName = UUID.randomUUID().toString() + "__" + Paths.get(filePath).getFileName().toString();
        String absFilePath = Paths.get(tempDir.getPath(), tempFileName).toString();
        File directory = new File(Paths.get(absFilePath).getParent().toString());
        if (!directory.exists()) {
            boolean d = directory.mkdirs();
            if (!d) {
                throw new IOException(MessageFormat.format(
                        "Failed to create folder, {0}", directory));
            }
        }
        return new File(absFilePath);
    }
}
