package study.wyy.datatransfer.api.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装
 * @author wyaoyao
 * @description
 * @date 2021/1/15 13:45
 *
 */
@Data
public class PageResult<T> implements Serializable {

    /**
     * 数据总数
     */
    private Long total;

    /**
     * 分页数据
     */
    private List<T> data;

    private Integer pageNo;

    private Integer pageSize;

    public PageResult() {
    }

    public PageResult(Long total, List<T> data) {
        this.total = total;
        this.data = data;
    }

    public PageResult(Long total, List<T> data, Integer pageNo, Integer pageSize) {
        this.total = total;
        this.data = data;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

}
