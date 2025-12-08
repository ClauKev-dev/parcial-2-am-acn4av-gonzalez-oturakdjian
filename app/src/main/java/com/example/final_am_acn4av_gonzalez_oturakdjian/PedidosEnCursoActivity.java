package com.example.final_am_acn4av_gonzalez_oturakdjian;

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
import android.os.Handler;

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
    private Handler statusUpdateHandler;
    private Runnable statusUpdateRunnable;
    private static final long UPDATE_INTERVAL_MS = 10000;
    private static final long STATUS_CHANGE_DELAY_MS = 60000;

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

        statusUpdateHandler = new Handler();
        statusUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                verificarYActualizarEstados();
                statusUpdateHandler.postDelayed(this, UPDATE_INTERVAL_MS);
            }
        };

        cargarPedidos();
        statusUpdateHandler.postDelayed(statusUpdateRunnable, UPDATE_INTERVAL_MS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPedidos();
        if (statusUpdateHandler != null && statusUpdateRunnable != null) {
            statusUpdateHandler.postDelayed(statusUpdateRunnable, UPDATE_INTERVAL_MS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (statusUpdateHandler != null && statusUpdateRunnable != null) {
            statusUpdateHandler.removeCallbacks(statusUpdateRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusUpdateHandler != null && statusUpdateRunnable != null) {
            statusUpdateHandler.removeCallbacks(statusUpdateRunnable);
        }
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
                    
                    long currentTime = System.currentTimeMillis();
                    List<String> ordersToUpdate = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        order.setId(document.getId());

                        String status = order.getStatus();
                        if (status != null && status.equals("en_curso")) {
                            com.google.firebase.Timestamp createdAt = order.getCreatedAt();
                            if (createdAt != null) {
                                long orderCreatedTime = createdAt.toDate().getTime();
                                long timeElapsed = currentTime - orderCreatedTime;
                                
                                if (timeElapsed >= STATUS_CHANGE_DELAY_MS) {

                                    ordersToUpdate.add(document.getId());
                                    order.setStatus("completado");
                                    pedidosHistorialList.add(order);
                                } else {
                                    pedidosEnCursoList.add(order);
                                }
                            } else {
                                pedidosEnCursoList.add(order);
                            }
                        } else {

                            pedidosHistorialList.add(order);
                        }
                    }

                    for (String orderId : ordersToUpdate) {
                        actualizarEstadoPedido(user.getUid(), orderId);
                    }

                    if (pedidosEnCursoList.isEmpty()) {
                        tvEmptyEnCurso.setVisibility(View.VISIBLE);
                        recyclerPedidosEnCurso.setVisibility(View.GONE);
                    } else {
                        tvEmptyEnCurso.setVisibility(View.GONE);
                        recyclerPedidosEnCurso.setVisibility(View.VISIBLE);
                        adapterEnCurso.notifyDataSetChanged();
                    }

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

    private void verificarYActualizarEstados() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        db.collection("users").document(user.getUid()).collection("orders")
                .whereEqualTo("status", "en_curso")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        com.google.firebase.Timestamp createdAt = order.getCreatedAt();

                        if (createdAt != null) {
                            long orderCreatedTime = createdAt.toDate().getTime();
                            long timeElapsed = currentTime - orderCreatedTime;

                            if (timeElapsed >= STATUS_CHANGE_DELAY_MS) {
                                actualizarEstadoPedido(user.getUid(), document.getId());
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("PedidosEnCursoActivity", "Error al verificar estados: " + e.getMessage(), e);
                });
    }

    private void actualizarEstadoPedido(String userId, String orderId) {
        db.collection("users").document(userId).collection("orders").document(orderId)
                .update("status", "completado", "updatedAt", com.google.firebase.Timestamp.now())
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("PedidosEnCursoActivity", "Estado actualizado a completado para pedido: " + orderId);
                    // Reload orders to reflect the change
                    cargarPedidos();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("PedidosEnCursoActivity", "Error al actualizar estado: " + e.getMessage(), e);
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

