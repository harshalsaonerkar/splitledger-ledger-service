package com.splitledger.ledger.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SettlementResponse {
    private String fromEmail;
    private String toEmail;
    private BigDecimal amount;
}
