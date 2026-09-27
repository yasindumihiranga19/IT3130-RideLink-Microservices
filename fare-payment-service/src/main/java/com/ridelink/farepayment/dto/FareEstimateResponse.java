package com.ridelink.farepayment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FareEstimateResponse {
    private BigDecimal estimatedAmount;
    private String currency;
    private String message;
}
