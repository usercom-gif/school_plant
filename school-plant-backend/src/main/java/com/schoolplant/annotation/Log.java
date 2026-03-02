package com.schoolplant.annotation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 模块名称 (ROLE/PLANT/APPLICATION/TASK/KNOWLEDGE/ADOPTION)
     */
    String module() default "";

    /**
     * 操作标题/描述
     */
    String title() default "";

    /**
     * 操作类型 (INSERT/UPDATE/DELETE/AUDIT/etc.)
     */
    String operationType() default "";
}
