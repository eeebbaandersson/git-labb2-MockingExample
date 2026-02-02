package com.example;

import com.example.shop.Item;
import com.example.shop.ShoppingCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class ShoppingCartTest {

    // Write test - test fails - write code - test passes - refactor

    // --Flöde--
    // Skriv test
    // Rött:Kompileringsfel (då produktionskod saknas), räknas som ett misslyckat test!
    // Grönt: Skapa enklast möjliga kod för att testet ska bli godkänt
    // Refactor: Nu är det godkänt att "städa" kod om behovet finns

    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart =  new ShoppingCart();
    }

    // --Tester--

    // Lägg till varor
    @Test
    void addItem_shouldIncreaseSizeOfShoppingCart() {
        // Arrange
        Item milk = new Item("Milk", new BigDecimal("16.0"));
        Item coffee = new Item("Coffee", new BigDecimal("93.0"));

        // Act
        cart.addItem(milk);
        cart.addItem(coffee);

        // Assert
        assertThat(cart.getItems()).hasSize(2).containsExactly(milk, coffee);
    }

    // Ta bort varor
    @Test
    void removeItem_shouldDecreaseSizeOfShoppingCart() {
        // Arrange
        Item milk = new Item("Milk", new BigDecimal("16.0"));
        Item coffee = new Item("Coffee", new BigDecimal("93.0"));
        cart.addItem(milk);
        cart.addItem(coffee);

        // Act
        cart.removeItem(milk);

        // Assert
        assertThat(cart.getItems()).hasSize(1).containsExactly(coffee);
    }

    // Beräkna totalpris
    @Test
    void calculateTotalPrice_shouldReturnSumOfAllItems() {
        // Arrange
        Item milk = new Item("Milk", new BigDecimal("16.0"));
        Item coffee = new Item("Coffee", new BigDecimal("93.0"));
        cart.addItem(milk);
        cart.addItem(coffee);

        // Act
        BigDecimal total = cart.calculateTotalPrice();

        // Assert
        assertThat(total).isEqualByComparingTo(new BigDecimal("109"));
    }

    // Applicera rabatter
    @Test
    void applyDiscount_shouldReduceTotalPriceByPercentage() {
        // Arrange
        Item milk = new Item("Milk", new BigDecimal("16.0"));
        Item coffee = new Item("Coffee", new BigDecimal("93.0"));
        cart.addItem(milk);
        cart.addItem(coffee);

        // Act
        BigDecimal discountedPrice = cart.applyDiscount(new BigDecimal("0.10")); // 10% rabatt

        // Assert
        assertThat(discountedPrice).isEqualByComparingTo(new BigDecimal("98.1"));
    }


    // Hantera kvantitetsuppdateringar
    @Test
    void updateQuantity_addingSameItemTwice_shouldUpdateQuantity() {
        // Arrange
        Item milk = new Item("Milk", new BigDecimal("16.0"));

        // Act
        cart.addItem(milk);
        cart.addItem(milk);

        // Assert
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItemQuantity(milk)).isEqualTo(2);
    }

    // Hantera EdgeCase-tester?
}
