package com.example.payment;

public class PaymentProcessor {

    // Betalning (bytt från PaymentApi)--> PaymentGateway
    // Lagring (bytt från DatabaseConnection) --> PaymentRepository
    // Kommunikation (bytt från EmailService)--> EmailService

    // REFACTORERINGSBESLUT
    // Bytt ut statiska/hårdkodade anrop mot interface som injeceras via konstruktorn


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
        // Anropar extern betaltjänst direkt med statisk API-nyckel
        PaymentApiResponse response = paymentGateway.processPayment(amount);

        // Skriver till databas direkt
        if (response.isSuccess()) {
            paymentRepository.savePayment(amount, "SUCCESS");
        }

        // Skickar e-post direkt
        if (response.isSuccess()) {
            emailService.sendConfirmation("user@example.com", amount);
        }

        return response.isSuccess();
    }
}
