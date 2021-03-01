package study.wyy.datatransfer.spring.formater;

import study.wyy.datatransfer.spring.model.Group;

/**
 * @author wyaoyao
 * @date 2021/2/25 13:36
 */
public class DefaultXlsxDataWriterFactory<T> implements XlsxDataWriterFactory<T> {
    @Override
    public XlsxDataWriter<T> from(Class<T> clazz) {
        if(null == clazz){
            throw new IllegalArgumentException("model class type should not null");
        }
        if (Group.class.isAssignableFrom(clazz)){
            return new XlsxDataMergeRowWriter<>();
        }
        return new DefaultXlsxDataWriter<>(clazz);

    }
}
