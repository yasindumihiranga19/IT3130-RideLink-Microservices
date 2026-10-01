package com.ridelink.ridemanagement.exception;

public class InvalidRideStatusException extends RuntimeException {
    public InvalidRideStatusException(String message) {
        super(message);
    }
}
