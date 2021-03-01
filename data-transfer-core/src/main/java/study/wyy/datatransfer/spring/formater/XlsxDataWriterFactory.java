package study.wyy.datatransfer.spring.formater;

/**
 * @author wyaoyao
 * @date 2021/2/23 17:40
 */
public interface XlsxDataWriterFactory<T> {

    XlsxDataWriter<T> from(Class<T> clazz);
}
