package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.List;

public class Order {
    private String id;
    private String userId;
    private List<Product> products;
    private double total;
    private String status; // "en_curso", "completado", "cancelado"
    private String cardNumber; // Últimos 4 dígitos
    private String cardHolder;
    @ServerTimestamp
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructor vacío necesario para Firestore
    public Order() {
    }

    public Order(String userId, List<Product> products, double total, String cardNumber, String cardHolder) {
        this.userId = userId;
        this.products = products;
        this.total = total;
        this.status = "en_curso";
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}

