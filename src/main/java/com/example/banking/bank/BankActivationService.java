package com.example.banking.bank;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankActivationService {
    private static final Map<String, String> ACCOUNT_PREFIXES = Map.of(
        "bank of america", "9898",
        "chase", "7878",
        "us bank", "6868"
    );

    private final BankDetailsRepository banks;
    private final ActivationOutboxRepository outbox;

    BankActivationService(BankDetailsRepository banks, ActivationOutboxRepository outbox) {
        this.banks = banks;
        this.outbox = outbox;
    }

    @Transactional("bankTransactionManager")
    public void savePending(BankDetails bank) {
        bank.status = ActivationStatus.IN_PROGRESS;
        bank.activationRequestId = UUID.randomUUID();
        banks.save(bank);
        outbox.save(new ActivationOutboxEvent(bank.id, bank.activationRequestId, null));
    }

    @Transactional("bankTransactionManager")
    public void activate(long customerId, UUID requestId) {
        var pendingBank = banks.findBankDetailsById(customerId)
            .filter(bank -> bank.status == ActivationStatus.IN_PROGRESS)
            .filter(bank -> requestId.equals(bank.activationRequestId));
        if (pendingBank.isEmpty()) {
            return;
        }

        var bank = pendingBank.get();
        var prefix = ACCOUNT_PREFIXES.get(bank.bankName.strip().toLowerCase(Locale.ROOT));
        bank.status = prefix != null && bank.accountNumber.startsWith(prefix)
            ? ActivationStatus.ACTIVATED : ActivationStatus.FAILED;
        outbox.save(new ActivationOutboxEvent(bank.id, requestId, bank.status));
    }
}
