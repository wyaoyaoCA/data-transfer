package study.wyy.datatransfer.spring.spi.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import study.wyy.datatransfer.api.enums.TaskStatus;
import study.wyy.datatransfer.api.model.DataTransferTask;
import study.wyy.datatransfer.api.model.TaskResult;
import study.wyy.datatransfer.api.service.TaskStorageService;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * @author wyaoyao
 * @date 2021/2/25 15:40
 */
@Slf4j
public class RedisTaskStorageService implements TaskStorageService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final String PREFIX = "data_transfer:result:";


    public RedisTaskStorageService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));
    }

    @Override
    public void save(DataTransferTask dataTransferTask) {
        try {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            Random random = new Random(System.currentTimeMillis());
            int num = random.nextInt(99);
            dataTransferTask.setId(Long.valueOf(time + num));
            String json = objectMapper.writer().writeValueAsString(dataTransferTask);
            redisTemplate.opsForValue().set(PREFIX + dataTransferTask.getId().toString(), json, Duration.ofHours(24));
        } catch (JsonProcessingException e) {
            log.error("save task to redis fail task: {}", dataTransferTask, e);
        }
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
        try {
            String json = objectMapper.writer().writeValueAsString(task);
            redisTemplate.opsForValue().set(PREFIX + task.getId().toString(), json, Duration.ofHours(24));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public DataTransferTask findById(Long id) {
        try {
            String value = redisTemplate.opsForValue().get(PREFIX + id.toString());
            if (StringUtils.isNotEmpty(value)) {
                return objectMapper.readValue(value, DataTransferTask.class);
            }
            return null;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Boolean deleteById(Long id) {
        Boolean delete = redisTemplate.delete(PREFIX + id.toString());
        return delete;
    }
}
