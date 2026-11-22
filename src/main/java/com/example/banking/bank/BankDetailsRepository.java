package com.example.banking.bank;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface BankDetailsRepository extends JpaRepository<BankDetails, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BankDetails> findBankDetailsById(Long id);
}
