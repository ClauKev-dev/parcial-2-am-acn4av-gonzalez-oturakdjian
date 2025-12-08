package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LootboxActivity extends BaseActivity {

    private static final String KEY_PRODUCT_NAME = "Llave";
    private static final double KEY_PRICE = 2500.0;
    private static final String KEY_IMAGE_URL = ""; // Can be set later if needed
    private static final String LOOTBOX_IMAGE_URL = "https://i.imgur.com/SMNYpRE.png";

    private RecyclerView recyclerLootboxProducts;
    private LootboxProductAdapter lootboxProductAdapter;
    private List<Product> lootboxProductList;
    private Button btnAbrirCaja;
    private FirebaseFirestore db;
    private int keyCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_lootbox);
        setupBottomNavigation();
        setupTopNavigation();
        setupDrawer();

        navigateToTab(3); // TAB_CUADRADO = 3

        db = FirebaseFirestore.getInstance();
        setupLootboxScreen();
        loadKeyCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadKeyCount(); // Reload key count when returning to this screen
    }

    private void setupLootboxScreen() {
        Button btnComprar = findViewById(R.id.btn_comprar);
        btnAbrirCaja = findViewById(R.id.btn_abrir_caja);
        recyclerLootboxProducts = findViewById(R.id.recycler_lootbox_products);
        ImageView ivLootbox = findViewById(R.id.ic_lootbox);

        // Load lootbox image from URL
        if (ivLootbox != null) {
            android.util.Log.d("LootboxActivity", "Loading lootbox image from: " + LOOTBOX_IMAGE_URL);
            Glide.with(this)
                    .load(LOOTBOX_IMAGE_URL)
                    .placeholder(R.drawable.ic_box)
                    .error(R.drawable.ic_box)
                    .fitCenter()
                    .into(ivLootbox);
        } else {
            android.util.Log.e("LootboxActivity", "ImageView ic_lootbox is null!");
        }

        // Setup product grid
        lootboxProductList = new ArrayList<>();
        // Add sample products that can be won from the lootbox
        lootboxProductList.add(new Product("https://tafirol.com/hubfs/Frame%20100000594899.png", "Tafirol para espasmos", 8000.0));
        lootboxProductList.add(new Product("https://www.isalaboratorios.com.ar/img/estuches/venta-libre/paracetamol-isa-500.png", "Paracetamol 500mg", 2500.0));
        lootboxProductList.add(new Product("https://farmacityar.vtexassets.com/arquivos/ids/260677/240916_ibuprofeno-400-x-20-comp_imagen-1.jpg?v=638629753908430000", "Ibuprofeno 400", 6000.0));
        lootboxProductList.add(new Product("https://images-us.eucerin.com/~/media/hansaplast/local/region-latam/2024/pdps%20packshots/transpiel%20x20.jpg", "Curitas", 3500.0));
        lootboxProductList.add(new Product("https://cdn.batitienda.com/baticloud/images/product_picture_0f1e2bc768fd41199b928b08b76a2a74_637873163055766508_0_m.jpg", "Jarabe para la tos", 4200.0));
        lootboxProductList.add(new Product("https://s2.elespanol.com/2018/06/08/ciencia/salud/farmacologia-farmacologia_clinica-salud_313481328_81018057_1706x1280.jpg", "Aspirina", 1800.0));

        if (recyclerLootboxProducts != null) {
            recyclerLootboxProducts.setLayoutManager(new GridLayoutManager(this, 3));
            lootboxProductAdapter = new LootboxProductAdapter(this, lootboxProductList);
            recyclerLootboxProducts.setAdapter(lootboxProductAdapter);
        }

        if (btnComprar != null) {
            btnComprar.setOnClickListener(v -> {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(this, "Debes iniciar sesión para comprar", Toast.LENGTH_LONG).show();
                    return;
                }

                // Create key product
                Product keyProduct = new Product(KEY_IMAGE_URL, KEY_PRODUCT_NAME, KEY_PRICE);
                
                // Add to cart
                CarritoManager.agregarProducto(keyProduct);
                
                Toast.makeText(this, KEY_PRODUCT_NAME + " agregada al carrito", Toast.LENGTH_SHORT).show();
                
                // Navigate to cart
                startActivity(new Intent(this, CarritoActivity.class));
            });
        }

        if (btnAbrirCaja != null) {
            btnAbrirCaja.setOnClickListener(v -> {
                if (keyCount > 0) {
                    // Future implementation: open lootbox if user has keys
                    Toast.makeText(this, "Función de abrir caja próximamente disponible", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No tienes llaves disponibles. Compra una llave primero.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadKeyCount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            updateButtonUI();
            return;
        }

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long keys = documentSnapshot.getLong("lootboxKeys");
                        keyCount = keys != null ? keys.intValue() : 0;
                    } else {
                        keyCount = 0;
                    }
                    updateButtonUI();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("LootboxActivity", "Error al cargar cantidad de llaves: " + e.getMessage());
                    keyCount = 0;
                    updateButtonUI();
                });
    }

    private void updateButtonUI() {
        if (btnAbrirCaja != null) {
            btnAbrirCaja.setText(String.format(Locale.getDefault(), "ABRIR CAJA (%d)", keyCount));
            
            if (keyCount > 0) {
                // Change to green when keys are available
                btnAbrirCaja.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary_green, getTheme())));
            } else {
                // Gray when no keys
                btnAbrirCaja.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.darker_gray, getTheme())));
            }
        }
    }
}

