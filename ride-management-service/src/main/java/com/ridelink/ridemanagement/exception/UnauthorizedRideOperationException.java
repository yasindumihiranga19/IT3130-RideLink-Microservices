package com.ridelink.ridemanagement.exception;

public class UnauthorizedRideOperationException extends RuntimeException {
    public UnauthorizedRideOperationException(String message) {
        super(message);
    }
}
