package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DiscountHelper {
    private static final double SENIOR_DISCOUNT_PERCENTAGE = 0.15; // 15%
    private static final int WOMAN_AGE_THRESHOLD = 60;
    private static final int MAN_AGE_THRESHOLD = 65;

    /**
     * Calculates if a user qualifies for senior discount
     * @param birthDateString Birth date in format "dd/MM/yyyy"
     * @param gender "Mujer" or "Hombre"
     * @return true if user qualifies for discount
     */
    public static boolean qualifiesForDiscount(String birthDateString, String gender) {
        if (birthDateString == null || birthDateString.isEmpty() || 
            gender == null || gender.isEmpty()) {
            return false;
        }

        int age = calculateAge(birthDateString);
        if (age < 0) {
            return false; // Invalid date
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

    /**
     * Calculates age from birth date string
     * @param birthDateString Date in format "dd/MM/yyyy"
     * @return age in years, or -1 if invalid
     */
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
            
            // Check if birthday hasn't occurred this year
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age;
        } catch (ParseException e) {
            android.util.Log.e("DiscountHelper", "Error parsing birth date: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Applies discount to a total amount
     * @param total Original total
     * @param qualifiesForDiscount Whether user qualifies
     * @return Discounted total
     */
    public static double applyDiscount(double total, boolean qualifiesForDiscount) {
        if (qualifiesForDiscount) {
            return total * (1 - SENIOR_DISCOUNT_PERCENTAGE);
        }
        return total;
    }

    /**
     * Gets the discount amount
     * @param total Original total
     * @param qualifiesForDiscount Whether user qualifies
     * @return Discount amount
     */
    public static double getDiscountAmount(double total, boolean qualifiesForDiscount) {
        if (qualifiesForDiscount) {
            return total * SENIOR_DISCOUNT_PERCENTAGE;
        }
        return 0.0;
    }
}

