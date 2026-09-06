package com.splitledger.ledger.controller;

import com.splitledger.ledger.dto.BalanceResponse;
import com.splitledger.ledger.dto.SettlementResponse;
import com.splitledger.ledger.entity.LedgerEntry;
import com.splitledger.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/group/{groupId}/balances")
    public ResponseEntity<List<BalanceResponse>> getBalances(
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(ledgerService.getGroupBalances(groupId));
    }

    @GetMapping("/group/{groupId}/settlements")
    public ResponseEntity<List<SettlementResponse>> getSettlements(
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(
                ledgerService.getSimplifiedSettlements(groupId));
    }

    @GetMapping("/group/{groupId}/history")
    public ResponseEntity<List<LedgerEntry>> getHistory(
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(ledgerService.getGroupHistory(groupId));
    }
}