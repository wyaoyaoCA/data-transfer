package study.wyy.datatransfer.spring.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * @author wyaoyao
 * @description
 * @date 2021/1/22 9:10
 */
@Data
@ConfigurationProperties(prefix = "data.transfer")
public class DataTaskProperties {

    @NestedConfigurationProperty
    private FileProperties file = new FileProperties();

    private String taskStorage = "local";
}
