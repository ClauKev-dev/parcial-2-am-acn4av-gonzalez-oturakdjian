package com.example.final_am_acn4av_gonzalez_oturakdjian;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class DescuentosActivity extends BaseActivity {

    private static final String PRODUCT_IMAGE_URL = "https://products-images.farmatodo.com.ar/4z3TGXMto5MD4mX2-R0CV5bCtWJg8fgmlaYSnSYaJSmWggsdy8IKGSOtg-_m4zMy";
    private static final String MERCADO_PAGO_LOGO_URL = "https://play-lh.googleusercontent.com/UrB8aayxpFSw0zzKNFSWGgqXbxfpQ_U_DLPCnjYVAlWN8GsnMkprpD80l3k6c0hEOKQRpnTxGtGeddgV3wFq7w";
    private static final String NARANJA_X_LOGO_URL = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRaaOX6tCgZ8LCKDYrZMWY9rIpcCE1HamVn3Q&s";
    private static final String VISA_LOGO_URL = "https://static.vecteezy.com/system/resources/previews/020/975/576/non_2x/visa-logo-visa-icon-transparent-free-png.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_descuentos);
        setupBottomNavigation();
        setupTopNavigation();
        setupDrawer();

        navigateToTab(1);

        setupProductCard();
        setupPaymentCards();
    }

    private void setupProductCard() {
        View productCard = findViewById(R.id.product_card);
        
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductSubtitle;
        TextView tvOriginalPrice;
        TextView tvDiscountedPrice;
        
        if (productCard != null) {
            ivProductImage = productCard.findViewById(R.id.iv_product_image);
            tvProductName = productCard.findViewById(R.id.tv_product_name);
            tvProductSubtitle = productCard.findViewById(R.id.tv_product_subtitle);
            tvOriginalPrice = productCard.findViewById(R.id.tv_original_price);
            tvDiscountedPrice = productCard.findViewById(R.id.tv_discounted_price);
        } else {
            ivProductImage = findViewById(R.id.iv_product_image);
            tvProductName = findViewById(R.id.tv_product_name);
            tvProductSubtitle = findViewById(R.id.tv_product_subtitle);
            tvOriginalPrice = findViewById(R.id.tv_original_price);
            tvDiscountedPrice = findViewById(R.id.tv_discounted_price);
        }

        if (ivProductImage != null) {
            android.util.Log.d("DescuentosActivity", "Loading product image from: " + PRODUCT_IMAGE_URL);
            Glide.with(this)
                    .load(PRODUCT_IMAGE_URL)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .fitCenter()
                    .into(ivProductImage);
        } else {
            android.util.Log.e("DescuentosActivity", "Product ImageView is null!");
        }

        if (tvProductName != null) {
            tvProductName.setText("Venlifax XR 75");
        }
        if (tvProductSubtitle != null) {
            tvProductSubtitle.setText("venlafaxine");
        }
        if (tvOriginalPrice != null) {
            tvOriginalPrice.setText("$7500");
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        }
        if (tvDiscountedPrice != null) {
            tvDiscountedPrice.setText("$3600");
        }
    }

    private void setupPaymentCards() {
        View mercadoPagoCard = findViewById(R.id.mercado_pago_card);
        View naranjaXCard = findViewById(R.id.naranja_x_card);
        View visaCard = findViewById(R.id.visa_card);

        if (mercadoPagoCard != null) {
            ImageView logo = mercadoPagoCard.findViewById(R.id.iv_payment_logo);
            TextView text = mercadoPagoCard.findViewById(R.id.tv_promotion_text);
            
            if (logo != null) {
                android.util.Log.d("DescuentosActivity", "Loading Mercado Pago logo");
                Glide.with(this)
                        .load(MERCADO_PAGO_LOGO_URL)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .fitCenter()
                        .into(logo);
            } else {
                android.util.Log.e("DescuentosActivity", "Mercado Pago logo ImageView is null!");
            }
            
            if (text != null) {
                text.setText("25% OFF utilizando Mercado pago.");
            }
        }

        if (naranjaXCard != null) {
            ImageView logo = naranjaXCard.findViewById(R.id.iv_payment_logo);
            TextView text = naranjaXCard.findViewById(R.id.tv_promotion_text);
            
            if (logo != null) {
                android.util.Log.d("DescuentosActivity", "Loading Naranja X logo");
                Glide.with(this)
                        .load(NARANJA_X_LOGO_URL)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .fitCenter()
                        .into(logo);
            } else {
                android.util.Log.e("DescuentosActivity", "Naranja X logo ImageView is null!");
            }
            
            if (text != null) {
                text.setText("25% OFF utilizando Naranja x.");
            }
        }

        if (visaCard != null) {
            ImageView logo = visaCard.findViewById(R.id.iv_payment_logo);
            TextView text = visaCard.findViewById(R.id.tv_promotion_text);
            
            if (logo != null) {
                android.util.Log.d("DescuentosActivity", "Loading VISA logo");
                Glide.with(this)
                        .load(VISA_LOGO_URL)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .fitCenter()
                        .into(logo);
            } else {
                android.util.Log.e("DescuentosActivity", "VISA logo ImageView is null!");
            }
            
            if (text != null) {
                text.setText("6 cuotas sin intereses usando tarjeta de credito VISA");
            }
        }
    }
}

