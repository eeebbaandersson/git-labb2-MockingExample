package com.example.payment;

public class PaymentProcessor {

    // Betalning (bytt från PaymentApi)--> PaymentGateway
    // Lagring (bytt från DatabaseConnection) --> PaymentRepository
    // Kommunikation (bytt från EmailService)--> EmailService

    // REFACTORERINGSBESLUT - Efter bytte till Dependency Injection

    // Koden var från början hårdkodad och anropade direkt specifika klasser ( ex. PaymentApi.charge()/ DatabaseConnection.getInstance())
    // vilket gjorde den begränsad och svår att modifera i ett framtida skedde.
    // Nu när processorn istället tar emot sina tjänster via konstruktorn kan vi med lätthet bytta ut dessa i ett framtida skedde utan att behöva ändra processorns logik.

    // Processorn var tidigare tvungen att veta om exakta tekniska detaljer så som SQL-syntax och API-nyckel, men nu när den
    // bara pratar med interfacen är fokuset endast på flödet/affärslogiken. Vi har nu separerat vad som ska göras från hur det utförs.

    // Tidigare hade vi behövt en aktiv databas och riktig internetuppkoppling vilket gjorde vår kod svår att testa.
    // Nu kan vi istället injicera "Mock-objekt" för att simulera olika scenarion i en isolerad miljö.


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
            emailService.sendConfirmation("user@example.com", amount);
        }

        // Skickar e-post direkt

        return response.isSuccess();

    }
}


