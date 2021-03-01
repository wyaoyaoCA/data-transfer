package study.wyy.datatransfer.spring.task;

import study.wyy.datatransfer.spring.model.DataTransferContext;
import study.wyy.datatransfer.spring.utils.ClassUtils;

import java.util.Map;

/**
 * @author wyaoyao
 * @date 2021/2/23 16:10
 */
public interface DataImporter<T,C extends DataTransferContext> extends DataTransfer<T,C>{

    @Override
    default Class<T> modelClass(){
        return ClassUtils.getGenericClassOnInterface(this.getClass(), XlsxDataExporter.class, 0, Map.class);
    }

    @Override
    default Class<C> contextType() {
        return ClassUtils.getGenericClassOnInterface(this.getClass(), XlsxDataExporter.class, 1, Map.class);
    }
}
