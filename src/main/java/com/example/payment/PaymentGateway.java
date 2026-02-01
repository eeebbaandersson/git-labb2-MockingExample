package com.example.payment;

public interface PaymentGateway {
     PaymentApiResponse processPayment(double amount);

}
