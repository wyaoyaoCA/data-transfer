package study.wyy.datatransfer.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wyaoyao
 * @description
 * @date 2021/2/23 12:00
 * 任务状态
 */
@AllArgsConstructor
@Getter
public enum  TaskStatus {
    CANCEL(-100, "已取消"),
    PARTLY_ERROR(-5, "部分数据导入出错"),
    DELETE_ERROR(-4, "删除时发生错误"),
    FINISH_ERROR(-3, "执行结束回调失败"),
    TIMEOUT(-2, "执行超时"),
    ERROR(-1, "失败"),
    PENDING(0, "等待中"),
    RUNNING(1, "执行中"),
    SUCCESS(2, "已完成"),
    DELETE(3, "已删除"),;


    private int code;
    private String desc;
}
