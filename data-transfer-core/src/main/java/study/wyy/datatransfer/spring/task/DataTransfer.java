package study.wyy.datatransfer.spring.task;

import study.wyy.datatransfer.spring.model.DataTransferContext;

/**
 * @author wyaoyao
 * @date 2021/2/23 14:28
 */
public interface DataTransfer<T,C extends DataTransferContext> {

    /**
     * 指定模型的类型
     * @return
     */
    Class<T> modelClass();

    /**
     * 指定上下文的类型
     * @return
     */
    Class<C> contextType();
}
