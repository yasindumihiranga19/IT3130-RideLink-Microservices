package com.ridelink.ridemanagement.service;

import com.ridelink.ridemanagement.dto.DriverAssignmentDto;
import com.ridelink.ridemanagement.dto.RideRequestDto;
import com.ridelink.ridemanagement.dto.RideResponseDto;
import com.ridelink.ridemanagement.dto.StatusUpdateDto;

import java.util.List;

public interface RideService {
    RideResponseDto createRideRequest(RideRequestDto requestDto);
    RideResponseDto assignDriver(Long rideId, DriverAssignmentDto assignmentDto);
    RideResponseDto updateRideStatus(Long rideId, StatusUpdateDto statusUpdateDto);
    RideResponseDto getRideById(Long rideId);
    List<RideResponseDto> getRidesByPassengerId(Long passengerId);
    List<RideResponseDto> getRidesByDriverId(Long driverId);
    void cancelRide(Long rideId);
}
