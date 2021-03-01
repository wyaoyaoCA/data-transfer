package study.wyy.datatransfer.spring.annotations;

import java.lang.annotation.*;

/**
 * @author wyaoyao
 * @description
 * @date 2021/1/20 15:38
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface NeedMerge {
}
