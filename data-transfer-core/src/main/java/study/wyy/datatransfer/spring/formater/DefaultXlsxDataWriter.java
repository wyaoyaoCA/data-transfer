package study.wyy.datatransfer.spring.formater;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.var;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wyaoyao
 * @date 2021/2/23 17:43
 */
public class DefaultXlsxDataWriter<T> implements XlsxDataWriter<T> {

    private final ObjectMapper mapper = new ObjectMapper();

    private static final Integer PAGE_SIZE = 2000;
    private final SXSSFWorkbook xlsBook;
    private final Sheet sheet;
    private final Class model;
    private Map<String, String> headerAlias;

    private List<String> headers = new ArrayList<>();
    private int cursorLine = 0;

    public DefaultXlsxDataWriter(Class<T> model) {
        xlsBook = new SXSSFWorkbook(PAGE_SIZE);
        sheet = xlsBook.createSheet();
        this.model = model;
    }

    private List<String> getHeadersFromModel(Class dataModel) {
        List<String> ret = new ArrayList<>();
        for(Field field : dataModel.getDeclaredFields()) {
            var shouldIgnore = field.getAnnotation(JsonIgnore.class);
            if(null != shouldIgnore) {
                continue;
            }
            var property = field.getAnnotation(JsonProperty.class);
            String name = field.getName();
            if(null != property && StringUtils.isNotEmpty(property.value())) {
                name = property.value();
            }
            ret.add(name);
        }
        return ret;
    }

    private void writeHead(T data) {
        if(CollectionUtils.isNotEmpty(this.headers)) {
            // 文件的头已经写入了
            return;
        }
        Class headersModel = null == model ? data.getClass() : model;
        if(headersModel == null) {
            return;
        }
        if(Map.class.isAssignableFrom(headersModel)) {
            headers = Arrays.stream(((Map) data).keySet().toArray())
                    .map(String::valueOf).collect(Collectors.toList());
        } else {
            headers = getHeadersFromModel(headersModel);
        }
        Row row = sheet.createRow(0);
        for(int j=0; j<headers.size(); ++j) {
            String fieldName = headers.get(j);
            Cell cell = row.createCell(j);
            cell.setCellValue(fieldName);
        }
    }

    @Override
    public void writeLine(T data, OutputStream outputStream) {
        if(null == data) {
            cursorLine+=1;
            return;
        }
        writeHead(data);
        Map dataMap;
        if(Map.class.isAssignableFrom(data.getClass())) {
            dataMap = (Map) data;
        } else {
            dataMap = mapper.convertValue(data, Map.class);
        }
        writeMap(dataMap, outputStream);
    }

    private void writeMap(Map data, OutputStream outputStream) {
        Row row = sheet.createRow(++cursorLine);
        for(int j=0; j<headers.size(); ++j) {
            String name = headers.get(j);
            String value = Objects.toString(data.get(name), "");
            Cell cell = row.createCell(j);
            cell.setCellValue(value);
        }
    }


    @Override
    public void flush(OutputStream outputStream) throws IOException {
        xlsBook.write(outputStream);
    }
}
