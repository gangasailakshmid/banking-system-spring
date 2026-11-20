package com.example.banking.personal;
import jakarta.persistence.*;
@Entity
@Table(name="login_accounts")
public class LoginAccount {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false,unique=true,length=100) public String username;
    @Column(nullable=false,length=100) public String passwordHash;
}
