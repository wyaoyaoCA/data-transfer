package study.wyy.datatransfer.api.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 任务结果
 * @author wyaoyao
 * @description
 * @date 2021/2/23 12:04
 *
 */
@Data
public class TaskResult implements Serializable {

    private DataTransferTask transferTask;

    @ApiModelProperty("导出文件路径")
    private String exportFilePath;

    /**
     * @see study.wyy.datatransfer.api.enums.TaskStatus
     */
    @ApiModelProperty("任务状态")
    private Integer status;

    /**
     * 成功数量
     */
    @ApiModelProperty("成功数量")
    private Long successCount;

    /**
     * 失败数量
     */
    @ApiModelProperty("失败数量")
    private Long errorCount;

    /**
     * 文件url
     */
    @ApiModelProperty("文件url")
    private String fileUrl;

    /**
     * error 信息
     */
    @ApiModelProperty("错误信息")
    private String error;

    @ApiModelProperty("结束时间")
    private Date finishTime;

    @ApiModelProperty("任务执行参数")
    private String executeParam;

    /**
     * 导入失败文件路径
     */
    private String errorRecordsFilePath;

    /**
     * 错误记录的文件地址
     */
    private String errorRecordsUrl;

    private Map<String,Object> extra;
}
