package study.wyy.datatransfer.spring.spi;

import study.wyy.datatransfer.api.model.DataTransferTask;
import study.wyy.datatransfer.spring.model.DataTransferContext;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author wyaoyao
 * @date 2021/2/23 16:21
 */
public interface FileManager {

    /**
     * 生成文件全路径
     * @param transferTask 任务
     * @param customerFileName 自定义的文件名
     * @param customerPath 使用者指定的文件路径
     * @return 文件全路径
     * customerFileName: 可以通过种方式自定义，执行任务的时候指定或者实现DataExporter的generateFileName方法
     * @see study.wyy.datatransfer.api.model.DataTransferTask#exportFileName
     * @see study.wyy.datatransfer.api.model.DataTransferTask#exportPath
     * @see study.wyy.datatransfer.spring.task.DataExporter#generateFileName(DataTransferContext)
     * @see study.wyy.datatransfer.spring.task.DataExporter#exportPath(DataTransferContext) (DataTransferContext)
     *
     */
    String generateFilePath(DataTransferTask transferTask, String customerFileName,String customerPath);

    String getFileUrl(String filePath);

    void saveFile(String filePath, File file) throws IOException;

    boolean deleteFile(String filePath) throws IOException;

    InputStream openStream(String filePath) throws IOException;
}
