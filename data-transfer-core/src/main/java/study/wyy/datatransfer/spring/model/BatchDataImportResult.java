package study.wyy.datatransfer.spring.model;

import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wyaoyao
 * @date 2021/2/25 9:27
 */
@Data
public class BatchDataImportResult<T> {

    private List<RowData<T>> successRows = new ArrayList<>();
    private List<Pair<RowData<T>, String>> errorRows = new ArrayList<>();

    public static <T> BatchDataImportResult<T> successResultOf(List<RowData<T>> successRows) {
        BatchDataImportResult<T> ret = new BatchDataImportResult<>();
        ret.setSuccessRows(successRows);
        return ret;
    }

    public void addErrorRow(RowData<T> rowData, String errorMessage) {
        errorRows.add(Pair.of(rowData, errorMessage));
    }

    public void addSuccessRow(RowData<T> rowData) {
        successRows.add(rowData);
    }
}
