package study.wyy.datatransfer.spring.executor;

import io.swagger.annotations.ApiOperation;
import study.wyy.datatransfer.api.model.DataTransferTask;
import study.wyy.datatransfer.spring.task.DataExporter;
import study.wyy.datatransfer.spring.task.DataImporter;

/**
 * @author wyaoyao
 * @date 2021/2/23 14:20
 */
public interface DataTransferTaskExecutor {

    /**
     * 异步执行任务
     * @param transferTask 任务
     * @return
     */
    @ApiOperation("执行任务")
    Boolean executeAsync(DataTransferTask transferTask);


    void registerImporter(DataImporter dataImporter);

    void registerExporter(DataExporter dataExporter);
}
