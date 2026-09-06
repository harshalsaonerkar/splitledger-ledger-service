package com.splitledger.ledger.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class BalanceResponse {

    private UUID userId;
    private String userEmail;
    private BigDecimal netBalance;
    private String status;
}
