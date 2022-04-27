package com.majka.customer;

import com.majka.amqp.RabbitMQMessageProducer;
import com.majka.clients.fraud.FraudCheckResponse;
import com.majka.clients.fraud.FraudClient;
import com.majka.clients.notification.NotificationRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
//    private final RestTemplate restTemplate;
    private final FraudClient fraudClient;
//    private final NotificationClient notificationClient;
    private final RabbitMQMessageProducer rabbitMQMessageProducer;

    public void registerCustomer(CustomerRegistrationRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();

        //check if email valid

        //check if email not taken
        customerRepository.saveAndFlush(customer);

        //check if fraudster
//        FraudCheckResponse fraudCheckResponse = restTemplate.getForObject(
//                "http://FRAUD/api/v1/fraud-check/{customerId}",
//                FraudCheckResponse.class, customer.getId());

        FraudCheckResponse fraudCheckResponse = fraudClient.isFraudster(customer.getId());

        if(fraudCheckResponse.isFraudster()){
            throw new IllegalStateException("Fraudster");
        }

        // save customer to db
//        customerRepository.save(customer);

        //send notification
        NotificationRequest notificationRequest = new NotificationRequest(
                customer.getId(),
                customer.getEmail(),
                String.format("Hi %s, welcome to your account...",
                        customer.getFirstName()));

//        notificationClient.sendNotification(notificationRequest);

        // rabbitmq
        rabbitMQMessageProducer.publish(notificationRequest,
                "internal.exchange", "internal.notification.routing-key");
    }

}
