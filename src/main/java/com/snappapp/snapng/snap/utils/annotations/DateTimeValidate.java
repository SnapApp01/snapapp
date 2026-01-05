package com.snappapp.snapng.snap.utils.annotations;

import com.snappapp.snapng.snap.utils.enums.DateTimeType;
import com.snappapp.snapng.snap.utils.validators.DateTimeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {DateTimeValidator.class})
public @interface DateTimeValidate {

    String message() default "Incorrect date format";

    boolean nullable() default false;

    DateTimeType type();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
