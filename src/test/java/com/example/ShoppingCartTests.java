package com.example;

import com.example.shop.Item;
import com.example.shop.ShoppingCart;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class ShoppingCartTests {

    // Write test - test fails - write code - test passes - refactor

    // --Flöde--
    // Skriv test
    // Rött:Kompileringsfel (då produktionskod saknas), räknas som ett misslyckat test!
    // Grönt: Skapa enklast möjliga kod för att testet ska bli godkänt
    // Refactor: Nu är det godkänt att "städa" kod om behovet finns


    // Implementera en @BeforeEach här med shoppingCarten som återanvänds i de flesta testerna?

    // --Tester--
    // Lägg till varor
    @Test
    void addItems_shouldBeStoredInShoppingCart() {
        // Arrange
        ShoppingCart cart = new ShoppingCart();
        Item milk = new Item("Milk", new BigDecimal("16.0"));
        Item coffee = new Item("Coffee", new BigDecimal("93.0"));

        // Act
        cart.addItem(milk);
        cart.addItem(coffee);

        // Assert - Förväntar oss att shoppingCart ska innehålla 2 varor
        assertThat(cart.getItems()).hasSize(2).containsExactly(milk, coffee);
    }

    // Ta bort varor
    @Test
    void removeItems_shouldBeRemovedFromShoppingCart() {
        // Arrange
        ShoppingCart cart = new ShoppingCart();
        Item milk = new Item("Milk", new BigDecimal("16.0"));
        Item coffee = new Item("Coffee", new BigDecimal("93.0"));
        cart.addItem(milk);
        cart.addItem(coffee);

        // Act
        cart.removeItem(milk);

        // Assert - Förvänta oss att shoppingCart nu endast ska innehålla coffee
        assertThat(cart.getItems()).hasSize(1).containsExactly(coffee);
    }

    // Beräkna totalpris
    @Test
    void calculateTotalPriceForItemsInShoppingCart() {
        // Arrange
        ShoppingCart cart = new ShoppingCart();
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
    // Hantera kvantitetsuppdateringar

    // Hantera EdgeCase-tester
}
