package com.example.payment;

import java.math.BigDecimal;

public interface PaymentRepository {
    void savePayment(BigDecimal amount, String message);

}
