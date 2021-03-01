package study.wyy.datatransfer.spring.properties;


import lombok.Data;

/**
 * @author wyaoyao
 * @description
 * @date 2021/1/22 9:12
 */
@Data
public class FileProperties {

    private String activate = "local";

    private String fileBaseDir;
}
