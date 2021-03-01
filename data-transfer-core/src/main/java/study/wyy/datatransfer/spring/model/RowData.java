package study.wyy.datatransfer.spring.model;

import lombok.Data;

import java.util.List;

/**
 * @author wyaoyao
 * @date 2021/2/25 9:22
 */
@Data
public class RowData<T> {
    Integer currentRowNumber;

    List<String> originCellDatas;

    T data;
}
