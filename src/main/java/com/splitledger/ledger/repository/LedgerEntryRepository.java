package com.splitledger.ledger.repository;

import com.splitledger.ledger.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByGroupIdOrderByCreatedAtDesc(UUID groupId);
    List<LedgerEntry> findByGroupIdAndUserId(UUID groupId, UUID userId);
}
