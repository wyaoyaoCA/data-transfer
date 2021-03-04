package study.wyy.datatransfer.spring.component;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import study.wyy.datatransfer.api.exception.DataTransferException;
import study.wyy.datatransfer.api.model.DataTransferTask;
import study.wyy.datatransfer.api.request.DataTransferCreateRequest;
import study.wyy.datatransfer.api.service.TaskStorageService;
import study.wyy.datatransfer.spring.spi.FileManager;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.UUID;

/**
 * @author wyaoyao
 * @date 2021/2/25 10:51
 */
@RestController
@RequestMapping("data/transfer")
@Slf4j
public class DataTransferRestController {

    private final DataTaskTrigger dataTaskTrigger;
    private final TaskStorageService taskStorageService;
    private final File temp;
    private final FileManager fileManager;

    @Autowired
    public DataTransferRestController(DataTaskTrigger dataTaskTrigger, TaskStorageService taskStorageService, FileManager fileManager) throws IOException {
        this.dataTaskTrigger = dataTaskTrigger;
        this.taskStorageService = taskStorageService;
        this.fileManager = fileManager;
        // 上传文件的临时保存目录
        temp = Files.createTempDirectory("data_transfer_import_temp").toFile();
    }

    @PostMapping("exportTask")
    @ApiOperation("创建导出任务")
    public DataTransferTask exportTask(@RequestBody DataTransferCreateRequest request) {
        request.checkParam();
        return dataTaskTrigger.doExport(request);
    }


    @PostMapping(value = "importTask")
    @ApiOperation("创建导入任务")
    public DataTransferTask importTask(DataTransferCreateRequest request, @RequestPart(required = false) MultipartFile importFile) throws IOException {
        request.checkParam();
        if (Objects.isNull(importFile) && StringUtils.isBlank(request.getImportFilePath())) {
            log.error("import file must define by importFile or importFilePath in request");
            throw new IllegalArgumentException("import.file.is.null");
        }
        if(Objects.nonNull(importFile)){
            // 保存上传的文件
            File tempFile = createTempFile(importFile.getName());
            importFile.transferTo(tempFile);
            request.setImportFilePath(tempFile.getPath());
        }
        return dataTaskTrigger.doImport(request);
    }

    @GetMapping("result/{id}")
    @ApiOperation("查询任务")
    public DataTransferTask result(@PathVariable Long id) {
        DataTransferTask transferTask = taskStorageService.findById(id);
        return transferTask;
    }

    /**
     * 创建临时文件
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    private File createTempFile(String fileName) throws IOException {
        String tempFileName = UUID.randomUUID().toString() + "__" + fileName;
        String absFilePath = Paths.get(temp.getPath(), tempFileName).toString();
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

    @GetMapping(value = "/downloadError/{id}")
    @ApiOperation("错误文件下载")
    public void downloadError(@PathVariable Long id, HttpServletResponse response) throws Exception {
        DataTransferTask task = taskStorageService.findById(id);
        if (task == null || StringUtils.isBlank(task.getErrorRecordsFilePath())) {
            throw new DataTransferException("file.not.exist");
        }
        try {
            //获取spring提供的文件系统资源对象
            Resource resource = new FileSystemResource(task.getErrorRecordsFilePath());
            //使用spring的工具类把resource中的文件转换成一个字节数组
            byte[] images = FileCopyUtils.copyToByteArray(resource.getFile());
            //使用response设置响应消息头
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("utf-8");
            // 获取文件名字
            String fileName = Paths.get(task.getErrorRecordsFilePath()).getFileName().toString();
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            //输出字节数组
            response.getOutputStream().write(images, 0, images.length);
            // 删除文件和删除redis中的结果
            fileManager.deleteFile(task.getErrorRecordsFilePath());
            taskStorageService.deleteById(id);
        } catch (Exception e) {
            log.error("download file error cause by {}", e.getMessage(), e);
            throw new DataTransferException("file.download.fail");
        }
    }
}
