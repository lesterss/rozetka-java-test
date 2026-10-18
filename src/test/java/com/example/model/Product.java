package com.example.model;

/**
 * Товар з каталогу: назва, ціна і чи є лейбл «ТОП ПРОДАЖІВ» (Top Sales).
 */
public record Product(String name, String price, boolean topSales) {

    @Override
    public String toString() {
        return name + " | " + price;
    }
}
