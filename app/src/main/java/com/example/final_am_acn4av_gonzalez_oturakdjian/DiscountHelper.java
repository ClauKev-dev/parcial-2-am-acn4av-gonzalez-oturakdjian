package com.example.final_am_acn4av_gonzalez_oturakdjian;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DiscountHelper {
    private static final double SENIOR_DISCOUNT_PERCENTAGE = 0.15;
    private static final double PAYMENT_METHOD_DISCOUNT_PERCENTAGE = 0.25;
    private static final int WOMAN_AGE_THRESHOLD = 60;
    private static final int MAN_AGE_THRESHOLD = 65;

    public static boolean qualifiesForDiscount(String birthDateString, String gender) {
        if (birthDateString == null || birthDateString.isEmpty() || 
            gender == null || gender.isEmpty()) {
            return false;
        }

        int age = calculateAge(birthDateString);
        if (age < 0) {
            return false;
        }

        if (gender.equalsIgnoreCase("Mujer") || gender.equalsIgnoreCase("Femenino") || 
            gender.equalsIgnoreCase("F")) {
            return age > WOMAN_AGE_THRESHOLD;
        } else if (gender.equalsIgnoreCase("Hombre") || gender.equalsIgnoreCase("Masculino") || 
                   gender.equalsIgnoreCase("M")) {
            return age > MAN_AGE_THRESHOLD;
        }

        return false;
    }

    public static int calculateAge(String birthDateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date birthDate = sdf.parse(birthDateString);
            if (birthDate == null) {
                return -1;
            }

            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);
            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age;
        } catch (ParseException e) {
            android.util.Log.e("DiscountHelper", "Error parsing birth date: " + e.getMessage());
            return -1;
        }
    }

    public static double applyDiscount(double total, boolean qualifiesForDiscount) {
        if (qualifiesForDiscount) {
            return total * (1 - SENIOR_DISCOUNT_PERCENTAGE);
        }
        return total;
    }

    public static double getDiscountAmount(double total, boolean qualifiesForDiscount) {
        if (qualifiesForDiscount) {
            return total * SENIOR_DISCOUNT_PERCENTAGE;
        }
        return 0.0;
    }

    public static boolean paymentMethodHasDiscount(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return false;
        }
        
        String type = paymentMethod.getType();
        String brand = paymentMethod.getCardBrand();

        if (PaymentMethod.TYPE_MERCADO_PAGO.equals(type)) {
            return true;
        }

        if (brand != null && brand.equalsIgnoreCase("Naranja X")) {
            return true;
        }
        
        return false;
    }

    public static double applyPaymentMethodDiscount(double total, PaymentMethod paymentMethod) {
        if (paymentMethodHasDiscount(paymentMethod)) {
            return total * (1 - PAYMENT_METHOD_DISCOUNT_PERCENTAGE);
        }
        return total;
    }

    public static double getPaymentMethodDiscountAmount(double total, PaymentMethod paymentMethod) {
        if (paymentMethodHasDiscount(paymentMethod)) {
            return total * PAYMENT_METHOD_DISCOUNT_PERCENTAGE;
        }
        return 0.0;
    }

    public static boolean supportsInstallments(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return false;
        }

        return PaymentMethod.TYPE_CREDIT.equals(paymentMethod.getType()) &&
               "Visa".equalsIgnoreCase(paymentMethod.getCardBrand());
    }

    public static double applyAllDiscounts(double total, boolean qualifiesForSeniorDiscount, PaymentMethod paymentMethod) {
        double discountedTotal = total;
        
        // Apply senior discount first
        if (qualifiesForSeniorDiscount) {
            discountedTotal = applyDiscount(discountedTotal, true);
        }
        
        // Apply payment method discount
        if (paymentMethodHasDiscount(paymentMethod)) {
            discountedTotal = applyPaymentMethodDiscount(discountedTotal, paymentMethod);
        }
        
        return discountedTotal;
    }

    public static double getTotalDiscountAmount(double total, boolean qualifiesForSeniorDiscount, PaymentMethod paymentMethod) {
        double seniorDiscount = qualifiesForSeniorDiscount ? getDiscountAmount(total, true) : 0.0;
        double totalAfterSenior = total - seniorDiscount;
        double paymentDiscount = getPaymentMethodDiscountAmount(totalAfterSenior, paymentMethod);
        return seniorDiscount + paymentDiscount;
    }
}

