package com.example.banking.bank;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface ActivationOutboxRepository extends JpaRepository<ActivationOutboxEvent, Long> {
    List<ActivationOutboxEvent> findTop20ByOrderByIdAsc();
}
