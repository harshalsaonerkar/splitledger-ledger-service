package com.splitledger.ledger.service;

import com.splitledger.ledger.dto.BalanceResponse;
import com.splitledger.ledger.dto.SettlementResponse;
import com.splitledger.ledger.entity.GroupBalance;
import com.splitledger.ledger.entity.LedgerEntry;
import com.splitledger.ledger.enums.EntryType;
import com.splitledger.ledger.event.ExpenseCreatedEvent;
import com.splitledger.ledger.repository.GroupBalanceRepository;
import com.splitledger.ledger.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final GroupBalanceRepository groupBalanceRepository;
    private final DebtSimplificationService debtSimplificationService;

    @Transactional
    public void processExpenseCreated(ExpenseCreatedEvent event) {
        log.info("Processing expense.created event: {}", event.getExpenseId());

        for (ExpenseCreatedEvent.SplitDetail split : event.getSplits()) {

            boolean isPayer = split.getUserId().equals(event.getPaidBy());

            if (isPayer) {
                // Payer paid for everyone — credit them the amount others owe
                BigDecimal othersShare = event.getTotalAmount()
                        .subtract(split.getAmount());

                if (othersShare.compareTo(BigDecimal.ZERO) > 0) {
                    ledgerEntryRepository.save(LedgerEntry.builder()
                            .groupId(event.getGroupId())
                            .userId(split.getUserId())
                            .userEmail(split.getUserEmail())
                            .expenseId(event.getExpenseId())
                            .amount(othersShare)
                            .type(EntryType.CREDIT)
                            .description("Paid for: " + event.getDescription())
                            .build());

                    // Update net balance
                    updateBalance(event.getGroupId(), split.getUserId(),
                            split.getUserEmail(), othersShare);
                }
            } else {
                // Non-payer owes their share
                ledgerEntryRepository.save(LedgerEntry.builder()
                        .groupId(event.getGroupId())
                        .userId(split.getUserId())
                        .userEmail(split.getUserEmail())
                        .expenseId(event.getExpenseId())
                        .amount(split.getAmount())
                        .type(EntryType.DEBIT)
                        .description("Share of: " + event.getDescription())
                        .build());

                updateBalance(event.getGroupId(), split.getUserId(),
                        split.getUserEmail(), split.getAmount().negate());
            }
        }
    }

    private void updateBalance(UUID groupId, UUID userId,
                               String userEmail, BigDecimal delta) {
        GroupBalance balance = groupBalanceRepository
                .findByGroupIdAndUserId(groupId, userId)
                .orElse(GroupBalance.builder()
                        .groupId(groupId)
                        .userId(userId)
                        .userEmail(userEmail)
                        .netBalance(BigDecimal.ZERO)
                        .build());

        balance.setNetBalance(balance.getNetBalance().add(delta));
        groupBalanceRepository.save(balance);
    }

    public List<BalanceResponse> getGroupBalances(UUID groupId) {
        return groupBalanceRepository.findByGroupId(groupId)
                .stream()
                .map(b -> BalanceResponse.builder()
                        .userId(b.getUserId())
                        .userEmail(b.getUserEmail())
                        .netBalance(b.getNetBalance())
                        .status(resolveStatus(b.getNetBalance()))
                        .build())
                .collect(Collectors.toList());
    }

    public List<SettlementResponse> getSimplifiedSettlements(UUID groupId) {
        List<GroupBalance> balances = groupBalanceRepository
                .findByGroupId(groupId);
        return debtSimplificationService.simplify(balances);
    }

    public List<LedgerEntry> getGroupHistory(UUID groupId) {
        return ledgerEntryRepository
                .findByGroupIdOrderByCreatedAtDesc(groupId);
    }

    private String resolveStatus(BigDecimal balance) {
        int cmp = balance.compareTo(BigDecimal.ZERO);
        if (cmp > 0) return "OWED";
        if (cmp < 0) return "OWES";
        return "SETTLED";
    }
}
