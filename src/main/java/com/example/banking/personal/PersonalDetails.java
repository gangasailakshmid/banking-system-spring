package com.example.banking.personal;
import jakarta.persistence.*;
@Entity
@Table(name="personal_details")
public class PersonalDetails {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false,length=100)
    public String firstName;
    @Column(nullable=false,length=100)
    public String middleName;
    @Column(nullable=false,length=100)
    public String lastName;
    @Column(nullable=false,length=10)
    public String gender;
}
