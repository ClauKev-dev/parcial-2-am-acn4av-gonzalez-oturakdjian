package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import java.util.Locale;

public class Product {
    private String imageUrl;
    private String name;
    private double price;
    private int quantity;
    private String id;

    public Product() {
        this.quantity = 1;
    }

    public Product(String imageUrl, String name, double price) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.quantity = 1;
    }

    public Product(int imageResId, String name, double price) {
        this.name = name;
        this.price = price;
        this.quantity = 1;
        this.imageUrl = null;
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public void increaseQuantity() { this.quantity++; }

    public String getPriceFormatted() {
        return String.format(Locale.getDefault(), "$%.2f", price * quantity);
    }
}
