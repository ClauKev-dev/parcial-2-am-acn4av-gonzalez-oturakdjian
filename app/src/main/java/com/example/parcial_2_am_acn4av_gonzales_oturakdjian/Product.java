package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import java.util.Locale;

public class Product {
    private int imageResId;
    private String name;
    private double price;
    private int quantity;

    public Product(int imageResId, String name, double price) {
        this.imageResId = imageResId;
        this.name = name;
        this.price = price;
        this.quantity = 1;
    }

    public int getImageResId() { return imageResId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void increaseQuantity() { this.quantity++; }

    public String getPriceFormatted() {
        return String.format(Locale.getDefault(), "$%.2f", price * quantity);
    }
}
