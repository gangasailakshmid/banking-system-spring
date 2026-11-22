package com.example.banking.bank;
import jakarta.persistence.*;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;
@Entity
@Table(name="bank_details")
public class BankDetails {
    @Id public Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @ColumnDefault("'INACTIVE'")
    public ActivationStatus status = ActivationStatus.INACTIVE;

    public UUID activationRequestId;

    @Column(nullable=false,length=100)
    public String bankName;
    @Column(nullable=false,length=34)
    public String accountNumber;
    @Column(nullable=false,length=9)
    public String ssn;
}
