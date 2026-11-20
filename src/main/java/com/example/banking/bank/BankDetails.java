package com.example.banking.bank;
import jakarta.persistence.*;
@Entity
@Table(name="bank_details")
public class BankDetails {
    @Id public Long id;
    @Column(nullable=false,length=100)
    public String bankName;
    @Column(nullable=false,length=34)
    public String accountNumber;
    @Column(nullable=false,length=9)
    public String ssn;
}
