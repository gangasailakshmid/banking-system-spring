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
    public RegistrationRepository(PersonalDetailsRepository people, BankDetailsRepository banks, AddressDetailsRepository addresses) {
        this.people=people; this.banks=banks; this.addresses=addresses;
    }
    public synchronized long count() { return people.count(); }
    public synchronized List<Registration> findAll() {
        return people.findAll(Sort.by(Sort.Direction.DESC,"id")).stream().map(p -> join(p)).toList();
    }
    public synchronized Optional<Registration> findById(Long id) { return people.findById(id).map(this::join); }
    private Registration join(PersonalDetails p) {
        var a=addresses.findById(p.id).orElseThrow(() -> new IllegalStateException("Missing address for registration "+p.id));
        var b=banks.findById(p.id).orElseThrow(() -> new IllegalStateException("Missing bank details for registration "+p.id));
        var r=new Registration(); r.setId(p.id);
        r.setFirstName(p.firstName);
        r.setMiddleName(p.middleName);
        r.setLastName(p.lastName);
        r.setGender(p.gender);
        r.setAddress(a.address);
        r.setCity(a.city);
        r.setState(a.state);
        r.setCountry(a.country);
        r.setPhone(a.phone);
        r.setBankName(b.bankName);
        r.setAccountNumber(b.accountNumber);
        r.setSsn(b.ssn);
        return r;
    }
    public synchronized Registration saveAndFlush(Registration r) {
        var before=r.getId()==null ? null : findById(r.getId()).orElseThrow();
        try {
            r.setId(people.saveAndFlush(personal(r)).id);
            addresses.save(address(r)); banks.saveAndFlush(bank(r)); return r;
        } catch (RuntimeException failure) {
            repair(r.getId(),before,failure);
            if(before==null) r.setId(null);
            throw failure;
        }
    }
    public synchronized void delete(Registration r) {
        var before=findById(r.getId()).orElseThrow();
        try {
            addresses.deleteById(r.getId()); banks.deleteById(r.getId()); people.deleteById(r.getId());
        } catch(RuntimeException failure) { repair(r.getId(),before,failure); throw failure; }
    }
    private void repair(Long id, Registration before, RuntimeException failure) {
        if(id==null) return;
        List<Runnable> repairs=before==null ? List.of(
            () -> addresses.deleteById(id), () -> banks.deleteById(id), () -> people.deleteById(id)) : List.of(
            () -> people.saveAndFlush(personal(before)), () -> addresses.save(address(before)), () -> banks.saveAndFlush(bank(before)));
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
