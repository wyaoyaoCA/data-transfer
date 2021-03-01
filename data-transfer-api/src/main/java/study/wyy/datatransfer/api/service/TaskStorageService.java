package study.wyy.datatransfer.api.service;

import study.wyy.datatransfer.api.model.DataTransferTask;
import study.wyy.datatransfer.api.model.TaskResult;

/**
 * @author wyaoyao
 * @date 2021/2/25 10:07
 */
public interface TaskStorageService {

    void save(DataTransferTask dataTransferTask);

    void update(TaskResult result);

    DataTransferTask findById(Long id);

    Boolean deleteById(Long id);



}
