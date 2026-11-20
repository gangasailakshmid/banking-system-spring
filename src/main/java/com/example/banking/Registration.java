package com.example.banking;

import java.io.Serializable;


public class Registration implements Serializable {


    private Long id;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    private String firstName;
    public String getFirstName() { return firstName; }
    public void setFirstName(String value) { this.firstName = value; }

    private String middleName;
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String value) { this.middleName = value; }

    private String lastName;
    public String getLastName() { return lastName; }
    public void setLastName(String value) { this.lastName = value; }

    private String gender;
    public String getGender() { return gender; }
    public void setGender(String value) { this.gender = value; }

    private String address;
    public String getAddress() { return address; }
    public void setAddress(String value) { this.address = value; }

    private String city;
    public String getCity() { return city; }
    public void setCity(String value) { this.city = value; }

    private String state;
    public String getState() { return state; }
    public void setState(String value) { this.state = value; }

    private String country;
    public String getCountry() { return country; }
    public void setCountry(String value) { this.country = value; }

    private String phone;
    public String getPhone() { return phone; }
    public void setPhone(String value) { this.phone = value; }

    private String bankName;
    public String getBankName() { return bankName; }
    public void setBankName(String value) { this.bankName = value; }

    private String accountNumber;
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String value) { this.accountNumber = value; }

    private String ssn;
    public String getSsn() { return ssn; }
    public void setSsn(String value) { this.ssn = value; }
}
