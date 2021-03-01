package study.wyy.datatransfer.spring.formater;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author wyaoyao
 * @date 2021/2/23 17:39
 */
public interface XlsxDataWriter<T> {

    void writeLine(T data, OutputStream outputStream);

    void flush(OutputStream outputStream) throws IOException;
}
