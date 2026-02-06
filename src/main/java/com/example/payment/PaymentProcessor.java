package com.example.payment;

import java.math.BigDecimal;

public class PaymentProcessor {

    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;


    public PaymentProcessor(PaymentGateway paymentGateway, PaymentRepository paymentRepository, EmailService emailService) {
        this.paymentGateway = paymentGateway;
        this.paymentRepository = paymentRepository;
        this.emailService = emailService;
    }

    public boolean processPayment(double amount) {
        PaymentApiResponse response = paymentGateway.processPayment(BigDecimal.valueOf(amount));

        if (response == null) {
            return false;
        }

        if (response.isSuccess()) {
            paymentRepository.savePayment(BigDecimal.valueOf(amount), "SUCCESS");
            emailService.sendConfirmation("user@example.com", BigDecimal.valueOf(amount));
        }

        return response.isSuccess();
    }
}


