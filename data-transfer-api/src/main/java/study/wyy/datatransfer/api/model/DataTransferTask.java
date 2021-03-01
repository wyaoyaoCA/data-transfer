package study.wyy.datatransfer.api.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import study.wyy.datatransfer.api.enums.DataTransferTaskType;
import study.wyy.datatransfer.api.enums.FileType;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author wyaoyao
 * @description
 * @date 2021/2/23 12:05
 */
@Data
public class DataTransferTask implements Serializable {

    /**
     * 任务id
     */
    @ApiModelProperty("任务id")
    private Long id;

    @ApiModelProperty("租户id")
    private Integer tenantId;

    @ApiModelProperty(value = "任务name，全局唯一",required = true)
    private String name;

    /**
     * 任务类型
     *  @see DataTransferTaskType
     */
    @ApiModelProperty(value = "任务类型",required = true)
    private Integer type;

    /**
     * extend name of file
     * @see FileType
     */
    @ApiModelProperty(value = "文件类型",required = true)
    private String fileExt;

    /**
     * 任务开始时间
     */
    @ApiModelProperty("任务开始时间")
    private Date startTime;

    /**
     * task execute finished time
     */
    @ApiModelProperty("任务结束时间")
    private Date finishTime;

    /**
     * task execute param
     */
    @ApiModelProperty(value = "当前任务的执行参数，json",required = false)
    private String executeParam;

    /**
     * user id
     */
    @ApiModelProperty(value = "用户id",required = false)
    private String userId;

    /**
     * Description of task
     */
    @ApiModelProperty(value = "任务描述",required = false)
    private String description;

    /**
     * task status
     */
    @ApiModelProperty("任务状态")
    private Integer status;

    @ApiModelProperty(value = "导出时指定的文件名字",required = false)
    private String exportFileName;

    @ApiModelProperty(value = "导出任务时，导出文件的路径",required = false)
    private String exportPath;

    @ApiModelProperty(value = "导入任务时，导入文件的路径",required = false)
    private String importFilePath;

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
     * 文件url
     */
    @ApiModelProperty("文件url")
    private String filePath;


    /**
     * 导入失败文件路径
     */
    private String errorRecordsFilePath;

    /**
     * 导入失败文件路径
     */
    private String errorRecordsUrl;

    /**
     * error 信息
     */
    @ApiModelProperty("错误信息")
    private String error;

    private Map<String,Object> extra;

}
