package org.example.be_sp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để đánh dấu các API endpoint cần xác thực token Sử dụng:
 * @RequireAuth trên method của controller
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuth {

    /**
     * Message trả về khi token không hợp lệ
     */
    String message() default "Token không hợp lệ hoặc đã hết hạn";

    /**
     * Có bắt buộc token hay không
     */
    boolean required() default true;
}
