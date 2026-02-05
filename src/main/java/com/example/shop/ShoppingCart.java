package com.example.shop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingCart {
    private final Map<Item, Integer> items = new HashMap<>();

    public void addItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        items.put(item, items.getOrDefault(item, 0) + 1);
    }

    public List<Item> getItems() {
        return new ArrayList<>(items.keySet());
    }

    public void removeItem(Item item) {
        Integer currentQuantity = items.get(item);

        if (currentQuantity == null) {
            throw new IllegalArgumentException("Cannot remove item: Item not found in shopping cart");
        }

        if (currentQuantity > 1) {
            // Minska kvantiteten med 1
            items.put(item, currentQuantity - 1);
        } else {
            // Fanns endast 1 vara, ta bort hela raden
            items.remove(item);
        }
    }

    public BigDecimal calculateTotalPrice() {
        var totalPrice = BigDecimal.ZERO;

        for (var entry : items.entrySet()) {
            // Hämtar priset från nykeln
            BigDecimal itemPrice = entry.getKey().price();

            // Hämtar antalet
            BigDecimal quantity = BigDecimal.valueOf(entry.getValue());

            // Räknar ut radsumman (pris * antal)
            BigDecimal lineTotal = itemPrice.multiply(quantity);

            // Addera till totalpriset (total = total + radsumma)
            totalPrice = totalPrice.add(lineTotal);
        }
        return totalPrice;
    }

    public BigDecimal applyDiscount(BigDecimal discountPercentage) {
        if (discountPercentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount cannot be negative");
        }

        if (discountPercentage.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Discount cannot be over 100%");
        }

        var totalPrice = calculateTotalPrice();
        return totalPrice.multiply(BigDecimal.ONE.subtract(discountPercentage));
    }

    public Integer getItemQuantity(Item item) {
        return items.getOrDefault(item, 0);
    }
}
