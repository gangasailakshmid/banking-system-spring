package com.example.banking;

import jakarta.servlet.http.HttpSession;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {
    private final RegistrationRepository registrations;

    public RegistrationController(RegistrationRepository registrations) {
        this.registrations = registrations;
    }

    @InitBinder("registration")
    public void bindRegistration(org.springframework.web.bind.WebDataBinder binder) {
        binder.setAllowedFields("firstName", "middleName", "lastName", "gender", "address",
            "city", "state", "country", "phone", "bankName", "accountNumber", "ssn");
    }

    private Registration find(Long id) {
        return registrations.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found."));
    }

    private String page(Registration form, Long editId, Model model, HttpSession session) {
        model.addAttribute("registration", form);
        model.addAttribute("editId", editId);
        model.addAttribute("users", registrations.findAll());
        return "home";
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        return page(new Registration(), null, model, session);
    }

    @GetMapping("/users/{id}/edit")
    public String edit(@PathVariable Long id, Model model, HttpSession session) {
        return page(find(id), id, model, session);
    }

    @PostMapping("/users/save")
    public String save(@ModelAttribute("registration") Registration form,
                       @RequestParam(required = false) Long editId,
                       Model model, HttpSession session,
                       RedirectAttributes redirect) {
        Registration existing = editId == null ? null : find(editId);
        if (existing != null) {
            if (form.getAccountNumber() == null || form.getAccountNumber().isBlank()) form.setAccountNumber(existing.getAccountNumber());
            if (form.getSsn() == null || form.getSsn().isBlank()) form.setSsn(existing.getSsn());
        }
        if (form.getMiddleName() == null) form.setMiddleName("");
        boolean valid = text(form.getFirstName(), 100) && text(form.getLastName(), 100)
            && form.getMiddleName().length() <= 100
            && form.getGender() != null && Set.of("Male", "Female", "Other").contains(form.getGender())
            && text(form.getAddress(), 500) && text(form.getCity(), 100)
            && text(form.getState(), 100) && text(form.getCountry(), 100)
            && text(form.getPhone(), 30) && text(form.getBankName(), 100)
            && form.getAccountNumber() != null && form.getAccountNumber().matches("[0-9]{1,34}")
            && form.getSsn() != null && form.getSsn().matches("(?:[0-9]{9}|[0-9]{3}-[0-9]{2}-[0-9]{4})");
        if (!valid) {
            model.addAttribute("error", "Check all required fields. Account numbers must contain 1–34 digits and SSNs must contain nine digits.");
            return page(form, editId, model, session);
        }
        Registration saved = existing == null ? new Registration() : existing;
        saved.setFirstName(form.getFirstName()); saved.setMiddleName(form.getMiddleName());
        saved.setLastName(form.getLastName()); saved.setGender(form.getGender());
        saved.setAddress(form.getAddress()); saved.setCity(form.getCity()); saved.setState(form.getState());
        saved.setCountry(form.getCountry()); saved.setPhone(form.getPhone()); saved.setBankName(form.getBankName());
        saved.setAccountNumber(form.getAccountNumber()); saved.setSsn(form.getSsn().replace("-", ""));
        registrations.saveAndFlush(saved);
        redirect.addFlashAttribute("message", existing == null ? "User registered successfully." : "User updated successfully.");
        return "redirect:/";
    }

    private boolean text(String value, int max) {
        return value != null && !value.isBlank() && value.length() <= max;
    }

    @PostMapping("/users/{id}/delete")
    public String delete(@PathVariable Long id,
                         HttpSession session, RedirectAttributes redirect) {
        registrations.delete(find(id));
        redirect.addFlashAttribute("message", "User deleted successfully.");
        return "redirect:/";
    }
}
