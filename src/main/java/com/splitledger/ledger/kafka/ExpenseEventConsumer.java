package com.splitledger.ledger.kafka;

import com.splitledger.ledger.event.ExpenseCreatedEvent;
import com.splitledger.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ExpenseEventConsumer {

    private final LedgerService ledgerService;

    @KafkaListener(
            topics = "expense.created",
            groupId = "ledger-service-group"
    )
    public void onExpenseCreated(ExpenseCreatedEvent event) {
        log.info("Received expense.created event for group: {}",
                event.getGroupId());
        try {
            ledgerService.processExpenseCreated(event);
            log.info("Successfully processed expense: {}",
                    event.getExpenseId());
        } catch (Exception e) {
            log.error("Failed to process expense event: {}",
                    e.getMessage());
        }
    }
}
