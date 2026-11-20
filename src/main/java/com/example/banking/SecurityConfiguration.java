package com.example.banking;

import com.example.banking.personal.LoginAccountRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
public class SecurityConfiguration {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean UserDetailsService userDetailsService(LoginAccountRepository accounts) {
        return username -> accounts.findByUsername(username)
            .map(a -> User.withUsername(a.username).password(a.passwordHash).roles("ADMIN").build())
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.FORWARD, jakarta.servlet.DispatcherType.ERROR).permitAll()
                .requestMatchers("/login","/css/**","/js/**","/error").permitAll()
                .anyRequest().authenticated())
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/",true).failureUrl("/login?error").permitAll())
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true).clearAuthentication(true).deleteCookies("BANKING_SPRING_SESSION"))
            .exceptionHandling(errors -> errors.accessDeniedHandler((request,response,exception) -> {
                response.setStatus(403);
                request.getRequestDispatcher("/WEB-INF/jsp/access-denied.jsp").forward(request,response);
            }));
        // CSRF remains enabled, including login, registration, delete and POST logout.
        return http.build();
    }
}
