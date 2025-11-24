package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CarritoActivity extends BaseActivity {

    private RecyclerView recyclerCarrito;
    private TextView tvTotal;
    private Button btnPagar;
    private ProgressBar progressBar;
    private CarritoAdapter adapter;
    private FirebaseFirestore db;

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
        btnPagar = findViewById(R.id.btnPagar);
        db = FirebaseFirestore.getInstance();

        recyclerCarrito.setLayoutManager(new LinearLayoutManager(this));

        // Configurar botón de pago
        btnPagar.setOnClickListener(v -> mostrarDialogoPago());

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

    private void mostrarDialogoPago() {
        List<Product> carrito = CarritoManager.getCarrito();
        if (carrito.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear diálogo personalizado
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_pago, null);
        builder.setView(dialogView);

        EditText etCardNumber = dialogView.findViewById(R.id.et_card_number);
        EditText etCardHolder = dialogView.findViewById(R.id.et_card_holder);
        EditText etExpiryDate = dialogView.findViewById(R.id.et_expiry_date);
        EditText etCVV = dialogView.findViewById(R.id.et_cvv);
        TextView tvTotalPago = dialogView.findViewById(R.id.tv_total_pago);

        // Calcular total
        double totalCalculado = 0;
        for (Product p : carrito) {
            totalCalculado += p.getPrice() * p.getQuantity();
        }
        final double total = totalCalculado; // Hacer final para usar en lambda
        tvTotalPago.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));

        // Formatear número de tarjeta (agregar espacios cada 4 dígitos)
        etCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().replaceAll(" ", "");
                if (input.length() > 16) {
                    input = input.substring(0, 16);
                }
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < input.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(input.charAt(i));
                }
                if (!formatted.toString().equals(s.toString())) {
                    etCardNumber.removeTextChangedListener(this);
                    etCardNumber.setText(formatted.toString());
                    etCardNumber.setSelection(formatted.length());
                    etCardNumber.addTextChangedListener(this);
                }
            }
        });

        // Formatear fecha de expiración (MM/YY)
        etExpiryDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().replaceAll("/", "");
                if (input.length() > 4) {
                    input = input.substring(0, 4);
                }
                if (input.length() >= 2 && !input.contains("/")) {
                    input = input.substring(0, 2) + "/" + input.substring(2);
                }
                if (!input.equals(s.toString())) {
                    etExpiryDate.removeTextChangedListener(this);
                    etExpiryDate.setText(input);
                    etExpiryDate.setSelection(input.length());
                    etExpiryDate.addTextChangedListener(this);
                }
            }
        });

        // Limitar CVV a 3 dígitos
        etCVV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 3) {
                    s.delete(3, s.length());
                }
            }
        });

        builder.setTitle("Datos de Pago");
        builder.setPositiveButton("Pagar", (dialog, which) -> {
            String cardNumber = etCardNumber.getText().toString().replaceAll(" ", "");
            String cardHolder = etCardHolder.getText().toString().trim();
            String expiryDate = etExpiryDate.getText().toString();
            String cvv = etCVV.getText().toString();

            // Validar campos
            if (cardNumber.length() != 16) {
                Toast.makeText(this, "Número de tarjeta inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            if (cardHolder.isEmpty()) {
                Toast.makeText(this, "Ingrese el nombre del titular", Toast.LENGTH_SHORT).show();
                return;
            }
            if (expiryDate.length() != 5) {
                Toast.makeText(this, "Fecha de expiración inválida", Toast.LENGTH_SHORT).show();
                return;
            }
            if (cvv.length() != 3) {
                Toast.makeText(this, "CVV inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            // Obtener últimos 4 dígitos
            String lastFour = cardNumber.substring(cardNumber.length() - 4);
            procesarPago(lastFour, cardHolder, total);
        });
        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void procesarPago(String cardLastFour, String cardHolder, double total) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión para realizar el pago", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Product> carrito = CarritoManager.getCarrito();
        if (carrito.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear copia de los productos para el pedido
        List<Product> productosPedido = new ArrayList<>();
        for (Product p : carrito) {
            Product copia = new Product(p.getImageUrl(), p.getName(), p.getPrice());
            copia.setQuantity(p.getQuantity());
            productosPedido.add(copia);
        }

        // Crear pedido
        Order order = new Order(user.getUid(), productosPedido, total, cardLastFour, cardHolder);

        // Guardar pedido en Firestore
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", order.getUserId());
        orderData.put("products", productosPedido);
        orderData.put("total", order.getTotal());
        orderData.put("status", "en_curso");
        orderData.put("cardNumber", order.getCardNumber());
        orderData.put("cardHolder", order.getCardHolder());
        orderData.put("createdAt", com.google.firebase.Timestamp.now());
        orderData.put("updatedAt", com.google.firebase.Timestamp.now());

        btnPagar.setEnabled(false);
        btnPagar.setText("Procesando...");

        db.collection("users").document(user.getUid()).collection("orders")
                .add(orderData)
                .addOnSuccessListener(documentReference -> {
                    order.setId(documentReference.getId());
                    android.util.Log.d("CarritoActivity", "Pedido guardado exitosamente: " + documentReference.getId());
                    
                    // Limpiar carrito después del pago exitoso (tanto local como en Firestore)
                    CarritoManager.limpiarCarrito();
                    android.util.Log.d("CarritoActivity", "Carrito limpiado después del pago");
                    
                    // Actualizar UI inmediatamente
                    actualizarTotal();
                    
                    // Recargar carrito desde Firestore para asegurar que esté vacío
                    cargarCarrito();
                    
                    // Actualizar botón
                    btnPagar.setEnabled(true);
                    btnPagar.setText("Pagar");
                    
                    // Mostrar mensaje de éxito
                    Toast.makeText(this, "¡Pago realizado exitosamente! El carrito ha sido vaciado.", Toast.LENGTH_LONG).show();
                    
                    // Opcional: navegar a pedidos en curso
                    // Intent intent = new Intent(this, PedidosEnCursoActivity.class);
                    // startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    String errorMsg = e.getMessage();
                    android.util.Log.e("CarritoActivity", "Error al guardar pedido: " + errorMsg, e);
                    
                    // Verificar si es un error de permisos
                    if (errorMsg != null && (errorMsg.contains("permission") || errorMsg.contains("PERMISSION_DENIED") || errorMsg.contains("Missing or insufficient permissions"))) {
                        android.util.Log.e("CarritoActivity", "⚠️ ERROR DE PERMISOS: Las reglas de Firestore no permiten escribir en orders");
                        Toast.makeText(this, "Error de permisos. Verifica las reglas de Firestore en Firebase Console.\n\nDebes agregar permisos para la colección 'orders'", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error al procesar el pago: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                    
                    btnPagar.setEnabled(true);
                    btnPagar.setText("Pagar");
                });
    }

}
