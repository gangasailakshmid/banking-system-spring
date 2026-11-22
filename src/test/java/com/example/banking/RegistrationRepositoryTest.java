package com.example.banking;

import com.example.banking.personal.*;
import com.example.banking.bank.*;
import com.example.banking.address.*;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationRepositoryTest {
    @Test void routesFieldsToSeparateStoresAndCompensatesFailedCreate() {
        var people=mock(PersonalDetailsRepository.class); var banks=mock(BankDetailsRepository.class);
        var addresses=mock(AddressDetailsRepository.class);
        var activation = mock(BankActivationService.class);
        when(people.saveAndFlush(any())).thenAnswer(i -> {PersonalDetails p=i.getArgument(0);p.id=42L;return p;});
        doThrow(new IllegalStateException("bank unavailable")).when(activation).savePending(any());
        var service=new RegistrationRepository(people,banks,addresses,activation);
        var r=new Registration(); r.setFirstName("Test");r.setCity("City");r.setAccountNumber("00123");
        assertThrows(IllegalStateException.class,()->service.saveAndFlush(r));
        verify(people).saveAndFlush(argThat(p -> "Test".equals(p.firstName)));
        verify(addresses).save(argThat(a -> a.id==42L && "City".equals(a.city)));
        verify(activation).savePending(argThat(b -> b.id==42L && "00123".equals(b.accountNumber)));
        verify(people).deleteById(42L);verify(addresses).deleteById(42L);
        verifyNoInteractions(banks);
        assertNull(r.getId());
    }
    @Test void joinsThreeStoresAndKeepsIncompleteRecordsInTheDirectory() {
        var people=mock(PersonalDetailsRepository.class);var banks=mock(BankDetailsRepository.class);
        var addresses=mock(AddressDetailsRepository.class);
        var activation = mock(BankActivationService.class);
        var p=new PersonalDetails();p.id=7L;p.firstName="Test";
        var a=new AddressDetails();a.id=7L;a.city="City";
        var b=new BankDetails();b.id=7L;b.accountNumber="00123";
        when(people.findById(7L)).thenReturn(Optional.of(p));when(addresses.findById(7L)).thenReturn(Optional.of(a));when(banks.findById(7L)).thenReturn(Optional.of(b));
        var service=new RegistrationRepository(people,banks,addresses,activation);
        var joined=service.findById(7L).orElseThrow();
        assertEquals("Test",joined.getFirstName());assertEquals("City",joined.getCity());assertEquals("00123",joined.getAccountNumber());
        when(addresses.findById(7L)).thenReturn(Optional.empty());
        when(people.findAll(any(Sort.class))).thenReturn(List.of(p));
        var listed = service.findAll().getFirst();
        assertEquals(7L, listed.getId());
        assertNull(listed.getCity());
        assertEquals("00123", listed.getAccountNumber());

        when(banks.findById(7L)).thenReturn(Optional.empty());
        var editable = service.findById(7L).orElseThrow();
        assertEquals("Test", editable.getFirstName());
        assertNull(editable.getAccountNumber());

        editable.setCity("Recovered city");
        editable.setAccountNumber("00123");
        when(people.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        service.saveAndFlush(editable);
        assertEquals(ActivationStatus.IN_PROGRESS, editable.getBankStatus());
        verify(addresses).save(argThat(address -> "Recovered city".equals(address.city)));
        verify(activation).savePending(argThat(bank -> "00123".equals(bank.accountNumber)));
    }

    @Test void failedEditRestoresMissingAddress() {
        var people = mock(PersonalDetailsRepository.class);
        var banks = mock(BankDetailsRepository.class);
        var addresses = mock(AddressDetailsRepository.class);
        var activation = mock(BankActivationService.class);
        var person = new PersonalDetails();
        person.id = 5L;
        var bank = new BankDetails();
        bank.id = 5L;
        when(people.findById(5L)).thenReturn(Optional.of(person));
        when(banks.findById(5L)).thenReturn(Optional.of(bank));
        when(people.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new IllegalStateException("bank unavailable")).when(activation).savePending(any());
        var service = new RegistrationRepository(people, banks, addresses, activation);
        var registration = service.findById(5L).orElseThrow();
        registration.setCity("Recovered city");

        assertThrows(IllegalStateException.class, () -> service.saveAndFlush(registration));

        var addressWrites = inOrder(addresses);
        addressWrites.verify(addresses).save(argThat(address -> "Recovered city".equals(address.city)));
        addressWrites.verify(addresses).deleteById(5L);
        verify(people).saveAndFlush(person);
        verify(banks, never()).saveAndFlush(any());
        assertEquals(5L, registration.getId());
    }
}
