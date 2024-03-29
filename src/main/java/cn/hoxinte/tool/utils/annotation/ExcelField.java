package cn.hoxinte.tool.utils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * Excel解析用注解, 加于字段上生成Excel或者解析Excel文件
 * @author  dominate
 * @since 2021-12-28 14:40
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelField {
    String value() default "";
}