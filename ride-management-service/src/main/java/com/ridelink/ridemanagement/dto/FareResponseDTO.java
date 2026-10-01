package com.ridelink.ridemanagement.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FareResponseDTO {
    private BigDecimal estimatedFare;
    private BigDecimal finalFare;
}
