package com.dr.digital.manage.log.annotation;

import java.lang.annotation.*;

/**
 * @author caor
 * @date 2020/8/12 11:05
 */
@Target(ElementType.METHOD) //注解放置的目标位置,METHOD是可注解在方法级别上
@Retention(RetentionPolicy.RUNTIME) //注解在哪个阶段执行
@Documented //生成文档
public @interface SysLog {
    String value() default "";
}
