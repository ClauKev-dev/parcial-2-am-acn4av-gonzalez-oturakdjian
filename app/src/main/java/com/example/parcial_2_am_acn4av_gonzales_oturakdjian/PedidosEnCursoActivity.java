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

    private RecyclerView recyclerPedidosEnCurso;
    private RecyclerView recyclerPedidosHistorial;
    private TextView tvEmptyEnCurso;
    private TextView tvEmptyHistorial;
    private PedidosAdapter adapterEnCurso;
    private PedidosAdapter adapterHistorial;
    private FirebaseFirestore db;
    private List<Order> pedidosEnCursoList;
    private List<Order> pedidosHistorialList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_pedidos_en_curso);
        setupBottomNavigation();
        setupTopNavigation();
        setupDrawer();

        recyclerPedidosEnCurso = findViewById(R.id.recyclerPedidosEnCurso);
        recyclerPedidosHistorial = findViewById(R.id.recyclerPedidosHistorial);
        tvEmptyEnCurso = findViewById(R.id.tvEmptyEnCurso);
        tvEmptyHistorial = findViewById(R.id.tvEmptyHistorial);
        db = FirebaseFirestore.getInstance();
        pedidosEnCursoList = new ArrayList<>();
        pedidosHistorialList = new ArrayList<>();

        recyclerPedidosEnCurso.setLayoutManager(new LinearLayoutManager(this));
        recyclerPedidosHistorial.setLayoutManager(new LinearLayoutManager(this));
        adapterEnCurso = new PedidosAdapter(this, pedidosEnCursoList);
        adapterHistorial = new PedidosAdapter(this, pedidosHistorialList);
        recyclerPedidosEnCurso.setAdapter(adapterEnCurso);
        recyclerPedidosHistorial.setAdapter(adapterHistorial);

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
            tvEmptyEnCurso.setVisibility(View.VISIBLE);
            recyclerPedidosEnCurso.setVisibility(View.GONE);
            tvEmptyHistorial.setVisibility(View.VISIBLE);
            recyclerPedidosHistorial.setVisibility(View.GONE);
            return;
        }

        db.collection("users").document(user.getUid()).collection("orders")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pedidosEnCursoList.clear();
                    pedidosHistorialList.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        order.setId(document.getId());
                        
                        // Separar pedidos en curso del historial
                        String status = order.getStatus();
                        if (status != null && status.equals("en_curso")) {
                            pedidosEnCursoList.add(order);
                        } else {
                            // Historial: completados y cancelados
                            pedidosHistorialList.add(order);
                        }
                    }

                    // Actualizar sección de pedidos en curso
                    if (pedidosEnCursoList.isEmpty()) {
                        tvEmptyEnCurso.setVisibility(View.VISIBLE);
                        recyclerPedidosEnCurso.setVisibility(View.GONE);
                    } else {
                        tvEmptyEnCurso.setVisibility(View.GONE);
                        recyclerPedidosEnCurso.setVisibility(View.VISIBLE);
                        adapterEnCurso.notifyDataSetChanged();
                    }

                    // Actualizar sección de historial
                    if (pedidosHistorialList.isEmpty()) {
                        tvEmptyHistorial.setVisibility(View.VISIBLE);
                        recyclerPedidosHistorial.setVisibility(View.GONE);
                    } else {
                        tvEmptyHistorial.setVisibility(View.GONE);
                        recyclerPedidosHistorial.setVisibility(View.VISIBLE);
                        adapterHistorial.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("PedidosEnCursoActivity", "Error al cargar pedidos: " + e.getMessage(), e);
                    tvEmptyEnCurso.setVisibility(View.VISIBLE);
                    recyclerPedidosEnCurso.setVisibility(View.GONE);
                    tvEmptyHistorial.setVisibility(View.VISIBLE);
                    recyclerPedidosHistorial.setVisibility(View.GONE);
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

