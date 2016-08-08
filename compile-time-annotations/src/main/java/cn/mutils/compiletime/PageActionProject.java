package cn.mutils.compiletime;

/**
 * Created by wenhua.ywh on 2016/8/6.
 *
 * 用于配置项目编译
 */
public @interface PageActionProject {

    public static enum ProjectType {
        LIBRARY,
        APPLICATION
    }

    String name() default "";

    ProjectType type() default ProjectType.LIBRARY;

    String[] dependencies() default {};

}
