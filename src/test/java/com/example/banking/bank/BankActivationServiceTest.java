package com.example.banking.bank;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankActivationServiceTest {
    private final BankDetailsRepository banks = mock(BankDetailsRepository.class);
    private final ActivationOutboxRepository outbox = mock(ActivationOutboxRepository.class);
    private final BankActivationService service = new BankActivationService(banks, outbox);

    @Test
    void saveQueuesActivationRequest() {
        var bank = new BankDetails();
        bank.id = 5L;

        service.savePending(bank);

        assertEquals(ActivationStatus.IN_PROGRESS, bank.status);
        assertNotNull(bank.activationRequestId);
        verify(banks).save(bank);
        verify(outbox).save(argThat(event -> event.customerId.equals(bank.id)
            && event.requestId.equals(bank.activationRequestId) && event.outcome == null));
    }

    @ParameterizedTest
    @CsvSource({
        "' Bank of AMERICA ',989800123,ACTIVATED",
        "Chase,787800123,ACTIVATED",
        "US Bank,686800123,ACTIVATED",
        "Bank of America,787800123,FAILED",
        "Chase,686800123,FAILED",
        "US Bank,989800123,FAILED",
        "Other Bank,989800123,FAILED"
    })
    void activationRecordsOutcomeOnce(String name, String account, ActivationStatus expected) {
        var bank = new BankDetails();
        bank.id = 5L;
        bank.bankName = name;
        bank.accountNumber = account;
        bank.status = ActivationStatus.IN_PROGRESS;
        bank.activationRequestId = UUID.randomUUID();
        when(banks.findBankDetailsById(bank.id)).thenReturn(Optional.of(bank));

        service.activate(bank.id, UUID.randomUUID());
        assertEquals(ActivationStatus.IN_PROGRESS, bank.status);
        verifyNoInteractions(outbox);

        service.activate(bank.id, bank.activationRequestId);
        service.activate(bank.id, bank.activationRequestId);

        assertEquals(expected, bank.status);
        verify(outbox).save(argThat(event -> event.customerId.equals(bank.id)
            && event.requestId.equals(bank.activationRequestId) && event.outcome == expected));
    }
}
