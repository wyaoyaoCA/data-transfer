package study.wyy.datatransfer.spring.spi.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import study.wyy.datatransfer.api.enums.TaskStatus;
import study.wyy.datatransfer.api.model.DataTransferTask;
import study.wyy.datatransfer.api.model.TaskResult;
import study.wyy.datatransfer.api.service.TaskStorageService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 本地 guava cache实现
 * @author wyaoyao
 * @date 2021/2/25 10:11
 */
@Slf4j
public class LocalGuavaCacheTaskStorageService implements TaskStorageService {
    private final Cache<String, DataTransferTask> cache;

    public LocalGuavaCacheTaskStorageService() {
        this.cache = CacheBuilder.newBuilder().build();
    }

    @Override
    public void save(DataTransferTask task) {
        Long id = generateTaskId(task);
        task.setId(id);
        cache.put(id.toString(), task);
    }

    @Override
    public void update(TaskResult result) {
        DataTransferTask task = result.getTransferTask();
        task.setFinishTime(result.getFinishTime());
        task.setStatus(result.getStatus());
        task.setErrorCount(result.getErrorCount());
        task.setSuccessCount(result.getSuccessCount());
        task.setExtra(result.getExtra());
        task.setErrorRecordsFilePath(result.getErrorRecordsFilePath());
        task.setErrorRecordsUrl(result.getErrorRecordsUrl());
        task.setFileUrl(result.getFileUrl());
        task.setFilePath(result.getExportFilePath());
        if (TaskStatus.SUCCESS.getCode() == result.getStatus()
                && null != result.getErrorCount() && result.getErrorCount() > 0) {
            task.setStatus(TaskStatus.PARTLY_ERROR.getCode());
        } else {
            task.setStatus(result.getStatus());
        }
        cache.put(task.getId().toString(), task);
    }

    @Override
    public DataTransferTask findById(Long id) {
        DataTransferTask transferTask = cache.getIfPresent(id.toString());
        return transferTask;
    }

    @Override
    public Boolean deleteById(Long id) {
        cache.invalidate(id.toString());
        return Boolean.TRUE;
    }


    private Long generateTaskId(DataTransferTask task){
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        Random random = new Random(System.currentTimeMillis());
        int num = random.nextInt(99);
        return Long.valueOf(time + num);
    }

}
