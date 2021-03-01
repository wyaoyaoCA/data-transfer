package study.wyy.datatransfer.spring.annotations;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author wyaoyao
 * @date 2021/2/23 16:27
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Exporter {

    String name() default "";

}
