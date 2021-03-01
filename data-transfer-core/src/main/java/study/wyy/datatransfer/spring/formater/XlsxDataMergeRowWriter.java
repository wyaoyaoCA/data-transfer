package study.wyy.datatransfer.spring.formater;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.var;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import study.wyy.datatransfer.spring.annotations.NeedMerge;
import study.wyy.datatransfer.spring.model.Group;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author wyaoyao
 * @description
 * @date 2021/1/20 15:39
 */
public class XlsxDataMergeRowWriter<T> implements XlsxDataWriter<T> {

    private ObjectMapper objectMapper = new ObjectMapper();
    private final SXSSFWorkbook xlsBook;
    private final Sheet sheet;
    private static final Integer PAGE_SIZE = 2000;

    private int mergeRowSize;
    List<Pair<Boolean, String>> headers = new ArrayList<>();

    /**
     * 偏移量
     */
    private int offset = 1;

    /**
     * 数据写入的游标
     */
    private int cursor = 0;
    public XlsxDataMergeRowWriter() {
        xlsBook = new SXSSFWorkbook(PAGE_SIZE);
        sheet = xlsBook.createSheet();
    }
    @Override
    public void writeLine(T t, OutputStream outputStream) {
        Group group = (Group)t;
        Class<T> model = (Class<T>) group.getData().get(0).getClass();
        writeHead(model);
        // 1 取出group里的数据
        List<T> data = group.getData();
        // 2 mergeRowSize
        mergeRowSize = group.getData().size();
        for (cursor = offset; cursor < data.size() + offset; cursor++) {
            // 获取组里的数据
            var dataLine = data.get(cursor - offset);
            writeGroupData(dataLine, outputStream);

        }
        offset = offset + mergeRowSize;
    }

    public void writeGroupData(T data, OutputStream outputStream) {
        Row row = sheet.createRow(cursor);
        Map map = objectMapper.convertValue(data, Map.class);
        for (int i = 0; i < headers.size(); ++i) {
            Pair<Boolean, String> pair = headers.get(i);
            if (pair.getLeft().equals(Boolean.TRUE) && cursor == offset && mergeRowSize > 1) {
                // 该字段需要合并, 写入该组的第一条数据合并，其他时候不在写入这些共同的数据，并且group中的数据大于1
                sheet.addMergedRegion(new CellRangeAddress(
                        offset, offset + mergeRowSize - 1, i, i));
                String value = Objects.toString(map.get(pair.getRight()),"");
                Cell cell = row.createCell(i);
                cell.setCellValue(value);
            } else {
                String value = Objects.toString(map.get(pair.getRight()),"");
                Cell cell = row.createCell(i);
                cell.setCellValue(value);
            }
        }
    }

    public List<Pair<Boolean, String>> getHeadersFromModel(Class dataModel) {
        List<Pair<Boolean, String>> ret = new ArrayList<>();
        for (Field field : dataModel.getDeclaredFields()) {
            JsonIgnore shouldIgnore = field.getAnnotation(JsonIgnore.class);
            if (null != shouldIgnore) {
                continue;
            }
            Pair pair;
            JsonProperty property = field.getAnnotation(JsonProperty.class);
            NeedMerge needMerge = field.getAnnotation(NeedMerge.class);
            String name = field.getName();
            if (null != property && StringUtils.isNotEmpty(property.value())) {
                name = property.value();
            }
            if (needMerge != null) {
                pair = Pair.of(Boolean.TRUE, name);
            } else {
                pair = Pair.of(Boolean.FALSE, name);
            }
            ret.add(pair);
        }
        return ret;
    }

    private void writeHead(Class<T> dataModelType) {
        if (CollectionUtils.isNotEmpty(headers)) {
            return;
        }
        headers = getHeadersFromModel(dataModelType);
        Row row = sheet.createRow(0);
        for (int j = 0; j < headers.size(); ++j) {
            String aliasName = headers.get(j).getRight();
            Cell cell = row.createCell(j);
            cell.setCellValue(aliasName);
        }
    }

    @Override
    public void flush(OutputStream outputStream) throws IOException {
        xlsBook.write(outputStream);
    }
}
