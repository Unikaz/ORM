package fr.unikaz.unikorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {
    String name() default "";
    boolean autoIncrement() default false;
    boolean primaryKey() default false;
    boolean unsigned() default false;
}

