package com.example.banking.bank;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "bank_activation_outbox")
class ActivationOutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long customerId;

    @Column(nullable = false)
    UUID requestId;

    // A pending request has no outcome yet.
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    ActivationStatus outcome;

    protected ActivationOutboxEvent() {}

    ActivationOutboxEvent(Long customerId, UUID requestId, ActivationStatus outcome) {
        this.customerId = customerId;
        this.requestId = requestId;
        this.outcome = outcome;
    }
}
