package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    private FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        container = findViewById(R.id.container);
        setContent(R.layout.activity_main);
    }

    protected void setContent(@LayoutRes int layoutResId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(layoutResId, container, false);
        container.removeAllViews();
        container.addView(view);
    }

    protected void navigateToTab(int tabIndex) {
        resetTabs();

        LinearLayout tabHome = findViewById(R.id.tab_home);
        LinearLayout tabDescuentos = findViewById(R.id.tab_descuentos);
        LinearLayout tabTienda = findViewById(R.id.tab_tienda);
        LinearLayout tabCuadrado = findViewById(R.id.tab_cuadrado);
        LinearLayout tabMenu = findViewById(R.id.tab_menu);

        switch (tabIndex) {
            case 0:
                tabHome.setAlpha(1f);
                break;
            case 1:
                tabDescuentos.setAlpha(1f);
                break;
            case 2:
                tabTienda.setAlpha(1f);
                break;
            case 3:
                tabCuadrado.setAlpha(1f);
                break;
            case 4:
                tabMenu.setAlpha(1f);
                break;
        }
    }

    protected void resetTabs() {
        LinearLayout tabHome = findViewById(R.id.tab_home);
        LinearLayout tabDescuentos = findViewById(R.id.tab_descuentos);
        LinearLayout tabTienda = findViewById(R.id.tab_tienda);
        LinearLayout tabCuadrado = findViewById(R.id.tab_cuadrado);
        LinearLayout tabMenu = findViewById(R.id.tab_menu);

        if (tabHome != null) tabHome.setAlpha(0.6f);
        if (tabDescuentos != null) tabDescuentos.setAlpha(0.6f);
        if (tabTienda != null) tabTienda.setAlpha(0.6f);
        if (tabCuadrado != null) tabCuadrado.setAlpha(0.6f);
        if (tabMenu != null) tabMenu.setAlpha(0.6f);
    }

    protected void setupBottomNavigation() {
        LinearLayout tabHome = findViewById(R.id.tab_home);
        LinearLayout tabDescuentos = findViewById(R.id.tab_descuentos);
        LinearLayout tabTienda = findViewById(R.id.tab_tienda);
        LinearLayout tabCuadrado = findViewById(R.id.tab_cuadrado);
        LinearLayout tabMenu = findViewById(R.id.tab_menu);

        // Constantes para los índices de tabs
        final int TAB_HOME = 0;
        final int TAB_DESCUENTOS = 1;
        final int TAB_TIENDA = 2;
        final int TAB_CUADRADO = 3;
        final int TAB_MENU = 4;

        View.OnClickListener listener = v -> {
            int tabIndex = -1;

            int id = v.getId();

            if (id == R.id.tab_home) {
                tabIndex = TAB_HOME;
                if (!(this instanceof MainActivity)) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            } else if (id == R.id.tab_descuentos) {
                tabIndex = TAB_DESCUENTOS;

            } else if (id == R.id.tab_tienda) {
                tabIndex = TAB_TIENDA;
                if (!(this instanceof CarritoActivity)) {
                    startActivity(new Intent(this, CarritoActivity.class));
                    finish();
                }
            } else if (id == R.id.tab_cuadrado) {
                tabIndex = TAB_CUADRADO;

            } else if (id == R.id.tab_menu) {
                tabIndex = TAB_MENU;

            }


            if (tabIndex != -1) {
                navigateToTab(tabIndex);
            }
        };


        if (tabHome != null) tabHome.setOnClickListener(listener);
        if (tabDescuentos != null) tabDescuentos.setOnClickListener(listener);
        if (tabTienda != null) tabTienda.setOnClickListener(listener);
        if (tabCuadrado != null) tabCuadrado.setOnClickListener(listener);
        if (tabMenu != null) tabMenu.setOnClickListener(listener);
    }

    protected void setupTopNavigation() {
        android.widget.ImageView ivProfile = findViewById(R.id.iv_profile);
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
            });
            // Make it clickable
            ivProfile.setClickable(true);
            ivProfile.setFocusable(true);
        }
    }


}

