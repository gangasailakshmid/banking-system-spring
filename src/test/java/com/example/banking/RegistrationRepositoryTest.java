package com.example.banking;

import com.example.banking.personal.*;
import com.example.banking.bank.*;
import com.example.banking.address.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationRepositoryTest {
    @Test void routesFieldsToSeparateStoresAndCompensatesFailedCreate() {
        var people=mock(PersonalDetailsRepository.class); var banks=mock(BankDetailsRepository.class);
        var addresses=mock(AddressDetailsRepository.class);
        when(people.saveAndFlush(any())).thenAnswer(i -> {PersonalDetails p=i.getArgument(0);p.id=42L;return p;});
        when(banks.saveAndFlush(any())).thenThrow(new IllegalStateException("bank unavailable"));
        var service=new RegistrationRepository(people,banks,addresses);
        var r=new Registration(); r.setFirstName("Test");r.setCity("City");r.setAccountNumber("00123");
        assertThrows(IllegalStateException.class,()->service.saveAndFlush(r));
        verify(people).saveAndFlush(argThat(p -> "Test".equals(p.firstName)));
        verify(addresses).save(argThat(a -> a.id==42L && "City".equals(a.city)));
        verify(banks).saveAndFlush(argThat(b -> b.id==42L && "00123".equals(b.accountNumber)));
        verify(people).deleteById(42L);verify(addresses).deleteById(42L);verify(banks).deleteById(42L);
        assertNull(r.getId());
    }
    @Test void joinsThreeStoresAndRejectsIncompleteRecords() {
        var people=mock(PersonalDetailsRepository.class);var banks=mock(BankDetailsRepository.class);
        var addresses=mock(AddressDetailsRepository.class);
        var p=new PersonalDetails();p.id=7L;p.firstName="Test";
        var a=new AddressDetails();a.id=7L;a.city="City";
        var b=new BankDetails();b.id=7L;b.accountNumber="00123";
        when(people.findById(7L)).thenReturn(Optional.of(p));when(addresses.findById(7L)).thenReturn(Optional.of(a));when(banks.findById(7L)).thenReturn(Optional.of(b));
        var service=new RegistrationRepository(people,banks,addresses);
        var joined=service.findById(7L).orElseThrow();
        assertEquals("Test",joined.getFirstName());assertEquals("City",joined.getCity());assertEquals("00123",joined.getAccountNumber());
        when(addresses.findById(7L)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class,()->service.findById(7L));
    }
}
