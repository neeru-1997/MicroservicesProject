package com.majka.customer;

import org.springframework.stereotype.Service;

@Service
public record CustomerService(CustomerRepository customerRepository) {

    public void registerCustomer(CustomerRegistrationRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();

        //check if email valid

        //check if email not taken

        // save customer to db
        customerRepository.save(customer);
    }

}
