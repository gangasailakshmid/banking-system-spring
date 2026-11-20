package com.example.banking;

import com.example.banking.personal.*;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements ApplicationRunner {
    private final LoginAccountRepository accounts;
    private final PasswordEncoder encoder;
    private final MongoTemplate mongo;
    private final String username;
    private final String password;
    public DatabaseInitializer(LoginAccountRepository accounts, PasswordEncoder encoder, MongoTemplate mongo,
        @Value("${banking.admin.username}") String username,@Value("${banking.admin.password}") String password) {
        this.accounts=accounts; this.encoder=encoder; this.mongo=mongo;
        this.username=username; this.password=password;
    }
    public void run(ApplicationArguments args) {
        mongo.executeCommand(new Document("ping",1));
        if(accounts.findByUsername(username).isEmpty()) {
            if(username.isBlank() || password.length()<12) throw new IllegalStateException("Set ADMIN_USERNAME and an ADMIN_PASSWORD of at least 12 characters before first startup.");
            var account=new LoginAccount(); account.username=username; account.passwordHash=encoder.encode(password);
            accounts.saveAndFlush(account);
        }
    }
}
