package com.example.payment;

public class PaymentProcessor {
    //    private static final String API_KEY = "sk_test_123456";

    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;


    public PaymentProcessor(PaymentGateway paymentGateway, PaymentRepository paymentRepository, EmailService emailService) {
        this.paymentGateway = paymentGateway;
        this.paymentRepository = paymentRepository;
        this.emailService = emailService;
    }

    public boolean processPayment(double amount) {
        PaymentApiResponse response = paymentGateway.processPayment(amount);

        if (response.isSuccess()) {
            paymentRepository.savePayment(amount, "SUCCESS");
            emailService.sendConfirmation("user@example.com", amount);
        }

        return response.isSuccess();
    }
}


