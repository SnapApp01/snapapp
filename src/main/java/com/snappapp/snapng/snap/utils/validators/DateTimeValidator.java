package com.snappapp.snapng.snap.utils.validators;

import com.google.api.client.util.Strings;
import com.snappapp.snapng.snap.utils.annotations.DateTimeValidate;
import com.snappapp.snapng.snap.utils.enums.DateTimeType;
import com.snappapp.snapng.snap.utils.utilities.DateTimeUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateTimeValidator implements ConstraintValidator<DateTimeValidate,String> {
    private boolean nullable;
    private DateTimeType type;

    @Override
    public void initialize(DateTimeValidate constraintAnnotation) {
        this.nullable = constraintAnnotation.nullable();
        this.type = constraintAnnotation.type();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if(Strings.isNullOrEmpty(s)){
            return nullable;
        }
        switch (type){
            case DATE:
                return isValidDate(s);
            case TIME:
                return isValidTime(s);
            case DATETIME:
                return isValidDateTime(s);
            default:
                return false;
        }
    }

    private boolean isValidDate(String s){
        try{
            DateTimeUtils.parseDate(s);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    private boolean isValidDateTime(String s){
        try{
            DateTimeUtils.parseDateTime(s);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    private boolean isValidTime(String s){
        try{
            DateTimeUtils.parseTime(s);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
}
