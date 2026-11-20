package com.example.banking;

import com.example.banking.personal.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;

@WebMvcTest(controllers={RegistrationController.class,LoginController.class})
@Import(SecurityConfiguration.class)
class SecurityTest {
    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;
    @MockitoBean RegistrationRepository registrations;
    @MockitoBean LoginAccountRepository accounts;

    @Test void anonymousUsersMustLogIn() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/users/1/edit")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
    }
    @Test void validatesPasswordAgainstStoredHash() throws Exception {
        var a=new LoginAccount(); a.username="admin"; a.passwordHash=encoder.encode("TestPassword123!");
        when(accounts.findByUsername("admin")).thenReturn(Optional.of(a));
        mvc.perform(post("/login").with(csrf()).param("username","admin").param("password","wrong"))
            .andExpect(redirectedUrl("/login?error")).andExpect(unauthenticated());
        mvc.perform(post("/login").with(csrf()).param("username","admin").param("password","TestPassword123!"))
            .andExpect(redirectedUrl("/")).andExpect(authenticated().withUsername("admin"));
    }
    @Test void mutationsRequireCsrf() throws Exception {
        mvc.perform(post("/users/save").with(user("admin"))).andExpect(status().isForbidden());
        mvc.perform(post("/users/1/delete").with(user("admin"))).andExpect(status().isForbidden());
        verifyNoInteractions(registrations);
    }
    @Test void validAuthenticatedSaveReachesService() throws Exception {
        mvc.perform(post("/users/save").with(user("admin")).with(csrf())
            .param("firstName","Test").param("lastName","User").param("gender","Other")
            .param("address","Street").param("city","City").param("state","CA").param("country","US")
            .param("phone","5550100").param("bankName","Bank").param("accountNumber","00123").param("ssn","000-00-0001"))
            .andExpect(redirectedUrl("/"));
        verify(registrations).saveAndFlush(argThat(r -> "000000001".equals(r.getSsn())));
    }
    @Test void logoutRequiresCsrfAndClearsAuthentication() throws Exception {
        mvc.perform(post("/logout").with(user("admin"))).andExpect(status().isForbidden());
        mvc.perform(post("/logout").with(user("admin")).with(csrf()))
            .andExpect(redirectedUrl("/login?logout")).andExpect(unauthenticated());
    }
}
