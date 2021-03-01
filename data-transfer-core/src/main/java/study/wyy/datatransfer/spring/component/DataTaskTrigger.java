package study.wyy.datatransfer.spring.component;

import study.wyy.datatransfer.api.enums.DataTransferTaskType;
import study.wyy.datatransfer.api.enums.TaskStatus;
import study.wyy.datatransfer.api.model.DataTransferTask;
import study.wyy.datatransfer.api.request.DataTransferCreateRequest;
import study.wyy.datatransfer.api.service.TaskStorageService;
import study.wyy.datatransfer.spring.executor.DataTransferTaskExecutor;

import java.util.Date;

/**
 * @author wyaoyao
 * @date 2021/2/25 14:16
 */
public class DataTaskTrigger {

    private final DataTransferTaskExecutor dataTransferTaskExecutor;
    private final TaskStorageService taskStorageService;

    public DataTaskTrigger(DataTransferTaskExecutor dataTransferTaskExecutor, TaskStorageService taskStorageService) {
        this.dataTransferTaskExecutor = dataTransferTaskExecutor;
        this.taskStorageService = taskStorageService;
    }

    public DataTransferTask doExport(DataTransferCreateRequest request){
        DataTransferTask task = convert(request);
        task.setType(DataTransferTaskType.EXPORT.getCode());
        task.setStatus(TaskStatus.RUNNING.getCode());
        taskStorageService.save(task);
        dataTransferTaskExecutor.executeAsync(task);
        return task;
    }

    public DataTransferTask doImport(DataTransferCreateRequest request){
        DataTransferTask task = convert(request);
        task.setType(DataTransferTaskType.IMPORT.getCode());
        task.setStatus(TaskStatus.RUNNING.getCode());
        taskStorageService.save(task);
        dataTransferTaskExecutor.executeAsync(task);
        return task;
    }

    private DataTransferTask convert(DataTransferCreateRequest request){
        DataTransferTask task = new DataTransferTask();
        task.setFileExt(request.getFileExt());
        task.setName(request.getName());
        task.setTenantId(request.getTenantId());
        task.setDescription(request.getDescription());
        task.setExportFileName(request.getExecuteParam());
        task.setExportPath(request.getExportPath());
        task.setExportFileName(request.getExportFileName());
        task.setImportFilePath(request.getImportFilePath());
        task.setUserId(request.getUserId());
        task.setStartTime(new Date());
        return task;
    }


}
