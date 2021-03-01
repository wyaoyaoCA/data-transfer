package study.wyy.datatransfer.spring.task;

import com.alibaba.excel.metadata.Sheet;
import study.wyy.datatransfer.api.exception.InvalidRowException;
import study.wyy.datatransfer.api.model.TaskResult;
import study.wyy.datatransfer.spring.model.BatchDataImportResult;
import study.wyy.datatransfer.spring.model.DataTransferContext;
import study.wyy.datatransfer.spring.model.RowData;
import study.wyy.datatransfer.spring.utils.ClassUtils;

import java.util.List;

/**
 * @author wyaoyao
 * @date 2021/2/23 16:16
 */
public interface XlsxDataImporter<T,C extends DataTransferContext> extends DataImporter<T,C>{

    /**
     * 导入书逻辑
     * @param data
     * @param context
     * @throws InvalidRowException
     */
    void importRowData(RowData<T> data, C context) throws InvalidRowException;

    /**
     * 指定批量导入的数据
     * 这里返回null, 则使用单行导入，只会调用importRowData(莫热门)
     * 如果返回大于0， 则只会调用batchImportRowData
     *建议用单行导入。使用batchImport，需要精确控制每行的错误逻辑
     * @return
     */
    default Integer batchSize() {
        return null;
    }

    /**
     * 指定导入的是excle的哪个sheet，标题行有几个
     * @return
     */
    default Sheet sheetProperty() {
        return new Sheet(/*第一个sheet*/1, /*标题1行*/1);
    }

    /**
     * 批量导入
     * @param dataList
     * @param context
     * @return
     */
    default BatchDataImportResult<T> batchImportRowData(List<RowData<T>> dataList, C context) {
        return BatchDataImportResult.successResultOf(dataList);
    }

    /**
     * 导入任务的前置逻辑
     * @param context
     */
    default void importStarted(C context) {
    }

    /**
     * 指定错误文件的名字
     * @param context
     * @return
     */
    default String generateFailLogFileName(C context) {
        return null;
    }

    /**
     * 指定错误文件的地址
     * @param context
     * @return
     */
    default String generateFailLogFilePath(C context) {
        return null;
    }

    /**
     * 导入任务的后置处理
     * @param executeResult
     * @param context
     */
    default void importFinished(TaskResult executeResult, C context) {
    }


    @Override
    default Class<C> contextType() {
        return ClassUtils.getGenericClassOnInterface(this.getClass(), XlsxDataImporter.class, 1, DataTransferContext.class);
    }

    @Override
    default Class<T> modelClass() {
        return ClassUtils.getGenericClassOnInterface(this.getClass(), XlsxDataImporter.class, 0, List.class);
    }

}
