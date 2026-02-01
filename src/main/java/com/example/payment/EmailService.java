package com.example.payment;

public interface EmailService {
    void sendConfirmation(String email, double amount);
}
