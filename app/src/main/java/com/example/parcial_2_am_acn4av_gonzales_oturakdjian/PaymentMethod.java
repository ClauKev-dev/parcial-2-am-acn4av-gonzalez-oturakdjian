package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

public class PaymentMethod {
    public static final String TYPE_DEBIT = "debit";
    public static final String TYPE_CREDIT = "credit";
    public static final String TYPE_MERCADO_PAGO = "mercado_pago";
    
    private String id;
    private String userId;
    private String type;
    private String cardNumber;
    private String cardHolder;
    private String cardBrand;
    private String expiryDate;
    private String mercadoPagoEmail;
    private String mercadoPagoPhone;
    private boolean isDefault;
    private com.google.firebase.Timestamp createdAt;

    public PaymentMethod() {
    }


    public PaymentMethod(String userId, String type, String cardNumber, String cardHolder, String cardBrand, String expiryDate) {
        this.userId = userId;
        this.type = type;
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.cardBrand = cardBrand;
        this.expiryDate = expiryDate;
        this.isDefault = false;
    }


    public PaymentMethod(String userId, String email, String phone) {
        this.userId = userId;
        this.type = TYPE_MERCADO_PAGO;
        this.mercadoPagoEmail = email;
        this.mercadoPagoPhone = phone;
        this.isDefault = false;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getCardBrand() {
        return cardBrand;
    }

    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getMercadoPagoEmail() {
        return mercadoPagoEmail;
    }

    public void setMercadoPagoEmail(String mercadoPagoEmail) {
        this.mercadoPagoEmail = mercadoPagoEmail;
    }

    public String getMercadoPagoPhone() {
        return mercadoPagoPhone;
    }

    public void setMercadoPagoPhone(String mercadoPagoPhone) {
        this.mercadoPagoPhone = mercadoPagoPhone;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public com.google.firebase.Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(com.google.firebase.Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getDisplayName() {
        if (TYPE_MERCADO_PAGO.equals(type)) {
            return "Mercado Pago";
        } else if (cardBrand != null && !cardBrand.isEmpty()) {
            return cardBrand + " " + (TYPE_CREDIT.equals(type) ? "Crédito" : "Débito");
        } else {
            return (TYPE_CREDIT.equals(type) ? "Tarjeta de Crédito" : "Tarjeta de Débito");
        }
    }

    public String getDisplayNumber() {
        if (TYPE_MERCADO_PAGO.equals(type)) {
            return mercadoPagoEmail != null ? mercadoPagoEmail : mercadoPagoPhone;
        } else if (cardNumber != null && cardNumber.length() >= 4) {
            return "**** " + cardNumber.substring(cardNumber.length() - 4);
        }
        return "";
    }
}

