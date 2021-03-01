package study.wyy.datatransfer.simple.example;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import study.wyy.datatransfer.api.exception.DataTransferException;
import study.wyy.datatransfer.api.model.PageInfo;
import study.wyy.datatransfer.api.model.PageResult;
import study.wyy.datatransfer.api.model.TaskResult;
import study.wyy.datatransfer.simple.model.Student;
import study.wyy.datatransfer.spring.annotations.Exporter;
import study.wyy.datatransfer.spring.model.DataTransferContext;
import study.wyy.datatransfer.spring.task.XlsxDataExporter;

import java.util.Collections;
import java.util.List;

/**
 * @author wyaoyao
 * @date 2021/2/25 17:16
 */
@Slf4j
@Exporter(name = "testStudentExport")
public class TestXlsxDataExporter implements XlsxDataExporter<Student, DataTransferContext> {

    @Override
    public PageResult<Student> exportData(PageInfo pageInfo, DataTransferContext context) {
        Integer pageNo = pageInfo.getPageNo();
        Integer pageSie = pageInfo.getPageSie();
        if(pageNo ==3){
            return new PageResult<>(6L, Collections.EMPTY_LIST);
        }
        List<Student> students = Lists.newArrayList();
        students.add(new Student(1L,"1",1));
        students.add(new Student(2L,"2",2));
        students.add(new Student(2L,"3",3));
        return new PageResult<>(3L,students);
    }

    @Override
    public String generateFileName(DataTransferContext context) {
        return "测试导出";
    }

    @Override
    public void exportStarted(DataTransferContext context) throws DataTransferException {
        log.info("开始导出");
    }

    @Override
    public void exportFinished(TaskResult executeResult, DataTransferContext context) throws DataTransferException {
        log.info("导出结束回调");
    }
}
