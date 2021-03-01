package study.wyy.datatransfer.spring.task;

import study.wyy.datatransfer.api.exception.DataTransferException;
import study.wyy.datatransfer.api.model.TaskResult;
import study.wyy.datatransfer.spring.model.DataTransferContext;
import study.wyy.datatransfer.spring.utils.ClassUtils;

import java.util.Map;

/**
 * @author wyaoyao
 * @date 2021/2/23 14:35
 */
public interface DataExporter<T, C extends DataTransferContext> extends DataTransfer {

    default void exportStarted(C context) throws DataTransferException {
    }

    default void exportFinished(TaskResult executeResult, C context) throws DataTransferException{
    }

    @Override
    default Class<T> modelClass(){
        return ClassUtils.getGenericClassOnInterface(this.getClass(), XlsxDataExporter.class, 0, Map.class);
    }

    @Override
    default Class<C> contextType() {
        return ClassUtils.getGenericClassOnInterface(this.getClass(), XlsxDataExporter.class, 1, Map.class);
    }

    /**
     * 生成导出文件的名字
     * @param context
     * @return
     */
    default String generateFileName(DataTransferContext context){
        return null;
    }

    /**
     * 指定导出文件的文件路径
     * eg: /usr/local/myExportFile
      * @param context
     * @return
     */
    default String exportPath(DataTransferContext context){
        return null;
    }
}
