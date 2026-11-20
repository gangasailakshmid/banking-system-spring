package com.example.banking.address;
import jakarta.persistence.*;
@org.springframework.data.mongodb.core.mapping.Document("address_details")
public class AddressDetails {
    @org.springframework.data.annotation.Id public Long id;
    public String address;
    public String city;
    public String state;
    public String country;
    public String phone;
}
