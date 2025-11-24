package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class CarritoActivity extends BaseActivity {

    private RecyclerView recyclerCarrito;
    private TextView tvTotal;
    private ProgressBar progressBar;
    private CarritoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_carrito);
        setupBottomNavigation();
        setupTopNavigation();
        // Ensure drawer is set up (inherited from BaseActivity, but ensure it's accessible)
        setupDrawer();
        recyclerCarrito = findViewById(R.id.recyclerCarrito);
        tvTotal = findViewById(R.id.tvTotal);

        recyclerCarrito.setLayoutManager(new LinearLayoutManager(this));

        // Cargar carrito desde Firestore
        cargarCarrito();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar carrito cuando la actividad se reanuda
        cargarCarrito();
    }

    private void cargarCarrito() {
        // Mostrar progress bar mientras se carga (si existe)
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        android.util.Log.d("CarritoActivity", "Cargando carrito...");

        CarritoManager.cargarCarrito(new CarritoManager.CarritoLoadListener() {
            @Override
            public void onCarritoLoaded(List<Product> carrito) {
                // Ocultar progress bar
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                android.util.Log.d("CarritoActivity", "Carrito cargado: " + carrito.size() + " productos");

                // Actualizar adapter con la lista del carrito
                List<Product> carritoActual = CarritoManager.getCarrito();
                if (adapter == null) {
                    adapter = new CarritoAdapter(CarritoActivity.this, carritoActual);
                    recyclerCarrito.setAdapter(adapter);
                } else {
                    // Actualizar la lista del adapter
                    adapter = new CarritoAdapter(CarritoActivity.this, carritoActual);
                    recyclerCarrito.setAdapter(adapter);
                }

                // Actualizar total
                actualizarTotal();
            }

            @Override
            public void onError(String error) {
                // Ocultar progress bar
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                android.util.Log.e("CarritoActivity", "Error al cargar carrito: " + error);
            }
        });
    }

    public void actualizarTotal() {
        double total = 0;
        for (Product p : CarritoManager.getCarrito()) {
            total += p.getPrice() * p.getQuantity();
        }
        tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
    }

    public void notificarCambioCarrito() {
        // Recargar carrito cuando hay cambios
        cargarCarrito();
    }

}
