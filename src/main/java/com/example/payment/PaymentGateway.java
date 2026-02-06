package com.example.payment;

import java.math.BigDecimal;

public interface PaymentGateway {
    PaymentApiResponse processPayment(BigDecimal amount);

}
