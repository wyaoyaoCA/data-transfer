package study.wyy.datatransfer.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author wyaoyao
 * @description
 * @date 2021/2/23 11:12
 * 任务类型
 */
@AllArgsConstructor
@Getter
public enum DataTransferTaskType {
    EXPORT(1, "数据导出"),
    IMPORT(2, "数据导入");

    private Integer code;

    private String desc;

    private static Map<Integer, DataTransferTaskType> codeMap = new HashMap();

    static {
        DataTransferTaskType[] values = DataTransferTaskType.values();
        for (DataTransferTaskType taskType : values) {
            codeMap.put(taskType.code, taskType);
        }
    }

    public static DataTransferTaskType from(Integer code){
        if(Objects.isNull(code)){
            throw new IllegalArgumentException("code.is.null");
        }
        DataTransferTaskType transferTaskType = codeMap.get(code);
        if(Objects.isNull(transferTaskType)){
            throw new IllegalArgumentException("nonsupport.task.type");
        }
        return transferTaskType;
    }
}
