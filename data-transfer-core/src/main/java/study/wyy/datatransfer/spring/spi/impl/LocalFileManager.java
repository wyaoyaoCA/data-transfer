package study.wyy.datatransfer.spring.spi.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import study.wyy.datatransfer.api.model.DataTransferTask;
import study.wyy.datatransfer.spring.spi.FileManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author wyaoyao
 * @date 2021/2/25 12:36
 */
public class LocalFileManager implements FileManager {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private final String fileBaseDir;

    public LocalFileManager(String fileBaseDir) {
        if(StringUtils.isNotEmpty(fileBaseDir)){
            this.fileBaseDir = fileBaseDir;
        }else {
            this.fileBaseDir = System.getProperty("user.home") + "/data_transfer";
        }
    }

    @Override
    public String generateFilePath(DataTransferTask task, String exportFileName, String exportPath) {
        String fileName = exportFileName;
        if(StringUtils.isBlank(fileName)){
            fileName = task.getName()+ "-" + dateFormat.format(new Date());
        }
        // add file ext
        fileName += ("." + task.getFileExt());
        String randomDirName = UUID.randomUUID().toString();
        if(StringUtils.isBlank(exportPath)){
            return Paths.get(fileBaseDir,
                    task.getName(),
                    dateFormatter.format(task.getStartTime()),
                    // 随机目录，防止覆盖
                    randomDirName,
                    fileName).toString();
        }
        return exportPath + "\\" + fileName;
    }

    @Override
    public String getFileUrl(String filePath) {
        if(StringUtils.isEmpty(filePath)) {
            return null;
        }
        return "file://" + Paths.get(fileBaseDir).resolve(filePath).toAbsolutePath().toString();
    }


    @Override
    public void saveFile(String filePath, File file) throws IOException {
        FileUtils.copyFile(file, new File(filePath));
    }

    @Override
    public boolean deleteFile(String filePath) throws IOException {
        if(StringUtils.isEmpty(filePath)) {
            return true;
        }
        Files.delete(Paths.get(fileBaseDir).resolve(filePath));
        return true;
    }

    @Override
    public InputStream openStream(String filePath) throws IOException {
        return new FileInputStream(filePath);
    }
}
