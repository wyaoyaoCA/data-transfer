package study.wyy.datatransfer.simple.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author wyaoyao
 * @date 2021/2/25 17:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student extends BaseRowModel implements Serializable {

    @ExcelProperty(index = 0)
    private Long id;

    @ExcelProperty(index = 1)
    @JsonProperty("姓名")
    private String name;

    @JsonProperty("年龄")
    @ExcelProperty(index = 2)
    private Integer age;
}
