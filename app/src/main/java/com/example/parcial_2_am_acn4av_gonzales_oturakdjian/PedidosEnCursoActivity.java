package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PedidosEnCursoActivity extends BaseActivity {

    private RecyclerView recyclerPedidos;
    private TextView tvEmpty;
    private PedidosAdapter adapter;
    private FirebaseFirestore db;
    private List<Order> pedidosList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_pedidos_en_curso);
        setupBottomNavigation();
        setupTopNavigation();
        setupDrawer();

        recyclerPedidos = findViewById(R.id.recyclerPedidos);
        tvEmpty = findViewById(R.id.tvEmpty);
        db = FirebaseFirestore.getInstance();
        pedidosList = new ArrayList<>();

        recyclerPedidos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PedidosAdapter(this, pedidosList);
        recyclerPedidos.setAdapter(adapter);

        cargarPedidos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPedidos();
    }

    private void cargarPedidos() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerPedidos.setVisibility(View.GONE);
            return;
        }

        db.collection("users").document(user.getUid()).collection("orders")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pedidosList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        order.setId(document.getId());
                        pedidosList.add(order);
                    }

                    if (pedidosList.isEmpty()) {
                        tvEmpty.setText("No hay pedidos");
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerPedidos.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerPedidos.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("PedidosEnCursoActivity", "Error al cargar pedidos: " + e.getMessage(), e);
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerPedidos.setVisibility(View.GONE);
                });
    }

    public static String formatearFecha(com.google.firebase.Timestamp timestamp) {
        if (timestamp == null) {
            return "Fecha no disponible";
        }
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
}

