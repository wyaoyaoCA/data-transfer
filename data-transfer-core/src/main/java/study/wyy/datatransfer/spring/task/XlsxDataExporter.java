package study.wyy.datatransfer.spring.task;

import study.wyy.datatransfer.api.model.PageInfo;
import study.wyy.datatransfer.api.model.PageResult;
import study.wyy.datatransfer.spring.model.DataTransferContext;
import study.wyy.datatransfer.spring.utils.ClassUtils;

import java.util.Map;

/**
 * @author wyaoyao
 * @date 2021/2/23 14:48
 */
public interface XlsxDataExporter<T,C extends DataTransferContext> extends DataExporter{

    /**
     * 导出数据
     * @param pageInfo 分页信息
     * @param context 上下文信息
     * @return
     */
    PageResult<T> exportData(PageInfo pageInfo, C context);

    default Integer batchSize(){
        // 默认每次导出100
        return 100;
    }

    @Override
    default Class<T> modelClass(){
        return ClassUtils.getGenericClassOnInterface(this.getClass(), XlsxDataExporter.class, 0, Map.class);
    }

    @Override
    default Class<C> contextType() {
        return ClassUtils.getGenericClassOnInterface(this.getClass(), XlsxDataExporter.class, 1, Map.class);
    }


}
