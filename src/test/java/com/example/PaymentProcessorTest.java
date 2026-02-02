package com.example;

import com.example.payment.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentProcessorTest {

    // Tjänsterna/delarna PaymentProcessor behöver för att kunna köra
    @Mock
    private EmailService emailService;
    @Mock
    private PaymentApiResponse paymentApiResponse;
    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private PaymentRepository paymentRepository;

    // SUT
    @InjectMocks
    private PaymentProcessor paymentProcessor;

    @Test
    void processPayment_shouldSavePaymentAndSendEmail_WhenPaymentIsSuccessful() {
        // Arrange
        when(paymentApiResponse.isSuccess()).thenReturn(true);
        when(paymentGateway.processPayment(100.0)).thenReturn(paymentApiResponse);

        // Act
        boolean result = paymentProcessor.processPayment(100.0);

        // Assert + verify
        assertThat(result).isTrue();

        verify(paymentRepository).savePayment(100.0, "SUCCESS");
        verify(emailService).sendConfirmation("user@example.com", 100.0);

    }

    @Test
    void processPayment_shouldNotSavePaymentOrSendEmail_WhenPaymentIsUnsuccessful() {
        // Arrange
        when(paymentApiResponse.isSuccess()).thenReturn(false);
        when(paymentGateway.processPayment(100.0)).thenReturn(paymentApiResponse);

        // Act
        boolean result = paymentProcessor.processPayment(100.0);

        // Assert + verify
        assertThat(result).isFalse();

        verify(paymentGateway).processPayment(100.0);
        verifyNoInteractions( paymentRepository,emailService);


    }




}