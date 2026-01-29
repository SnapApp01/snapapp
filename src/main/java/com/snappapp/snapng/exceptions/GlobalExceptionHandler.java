package com.snappapp.snapng.exceptions;

import com.snappapp.snapng.dto.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException exception) {

        List<String> errorMessage = new ArrayList<>();

        exception.getBindingResult().getFieldErrors().forEach(error -> {
            errorMessage.add(error.getDefaultMessage());
        });
        return ResponseEntity
                .badRequest()
                .body(
                        GenericResponse.builder()
                                .isSuccess(false)
                                .message("Registration Failed: Please provide valid data.")
                                .data(errorMessage)
                                .httpStatus(HttpStatus.BAD_REQUEST)
                                .build()
                );
    }

    @ExceptionHandler(value = UserAlreadyExistsException.class)
    public ResponseEntity<GenericResponse> UserAlreadyExistsExceptionHandler(UserAlreadyExistsException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        GenericResponse.builder()
                                .isSuccess(false)
                                .message(exception.getMessage())
                                .httpStatus(HttpStatus.CONFLICT)
                                .build()
                );
    }

    @ExceptionHandler(value = InsufficientBalanceException.class)
    public ResponseEntity<GenericResponse> InsufficientBalanceExceptionHandler(InsufficientBalanceException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        GenericResponse.builder()
                                .isSuccess(false)
                                .message(exception.getMessage())
                                .httpStatus(HttpStatus.BAD_REQUEST)
                                .build()
                );
    }


    @ExceptionHandler(value = DeliveryAlreadyAssignedException.class)
    public ResponseEntity<GenericResponse> DeliveryAlreadyAssignedExceptionHandler(DeliveryAlreadyAssignedException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        GenericResponse.builder()
                                .isSuccess(false)
                                .message(exception.getMessage())
                                .httpStatus(HttpStatus.CONFLICT)
                                .build()
                );
    }

    @ExceptionHandler(value = OtpDeliveryException.class)
    public ResponseEntity<GenericResponse> OtpDeliveryExceptionHandler(OtpDeliveryException exception) {
        return ResponseEntity
                .status(HttpStatus.EXPECTATION_FAILED)
                .body(
                        GenericResponse.builder()
                                .isSuccess(false)
                                .message(exception.getMessage())
                                .httpStatus(HttpStatus.EXPECTATION_FAILED)
                                .build()
                );
    }

    @ExceptionHandler(value = TokenRefreshException.class)
    public ResponseEntity<GenericResponse> TokenRefreshExceptionHandler(TokenRefreshException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        GenericResponse.builder()
                                .isSuccess(false)
                                .message(exception.getMessage())
                                .httpStatus(HttpStatus.BAD_REQUEST)
                                .build()
                );
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    public ResponseEntity<GenericResponse> InvalidTokenExceptionHandler(InvalidTokenException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        GenericResponse.builder()
                                .isSuccess(false)
                                .message(exception.getMessage())
                                .httpStatus(HttpStatus.BAD_REQUEST)
                                .build()
                );
    }

    @ExceptionHandler(value = InvalidRequestException.class)
    public ResponseEntity<GenericResponse> InvalidRequestExceptionHandler(InvalidRequestException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        GenericResponse.builder()
                                .isSuccess(false)
                                .message(exception.getMessage())
                                .httpStatus(HttpStatus.BAD_REQUEST)
                                .build()
                );
    }

    @ExceptionHandler(value = EmailException.class)
    public ResponseEntity<GenericResponse> EmailExceptionHandler(EmailException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        GenericResponse.builder()
                                .isSuccess(false)
                                .message(exception.getMessage())
                                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                .build()
                );
    }

    @ExceptionHandler(value = InvalidCredentialsException.class)
    public ResponseEntity<GenericResponse> InvalidCredentialsExceptionHandler(InvalidCredentialsException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        GenericResponse.builder()
                                .isSuccess(false)
                                .message(exception.getMessage())
                                .httpStatus(HttpStatus.BAD_REQUEST)
                                .build()
                );
    }

    @ExceptionHandler(value = FailedProcessException.class)
    public ResponseEntity<GenericResponse> FailedProcessExceptionHandler(FailedProcessException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        GenericResponse.builder()
                                .isSuccess(false)
                                .message(exception.getMessage())
                                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                .build()
                );
    }

    @ExceptionHandler(value = MediaNotFoundException.class)
    public ResponseEntity<GenericResponse> MediaNotFoundExceptionHandler(MediaNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        GenericResponse.builder()
                                .isSuccess(false)
                                .message(exception.getMessage())
                                .httpStatus(HttpStatus.NOT_FOUND)
                                .build()
                );
    }

    @ExceptionHandler(value = InvalidMediaTypeException.class)
    public ResponseEntity<GenericResponse> InvalidMediaTypeExceptionHandler(InvalidMediaTypeException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        GenericResponse.builder()
                                .isSuccess(false)
                                .message(exception.getMessage())
                                .httpStatus(HttpStatus.BAD_REQUEST)
                                .build()
                );
    }

    @ExceptionHandler(value = RoleNotFoundException.class)
    public ResponseEntity<GenericResponse> RoleNotFoundExceptionHandler(RoleNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        GenericResponse.builder()
                                .isSuccess(false)
                                .message("Failed! Role not found.")
                                .httpStatus(HttpStatus.NOT_FOUND)
                                .build()
                );
    }

    @ExceptionHandler(value = MediaUploadFailedException.class)
    public ResponseEntity<GenericResponse> MediaUploadFailedExceptionHandler(MediaUploadFailedException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        GenericResponse.builder()
                                .isSuccess(false)
                                .message("Failed! Role not found.")
                                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                .build()
                );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GenericResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        GenericResponse response = GenericResponse.builder()
                .isSuccess(false)
                .message(ex.getMessage())
                .httpStatus(HttpStatus.NOT_FOUND)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<GenericResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        GenericResponse response = GenericResponse.builder()
                .isSuccess(false)
                .message(ex.getMessage())
                .httpStatus(HttpStatus.CONFLICT)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<GenericResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = "Registration failed: An error occurred.";
        if (ex.getMessage().contains("ukdu5v5sr43g5bfnji4vb8hg5s3")) {
            message = "The provided phone number is already registered.";
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        GenericResponse.builder()
                                .isSuccess(false)
                                .message(message)
                                .data(null)
                                .httpStatus(HttpStatus.CONFLICT)
                                .build()
                );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GenericResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        GenericResponse response = GenericResponse.builder()
                .isSuccess(false)
                .message(ex.getMessage())
                .data(null)
                .httpStatus(HttpStatus.BAD_REQUEST)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}