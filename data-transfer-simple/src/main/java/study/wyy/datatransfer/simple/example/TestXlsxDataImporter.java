package study.wyy.datatransfer.simple.example;

import lombok.extern.slf4j.Slf4j;
import study.wyy.datatransfer.api.exception.InvalidRowException;
import study.wyy.datatransfer.api.model.TaskResult;
import study.wyy.datatransfer.simple.model.Student;
import study.wyy.datatransfer.spring.annotations.Importer;
import study.wyy.datatransfer.spring.model.DataTransferContext;
import study.wyy.datatransfer.spring.model.RowData;
import study.wyy.datatransfer.spring.task.XlsxDataImporter;

/**
 * @author wyaoyao
 * @date 2021/2/25 18:06
 */
@Importer(name = "testStudentExport")
@Slf4j
public class TestXlsxDataImporter implements XlsxDataImporter<Student, DataTransferContext> {

    @Override
    public void importStarted(DataTransferContext context) {
        log.info("开始导入");
    }

    @Override
    public void importFinished(TaskResult executeResult, DataTransferContext context) {
        log.info("导入结束");
    }

    @Override
    public void importRowData(RowData<Student> data, DataTransferContext context) throws InvalidRowException {
        Student student = data.getData();
        System.out.println(student);
    }
}
