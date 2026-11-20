package com.example.banking;

import com.example.banking.Registration;
import com.example.banking.RegistrationRepository;
import java.util.Set;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PersonController {
    private final RegistrationRepository registrations;

    public PersonController(RegistrationRepository registrations) {
        this.registrations = registrations;
    }

    private Registration draft(HttpSession session) {
        Registration draft = (Registration) session.getAttribute("registrationDraft");
        if (draft == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start registration from the personal details form.");
        }
        return draft;
    }

    @PostMapping("/register")
    public String register(@RequestParam String bankName,
                           @RequestParam String accountNumber,
                           @RequestParam String ssn, HttpSession session) {
        if (bankName.isBlank() || bankName.length() > 100
                || !accountNumber.matches("[0-9]{1,34}")
                || !ssn.matches("(?:[0-9]{9}|[0-9]{3}-[0-9]{2}-[0-9]{4})")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a bank name, numeric account number, and nine-digit SSN.");
        }
        Registration registration = draft(session);
        if (registration.getAddress() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Complete the address form first.");
        }
        registration.setBankName(bankName);
        registration.setAccountNumber(accountNumber);
        registration.setSsn(ssn.replace("-", ""));
        registrations.saveAndFlush(registration);
        session.removeAttribute("registrationDraft");
        return "registration-success";
    }

    @GetMapping("/register/new")
    public String form(HttpSession session) {
        session.removeAttribute("registrationDraft");
        return "person-form";
    }

    @PostMapping("/submit")
    public String submit(@RequestParam String firstName,
                         @RequestParam String lastName,
                         @RequestParam(defaultValue = "") String middleName,
                         @RequestParam String gender, HttpSession session) {
        if (firstName.isBlank() || lastName.isBlank()
                || firstName.length() > 100 || lastName.length() > 100
                || middleName.length() > 100
                || !Set.of("Male", "Female", "Other").contains(gender)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter valid names and gender.");
        }
        Registration registration = new Registration();
        registration.setFirstName(firstName);
        registration.setMiddleName(middleName);
        registration.setLastName(lastName);
        registration.setGender(gender);
        session.setAttribute("registrationDraft", registration);
        return "person-result";
    }

    @PostMapping("/submit-address")
    public String submitAddress(@RequestParam String address,
                                @RequestParam String city,
                                @RequestParam String state,
                                @RequestParam String country,
                                @RequestParam String phone, HttpSession session) {
        if (address.isBlank() || address.length() > 500
                || city.isBlank() || city.length() > 100
                || state.isBlank() || state.length() > 100
                || country.isBlank() || country.length() > 100
                || phone.isBlank() || phone.length() > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter valid address and contact details.");
        }
        Registration registration = draft(session);
        registration.setAddress(address);
        registration.setCity(city);
        registration.setState(state);
        registration.setCountry(country);
        registration.setPhone(phone);
        return "address-result";
    }
}
