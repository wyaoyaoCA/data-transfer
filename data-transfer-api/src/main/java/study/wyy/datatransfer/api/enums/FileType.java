package study.wyy.datatransfer.api.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author wyaoyao
 * @description
 * @date 2021/2/23 10:25
 * 文件类型
 */
public enum  FileType {

    /**
     * xlsx 文件
     */
    XLSX("xlsx");

    /**
     * 文件扩展名
     */
    @Getter
    private String fileExt;


    FileType(String fileExt) {
        this.fileExt = fileExt;
    }

    private static Map<String,FileType> extMap = new HashMap<>();
    static {
        FileType[] values = FileType.values();
        for (FileType fileType : values) {
            extMap.put(fileType.getFileExt(),fileType);
        }
    }
    public static  FileType fromExt(String fileExt){
        if(StringUtils.isBlank(fileExt)){
            throw new IllegalArgumentException("file.ext.is.null");
        }
        FileType fileType = extMap.get(fileExt);
        if(Objects.isNull(fileExt)){
            throw new IllegalArgumentException("nonsupport.file.ext");
        }
        return fileType;
    }

}
