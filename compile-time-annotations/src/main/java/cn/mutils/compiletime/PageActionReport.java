package cn.mutils.compiletime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wenhua.ywh on 2016/8/11.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface PageActionReport {
    String[] actions() default {};

    String[] pages() default {};
}
