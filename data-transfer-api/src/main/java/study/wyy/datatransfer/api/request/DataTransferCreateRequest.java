package study.wyy.datatransfer.api.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import study.wyy.datatransfer.api.enums.DataTransferTaskType;
import study.wyy.datatransfer.api.enums.FileType;
import study.wyy.datatransfer.api.utils.ParamUtils;

import java.io.Serializable;

/**
 * @author wyaoyao
 * @date 2021/2/25 14:00
 */
@Data
public class DataTransferCreateRequest implements Serializable {

    @ApiModelProperty("租户id")
    private Integer tenantId;

    @ApiModelProperty(value = "任务name，全局唯一",required = true)
    private String name;

    /**
     * extend name of file
     * @see FileType
     */
    @ApiModelProperty(value = "文件类型",required = true)
    private String fileExt;

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

    @ApiModelProperty(value = "导出时指定的文件名字",required = false)
    private String exportFileName;

    @ApiModelProperty(value = "导出任务时，导出文件的路径",required = false)
    private String exportPath;

    @ApiModelProperty(value = "导入任务时，导入文件的路径",required = false)
    private String importFilePath;


    public void checkParam() {
        ParamUtils.notBlank(name,"task.name.is.null");
        ParamUtils.notBlank(fileExt,"file.ext.is.null");
        ParamUtils.nonNull(FileType.fromExt(fileExt),"nonsupport.file.ext");
    }
}
