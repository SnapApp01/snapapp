package com.snappapp.snapng.snap.api_lib.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snappapp.snapng.exceptions.ResourceAlreadyExistsException;
import com.snappapp.snapng.snap.api_lib.exceptions.ApiResponseCode;
import com.snappapp.snapng.snap.api_lib.exceptions.SnapApiException;
import com.snappapp.snapng.snap.api_lib.wrappers.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Iterator;

@ControllerAdvice(basePackages = "com.nimisitech.snapng")
@Slf4j
public class ApiServiceAdvice implements ResponseBodyAdvice<Object> {
    private final static ApiResponseCode defaultResponseCode = ApiResponseCode.UNKNOWN_ERROR;

    private final static String defaultErrorMsg = "Oops, there was an error from us and we are fixing!";

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleDataAlreadyExistsExceptions(ResourceAlreadyExistsException ex){
        ApiResponseCode code = ApiResponseCode.ALREADY_EXIST;
        log.warn(String.format("%s [%s] : %s",code.name(),code.getCode(),ex.getMessage()),ex);
        return new ResponseEntity<>(ApiResponse.errorResponse(ex.getMessage(), code),HttpStatus.EXPECTATION_FAILED);
    }
//
//    @ExceptionHandler(DataNotFoundException.class)
//    public ResponseEntity<ApiResponse> handleDataNotFoundExceptions(DataNotFoundException ex){
//        ApiResponseCode code = ApiResponseCode.ITEM_NOT_FOUND;
//        log.warn(String.format("%s [%s] : %s",code.name(),code.getCode(),ex.getMessage()),ex);
//        return new ResponseEntity<>(ApiResponse.errorResponse(ex.getMessage(), code),HttpStatus.EXPECTATION_FAILED);
//    }
//
//    @ExceptionHandler(InvalidProcessException.class)
//    public ResponseEntity<ApiResponse> handleException(InvalidProcessException ex){
//        ApiResponseCode code = ApiResponseCode.INCORRECT_TASK;
//        log.warn(String.format("%s [%s] : %s",code.name(),code.getCode(),ex.getMessage()),ex);
//        return new ResponseEntity<>(ApiResponse.errorResponse(ex.getMessage(), code),HttpStatus.UNPROCESSABLE_ENTITY);
//    }
//
//    @ExceptionHandler(UserNotRegisteredException.class)
//    public ResponseEntity<ApiResponse> handleException(UserNotRegisteredException ex){
//        ApiResponseCode code = ApiResponseCode.NOT_REGISTERED;
//        log.warn(String.format("%s [%s] : %s",code.name(),code.getCode(),ex.getMessage()),ex);
//        return new ResponseEntity<>(ApiResponse.errorResponse(ex.getMessage(), code),HttpStatus.EXPECTATION_FAILED);
//    }
//
//    @ExceptionHandler(InvalidCredentialsException.class)
//    public ResponseEntity<ApiResponse> handleException(InvalidCredentialsException ex){
//        ApiResponseCode code = ApiResponseCode.INVALID_CLIENT;
//        log.warn(String.format("%s [%s] : %s",code.name(),code.getCode(),ex.getMessage()),ex);
//        return new ResponseEntity<>(ApiResponse.errorResponse(ex.getMessage(), code),HttpStatus.UNAUTHORIZED);
//    }

    @ExceptionHandler(SnapApiException.class)
    public ResponseEntity<ApiResponse> handleSnapApiExceptions(SnapApiException ex){
        log.warn(String.format("%s [%s] : %s",ex.getResponseCode().name(),ex.getResponseCode().getCode(),ex.getMessage()),ex);
        return new ResponseEntity<>(ApiResponse.errorResponse(ex.getMessage(), ex.getResponseCode()),ex.getHttpStatus());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse> handleMissingHeaderException(MissingRequestHeaderException ex){
        log.warn("Missing header exception:",ex);
        return new ResponseEntity<>(ApiResponse.errorResponse(ex.getMessage(), ApiResponseCode.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(com.fasterxml.jackson.databind.exc.InvalidFormatException.class)
    public ResponseEntity<ApiResponse> handleInvalidFormatException(com.fasterxml.jackson.databind.exc.InvalidFormatException ex){
        log.warn("Invalid request format exception:",ex);
        return new ResponseEntity<>(ApiResponse.errorResponse(ex.getMessage(), ApiResponseCode.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex){
        log.warn("Json parse exception due to type mismatch: ",ex);
        return new ResponseEntity<>(ApiResponse.errorResponse(ex.getMessage(), ApiResponseCode.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }



    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ApiResponse> handleMethodNotSupported(MethodArgumentNotValidException ex) {
        ApiResponseCode code = ApiResponseCode.BAD_REQUEST;
        log.warn(String.format("%s [%s] : %s",code.name(),code.getCode(),ex.getMessage()),ex);
        StringBuilder errorBuilder = new StringBuilder();
        FieldError fieldError;
        for(Iterator<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().iterator(); fieldErrors.hasNext(); errorBuilder.append(fieldError.getDefaultMessage())) {
            fieldError = fieldErrors.next();
            errorBuilder.append(" ");

        }
        return new ResponseEntity<>(ApiResponse.errorResponse(errorBuilder.toString(),code), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception ex){
        log.error(String.format("%s [%s] : %s",defaultResponseCode.name(),defaultResponseCode.getCode(),ex.getMessage()),ex);
        return new ResponseEntity<>(ApiResponse.errorResponse(defaultErrorMsg, defaultResponseCode), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        ApiResponse rsp = body instanceof ApiResponse ? (ApiResponse) body : ApiResponse.successResponse(body);
        if (body instanceof String) {
            ObjectMapper mapper = new ObjectMapper();

            try {
                return mapper.writeValueAsString(rsp);
            } catch (JsonProcessingException var10) {
                return body;
            }
        }
        return rsp;
    }
}
