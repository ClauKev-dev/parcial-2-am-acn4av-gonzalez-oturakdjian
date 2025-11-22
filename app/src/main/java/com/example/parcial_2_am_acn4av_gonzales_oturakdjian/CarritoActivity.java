package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class CarritoActivity extends BaseActivity {

    private RecyclerView recyclerCarrito;
    private TextView tvTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_carrito);
        setupBottomNavigation();
        setupTopNavigation();
        recyclerCarrito = findViewById(R.id.recyclerCarrito);
        tvTotal = findViewById(R.id.tvTotal);

        recyclerCarrito.setLayoutManager(new LinearLayoutManager(this));

        List<Product> carrito = CarritoManager.getCarrito(); // tu clase estática previa
        CarritoAdapter adapter = new CarritoAdapter(this, carrito);
        recyclerCarrito.setAdapter(adapter);

        double total = 0;
        for (Product p : carrito) {
            total += p.getPrice() * p.getQuantity();
        }
        tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));

    }

    public void actualizarTotal() {
        double total = 0;
        for (Product p : CarritoManager.getCarrito()) {
            total += p.getPrice() * p.getQuantity();
        }
        tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
    }

}
