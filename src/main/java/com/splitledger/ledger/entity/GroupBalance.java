package com.splitledger.ledger.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_balances",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"group_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBalance extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    // positive = owed money, negative = owes money
    @Column(name = "net_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal netBalance;

}
