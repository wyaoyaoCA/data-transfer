package study.wyy.datatransfer.spring.component;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import study.wyy.datatransfer.api.model.DataTransferTask;
import study.wyy.datatransfer.api.request.DataTransferCreateRequest;
import study.wyy.datatransfer.api.service.TaskStorageService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.UUID;

/**
 * @author wyaoyao
 * @date 2021/2/25 10:51
 */
@RestController
@RequestMapping("data/transfer")
@Slf4j
public class DataTransferRestController {

    private final DataTaskTrigger dataTaskTrigger;
    private final TaskStorageService taskStorageService;
    private final File temp;

    @Autowired
    public DataTransferRestController(DataTaskTrigger dataTaskTrigger, TaskStorageService taskStorageService) throws IOException {
        this.dataTaskTrigger = dataTaskTrigger;
        this.taskStorageService = taskStorageService;
        // 上传文件的临时保存目录
        temp = Files.createTempDirectory("data_transfer_import_temp").toFile();
    }

    @PostMapping("exportTask")
    @ApiOperation("创建导出任务")
    public DataTransferTask exportTask(@RequestBody DataTransferCreateRequest request) {
        request.checkParam();
        return dataTaskTrigger.doExport(request);
    }


    @PostMapping(value = "importTask")
    @ApiOperation("创建导入任务")
    public DataTransferTask importTask(DataTransferCreateRequest request, @RequestPart(required = false) MultipartFile importFile) throws IOException {
        request.checkParam();
        if (Objects.isNull(importFile) && StringUtils.isBlank(request.getImportFilePath())) {
            log.error("import file must define by importFile or importFilePath in request");
            throw new IllegalArgumentException("import.file.is.null");
        }
        if(Objects.nonNull(importFile)){
            // 保存上传的文件
            File tempFile = createTempFile(importFile.getName());
            importFile.transferTo(tempFile);
            request.setImportFilePath(tempFile.getPath());
        }
        return dataTaskTrigger.doImport(request);
    }

    @GetMapping("result/{id}")
    @ApiOperation("查询任务")
    public DataTransferTask result(@PathVariable Long id) {
        DataTransferTask transferTask = taskStorageService.findById(id);
        return transferTask;
    }

    /**
     * 创建临时文件
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    private File createTempFile(String fileName) throws IOException {
        String tempFileName = UUID.randomUUID().toString() + "__" + fileName;
        String absFilePath = Paths.get(temp.getPath(), tempFileName).toString();
        File directory = new File(Paths.get(absFilePath).getParent().toString());
        if (!directory.exists()) {
            boolean d = directory.mkdirs();
            if (!d) {
                throw new IOException(MessageFormat.format(
                        "Failed to create folder, {0}", directory));
            }
        }
        return new File(absFilePath);
    }
}
