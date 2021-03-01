package study.wyy.datatransfer.spring.model;

import java.util.List;

/**
 * @author wyaoyao
 * @description
 * @date 2021/1/20 15:41
 */
public class Group<T> {

    private List<T> elements;

    public Group(List<T> elements) {
        this.elements = elements;
    }

    public List<T> getData() {
        return elements;
    }

    public void add(T t) {
        elements.add(t);
    }

    public Boolean remove(T t) {
        return elements.remove(t);
    }
}
