package com.ridelink.farepayment.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareResponseDTO {
    private BigDecimal estimatedFare;
    private BigDecimal finalFare;
}
