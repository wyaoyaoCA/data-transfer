package study.wyy.datatransfer.api.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wyaoyao
 * @description 分页查询基类
 * @date 2021/1/18 13:50
 */
@Data
public class PageInfo implements Serializable {

    private Integer pageNo;

    private Integer pageSie;

    public PageInfo(Integer pageNo, Integer pageSie) {
        this.pageNo = pageNo;
        this.pageSie = pageSie;
    }

    public PageInfo() {
        this(1, 20);
    }


    public Integer getOffset() {
        return (getPageNo() - 1) * pageSie;
    }

    public Boolean hasNext(){
        return null;

    }
    public void nextPage(){
        if (this.pageNo == null) {
            this.pageNo = 1;
        }
        this.pageNo++;
    }
}
