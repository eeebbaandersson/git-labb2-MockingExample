package com.example.payment;

import java.math.BigDecimal;

public interface EmailService {
    void sendConfirmation(String email, BigDecimal amount);
}
