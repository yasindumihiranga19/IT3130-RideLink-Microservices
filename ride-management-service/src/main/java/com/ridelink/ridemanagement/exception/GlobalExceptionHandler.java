package com.ridelink.ridemanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RideNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRideNotFoundException(RideNotFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Ride Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidRideStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRideStatusException(InvalidRideStatusException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Ride Status", ex.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedRideOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedRideOperationException(UnauthorizedRideOperationException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Unauthorized Operation", ex.getMessage(), request);
    }

    @ExceptionHandler(DriverNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleDriverNotAvailableException(DriverNotAvailableException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Driver Not Available", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce("", (a, b) -> a + b + "; ");
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Error", message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now().toString(),
                status.value(),
                error,
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}
