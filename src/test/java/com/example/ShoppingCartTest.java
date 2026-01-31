package com.example;

import com.example.shop.Item;
import com.example.shop.ShoppingCart;
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

    // Använda record för Item --> slippa alla setter/getters?

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
    // Applicera rabatter
    // Hantera kvantitetsuppdateringar
}
