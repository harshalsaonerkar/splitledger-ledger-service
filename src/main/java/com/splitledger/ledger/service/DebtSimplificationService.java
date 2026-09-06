package com.splitledger.ledger.service;

import com.splitledger.ledger.dto.SettlementResponse;
import com.splitledger.ledger.entity.GroupBalance;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DebtSimplificationService {

    public List<SettlementResponse> simplify(List<GroupBalance> balances) {
        List<SettlementResponse> transactions = new ArrayList<>();

        // Separate into creditors (positive) and debtors (negative)
        List<GroupBalance> creditors = new ArrayList<>();
        List<GroupBalance> debtors = new ArrayList<>();

        for (GroupBalance b : balances) {
            if (b.getNetBalance().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(clone(b));
            } else if (b.getNetBalance().compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(clone(b));
            }
            // zero balance = already settled, skip
        }

        // Greedy matching — always match largest creditor with largest debtor
        int i = 0, j = 0;
        while (i < creditors.size() && j < debtors.size()) {
            GroupBalance creditor = creditors.get(i);
            GroupBalance debtor = debtors.get(j);

            BigDecimal debtorOwes = debtor.getNetBalance().abs();
            BigDecimal creditorIsOwed = creditor.getNetBalance();

            // Amount to settle is the minimum of what's owed and what's due
            BigDecimal settleAmount = creditorIsOwed.min(debtorOwes);

            transactions.add(SettlementResponse.builder()
                    .fromEmail(debtor.getUserEmail())
                    .toEmail(creditor.getUserEmail())
                    .amount(settleAmount)
                    .build());

            // Reduce balances
            creditor.setNetBalance(creditorIsOwed.subtract(settleAmount));
            debtor.setNetBalance(debtor.getNetBalance().add(settleAmount));

            // Move pointer if fully settled
            if (creditor.getNetBalance().compareTo(BigDecimal.ZERO) == 0) i++;
            if (debtor.getNetBalance().compareTo(BigDecimal.ZERO) == 0) j++;
        }

        return transactions;
    }

    private GroupBalance clone(GroupBalance b) {
        GroupBalance copy = new GroupBalance();
        copy.setUserId(b.getUserId());
        copy.setUserEmail(b.getUserEmail());
        copy.setNetBalance(b.getNetBalance());
        return copy;
    }
}
