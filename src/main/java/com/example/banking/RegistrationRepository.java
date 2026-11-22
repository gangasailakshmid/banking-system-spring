package com.example.banking;

import com.example.banking.personal.*;
import com.example.banking.bank.*;
import com.example.banking.address.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

/** Coordinates separate local transactions; compensates caught failures, without claiming distributed atomicity. */
@Service
public class RegistrationRepository {
    private final PersonalDetailsRepository people;
    private final BankDetailsRepository banks;
    private final AddressDetailsRepository addresses;
    private final BankActivationService activation;
    public RegistrationRepository(PersonalDetailsRepository people, BankDetailsRepository banks, AddressDetailsRepository addresses,
                                  BankActivationService activation) {
        this.people=people; this.banks=banks; this.addresses=addresses; this.activation=activation;
    }
    public synchronized long count() { return people.count(); }
    public synchronized List<Registration> findAll() {
        return people.findAll(Sort.by(Sort.Direction.DESC,"id")).stream().map(this::join).toList();
    }
    public synchronized Optional<Registration> findById(Long id) { return people.findById(id).map(this::join); }
    private Registration join(PersonalDetails p) {
        var stored = load(p);
        var r = new Registration();
        r.setId(p.id);
        r.setFirstName(p.firstName);
        r.setMiddleName(p.middleName);
        r.setLastName(p.lastName);
        r.setGender(p.gender);
        var a = stored.address();
        if (a != null) {
            r.setAddress(a.address);
            r.setCity(a.city);
            r.setState(a.state);
            r.setCountry(a.country);
            r.setPhone(a.phone);
        }
        var b = stored.bank();
        if (b != null) {
            r.setBankStatus(b.status);
            r.setBankName(b.bankName);
            r.setAccountNumber(b.accountNumber);
            r.setSsn(b.ssn);
        }
        return r;
    }

    private StoredRegistration load(PersonalDetails person) {
        return new StoredRegistration(person, addresses.findById(person.id).orElse(null),
            banks.findById(person.id).orElse(null));
    }

    private StoredRegistration snapshot(Long id) {
        return people.findById(id).map(this::load).orElseThrow();
    }

    private record StoredRegistration(PersonalDetails person, AddressDetails address, BankDetails bank) {}

    public synchronized Registration saveAndFlush(Registration r) {
        var before=r.getId()==null ? null : snapshot(r.getId());
        try {
            r.setId(people.saveAndFlush(personal(r)).id);
            addresses.save(address(r));
            activation.savePending(bank(r));
            r.setBankStatus(ActivationStatus.IN_PROGRESS);
            return r;
        } catch (RuntimeException failure) {
            repair(r.getId(),before,failure,false);
            if(before==null) r.setId(null);
            throw failure;
        }
    }
    public synchronized void delete(Registration r) {
        var before=snapshot(r.getId());
        try {
            addresses.deleteById(r.getId()); banks.deleteById(r.getId()); people.deleteById(r.getId());
        } catch(RuntimeException failure) { repair(r.getId(),before,failure,true); throw failure; }
    }
    private void repair(Long id, StoredRegistration before, RuntimeException failure, boolean restoreBank) {
        if(id==null) return;
        List<Runnable> repairs=before==null ? List.of(
            () -> addresses.deleteById(id), () -> people.deleteById(id)) : List.of(
            () -> people.saveAndFlush(before.person()),
            () -> {
                if (before.address() == null) addresses.deleteById(id);
                else addresses.save(before.address());
            },
            () -> {
                if (!restoreBank) return;
                if (before.bank() == null) banks.deleteById(id);
                else banks.saveAndFlush(before.bank());
            });
        for(var repair:repairs) {try {repair.run();} catch(RuntimeException e) {failure.addSuppressed(e);}}
    }
    private PersonalDetails personal(Registration r) {
        var e=new PersonalDetails(); e.id=r.getId();
        e.firstName=r.getFirstName();
        e.middleName=r.getMiddleName();
        e.lastName=r.getLastName();
        e.gender=r.getGender();
        return e;
    }
    private AddressDetails address(Registration r) {
        var e=new AddressDetails(); e.id=r.getId();
        e.address=r.getAddress();
        e.city=r.getCity();
        e.state=r.getState();
        e.country=r.getCountry();
        e.phone=r.getPhone();
        return e;
    }
    private BankDetails bank(Registration r) {
        var e=new BankDetails(); e.id=r.getId();
        e.bankName=r.getBankName();
        e.accountNumber=r.getAccountNumber();
        e.ssn=r.getSsn();
        return e;
    }
}
