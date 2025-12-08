package com.example.final_am_acn4av_gonzalez_oturakdjian;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StrikethroughSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
        double total = CarritoManager.getCarrito().stream().mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();

        // Check for senior discount
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String gender = documentSnapshot.getString("gender");
                        String birthdate = documentSnapshot.getString("birthdate");
                        
                        boolean qualifies = DiscountHelper.qualifiesForDiscount(birthdate, gender);
                        double finalTotal = DiscountHelper.applyDiscount(total, qualifies);
                        double discount = DiscountHelper.getDiscountAmount(total, qualifies);
                        
                        if (qualifies) {
                            // Create text with original price crossed out in red, then final price
                            String originalPriceText = String.format(Locale.getDefault(), "$%.2f", total);
                            String finalPriceText = String.format(Locale.getDefault(), " $%.2f", finalTotal);
                            String fullText = "Total: " + originalPriceText + finalPriceText;
                            
                            SpannableString spannable = new SpannableString(fullText);
                            
                            // Find the position of the original price
                            int originalPriceStart = fullText.indexOf(originalPriceText);
                            int originalPriceEnd = originalPriceStart + originalPriceText.length();
                            
                            // Apply strikethrough and red color to original price
                            spannable.setSpan(new StrikethroughSpan(), originalPriceStart, originalPriceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            spannable.setSpan(new ForegroundColorSpan(Color.RED), originalPriceStart, originalPriceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            
                            tvTotal.setText(spannable);
                        } else {
                            tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", finalTotal));
                        }
                    } else {
                        tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
                    }
                })
                .addOnFailureListener(e -> {
                    // On error, just show regular total
                    tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
                });
        } else {
            tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
        }
    }

    public void notificarCambioCarrito() {
        // Recargar carrito cuando hay cambios
        cargarCarrito();
    }

    private void mostrarDialogoPago() {
        mostrarSeleccionMetodoPago();
    }

    private void mostrarSeleccionMetodoPago() {
        List<Product> carrito = CarritoManager.getCarrito();
        if (carrito.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calcular total base
        double totalCalculado = carrito.stream().mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();

        // Crear diálogo de selección de método de pago
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_payment_method_selection, null);
        builder.setView(dialogView);

        RecyclerView recyclerSavedMethods = dialogView.findViewById(R.id.recycler_saved_payment_methods);
        TextView tvSavedMethodsTitle = dialogView.findViewById(R.id.tv_saved_methods_title);
        TextView tvNoSavedMethods = dialogView.findViewById(R.id.tv_no_saved_methods);
        RadioGroup rgPaymentType = dialogView.findViewById(R.id.rg_payment_type);
        RadioButton rbDebit = dialogView.findViewById(R.id.rb_debit);
        RadioButton rbCredit = dialogView.findViewById(R.id.rb_credit);
        RadioButton rbMercadoPago = dialogView.findViewById(R.id.rb_mercado_pago);
        TextView tvTotalPayment = dialogView.findViewById(R.id.tv_total_payment);

        // Update total display
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            updateTotalDisplay(tvTotalPayment, totalCalculado, null, user.getUid());
        } else {
            tvTotalPayment.setText(String.format(Locale.getDefault(), "Total: $%.2f", totalCalculado));
        }

        // Load saved payment methods
        List<PaymentMethod> savedMethods = new ArrayList<>();
        PaymentMethodAdapter adapter = new PaymentMethodAdapter(savedMethods, paymentMethod -> {
            // User selected a saved payment method
            processPaymentWithMethod(paymentMethod, totalCalculado);
        });
        recyclerSavedMethods.setLayoutManager(new LinearLayoutManager(this));
        recyclerSavedMethods.setAdapter(adapter);

        if (user != null) {
            loadSavedPaymentMethods(user.getUid(), savedMethods, adapter, tvSavedMethodsTitle, recyclerSavedMethods, tvNoSavedMethods);
        }

        // Set default selection
        rbDebit.setChecked(true);

        builder.setTitle("Método de Pago");
        builder.setPositiveButton("Continuar", (dialog, which) -> {
            int selectedId = rgPaymentType.getCheckedRadioButtonId();
            if (selectedId == R.id.rb_debit || selectedId == R.id.rb_credit) {
                mostrarFormularioTarjeta(selectedId == R.id.rb_credit, totalCalculado);
            } else if (selectedId == R.id.rb_mercado_pago) {
                mostrarFormularioMercadoPago(totalCalculado);
            }
        });
        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loadSavedPaymentMethods(String userId, List<PaymentMethod> savedMethods, PaymentMethodAdapter adapter,
                                        TextView tvTitle, RecyclerView recyclerView, TextView tvNoSaved) {
        db.collection("users").document(userId).collection("paymentMethods")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    savedMethods.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        PaymentMethod pm = document.toObject(PaymentMethod.class);
                        pm.setId(document.getId());
                        savedMethods.add(pm);
                    }

                    if (savedMethods.isEmpty()) {
                        tvTitle.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        tvNoSaved.setVisibility(View.VISIBLE);
                    } else {
                        tvTitle.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                        tvNoSaved.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    tvTitle.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    tvNoSaved.setVisibility(View.VISIBLE);
                });
    }

    private void mostrarFormularioTarjeta(boolean isCredit, double totalCalculado) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_pago, null);
        builder.setView(dialogView);

        EditText etCardNumber = dialogView.findViewById(R.id.et_card_number);
        EditText etCardHolder = dialogView.findViewById(R.id.et_card_holder);
        EditText etExpiryDate = dialogView.findViewById(R.id.et_expiry_date);
        EditText etCVV = dialogView.findViewById(R.id.et_cvv);
        TextView tvTotalPago = dialogView.findViewById(R.id.tv_total_pago);
        CheckBox cbSaveMethod = dialogView.findViewById(R.id.cb_save_payment_method);
        
        // Log if checkbox is not found
        if (cbSaveMethod == null) {
            android.util.Log.w("CarritoActivity", "Checkbox cb_save_payment_method not found in layout");
        }

        // Add save checkbox if not exists in layout
        if (cbSaveMethod == null) {
            // We'll add it programmatically if needed
        }

        // Update total
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            updateTotalDisplay(tvTotalPago, totalCalculado, null, user.getUid());
        }

        // Card formatting listeners (same as before)
        setupCardFormatting(etCardNumber, etExpiryDate, etCVV);

        builder.setTitle(isCredit ? "Tarjeta de Crédito" : "Tarjeta de Débito");
        builder.setPositiveButton("Pagar", (dialog, which) -> {
            String cardNumber = etCardNumber.getText().toString().replaceAll(" ", "");
            String cardHolder = etCardHolder.getText().toString().trim();
            String expiryDate = etExpiryDate.getText().toString();
            String cvv = etCVV.getText().toString();

            if (!validateCardForm(cardNumber, cardHolder, expiryDate, cvv)) {
                return;
            }

            // Detect card brand
            String cardBrand = detectCardBrand(cardNumber);
            String lastFour = cardNumber.substring(cardNumber.length() - 4);

            // Create payment method
            PaymentMethod paymentMethod = new PaymentMethod(
                user.getUid(),
                isCredit ? PaymentMethod.TYPE_CREDIT : PaymentMethod.TYPE_DEBIT,
                lastFour,
                cardHolder,
                cardBrand,
                expiryDate
            );

            // Save payment method if checkbox is checked
            boolean shouldSave = cbSaveMethod != null && cbSaveMethod.isChecked();
            android.util.Log.d("CarritoActivity", "Should save payment method: " + shouldSave + " (checkbox checked: " + (cbSaveMethod != null ? cbSaveMethod.isChecked() : "null") + ")");
            
            if (shouldSave && user != null) {
                android.util.Log.d("CarritoActivity", "Saving payment method before processing payment");
                savePaymentMethod(paymentMethod, () -> {
                    android.util.Log.d("CarritoActivity", "Payment method save completed, processing payment");
                    processPaymentWithMethod(paymentMethod, totalCalculado);
                });
            } else {
                android.util.Log.d("CarritoActivity", "Skipping payment method save, processing payment directly");
                processPaymentWithMethod(paymentMethod, totalCalculado);
            }
        });
        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void mostrarFormularioMercadoPago(double totalCalculado) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get user profile data to validate email/phone
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Error al cargar datos del perfil", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String profileEmail = documentSnapshot.getString("email");
                    String profilePhone = documentSnapshot.getString("phone");

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog_mercado_pago_login, null);
                    builder.setView(dialogView);

                    EditText etEmailPhone = dialogView.findViewById(R.id.et_mp_email_phone);
                    EditText etPassword = dialogView.findViewById(R.id.et_mp_password);
                    TextView tvError = dialogView.findViewById(R.id.tv_mp_error);
                    CheckBox cbSave = dialogView.findViewById(R.id.cb_save_mp_method);

                    builder.setTitle("Mercado Pago");
                    builder.setPositiveButton("Continuar", (dialog, which) -> {
                        String emailPhone = etEmailPhone.getText().toString().trim();
                        String password = etPassword.getText().toString().trim();

                        // Validate email/phone matches profile
                        boolean isValid = false;
                        String mpEmail = null;
                        String mpPhone = null;

                        if (emailPhone.contains("@")) {
                            // It's an email
                            if (profileEmail != null && profileEmail.equalsIgnoreCase(emailPhone)) {
                                isValid = true;
                                mpEmail = emailPhone;
                            }
                        } else {
                            // It's a phone
                            if (profilePhone != null && profilePhone.replaceAll("[^0-9]", "").equals(emailPhone.replaceAll("[^0-9]", ""))) {
                                isValid = true;
                                mpPhone = emailPhone;
                            }
                        }

                        if (!isValid) {
                            tvError.setText("El email o teléfono debe coincidir con el de tu perfil");
                            tvError.setVisibility(View.VISIBLE);
                            return;
                        }

                        if (password.isEmpty()) {
                            tvError.setText("La contraseña es requerida");
                            tvError.setVisibility(View.VISIBLE);
                            return;
                        }

                        // Create Mercado Pago payment method
                        PaymentMethod paymentMethod = new PaymentMethod(user.getUid(), mpEmail, mpPhone);

                        // Save if checkbox is checked
                        boolean shouldSave = cbSave.isChecked();
                        android.util.Log.d("CarritoActivity", "Mercado Pago - Should save payment method: " + shouldSave);
                        
                        if (shouldSave) {
                            android.util.Log.d("CarritoActivity", "Saving Mercado Pago payment method before processing payment");
                            savePaymentMethod(paymentMethod, () -> {
                                android.util.Log.d("CarritoActivity", "Mercado Pago payment method save completed, processing payment");
                                processPaymentWithMethod(paymentMethod, totalCalculado);
                            });
                        } else {
                            android.util.Log.d("CarritoActivity", "Skipping Mercado Pago payment method save, processing payment directly");
                            processPaymentWithMethod(paymentMethod, totalCalculado);
                        }
                    });
                    builder.setNegativeButton("Cancelar", null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar datos del perfil", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateCardForm(String cardNumber, String cardHolder, String expiryDate, String cvv) {
        if (cardNumber.length() != 16) {
            Toast.makeText(this, "Número de tarjeta inválido", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (cardHolder.isEmpty()) {
            Toast.makeText(this, "Ingrese el nombre del titular", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (expiryDate.length() != 5) {
            Toast.makeText(this, "Fecha de expiración inválida", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (cvv.length() != 3) {
            Toast.makeText(this, "CVV inválido", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String detectCardBrand(String cardNumber) {
        if (cardNumber.startsWith("4")) {
            return "Visa";
        } else if (cardNumber.startsWith("5")) {
            return "Mastercard";
        } else if (cardNumber.startsWith("3")) {
            return "American Express";
        } else if (cardNumber.startsWith("6")) {
            return "Naranja X";
        }
        return "Desconocida";
    }

    private void setupCardFormatting(EditText etCardNumber, EditText etExpiryDate, EditText etCVV) {
        // Card number formatting
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

        // Expiry date formatting
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

        // CVV formatting
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
    }

    private void savePaymentMethod(PaymentMethod paymentMethod, Runnable onSuccess) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            android.util.Log.e("CarritoActivity", "Cannot save payment method: user is null");
            onSuccess.run();
            return;
        }

        Map<String, Object> pmData = new HashMap<>();
        pmData.put("userId", paymentMethod.getUserId());
        pmData.put("type", paymentMethod.getType());
        if (paymentMethod.getCardNumber() != null) {
            pmData.put("cardNumber", paymentMethod.getCardNumber());
            pmData.put("cardHolder", paymentMethod.getCardHolder());
            pmData.put("cardBrand", paymentMethod.getCardBrand());
            pmData.put("expiryDate", paymentMethod.getExpiryDate());
        }
        if (paymentMethod.getMercadoPagoEmail() != null) {
            pmData.put("mercadoPagoEmail", paymentMethod.getMercadoPagoEmail());
        }
        if (paymentMethod.getMercadoPagoPhone() != null) {
            pmData.put("mercadoPagoPhone", paymentMethod.getMercadoPagoPhone());
        }
        pmData.put("isDefault", paymentMethod.isDefault());
        pmData.put("createdAt", com.google.firebase.Timestamp.now());

        android.util.Log.d("CarritoActivity", "Saving payment method to: users/" + user.getUid() + "/paymentMethods");
        android.util.Log.d("CarritoActivity", "Payment method data: " + pmData.toString());

        db.collection("users").document(user.getUid()).collection("paymentMethods")
                .add(pmData)
                .addOnSuccessListener(documentReference -> {
                    paymentMethod.setId(documentReference.getId());
                    android.util.Log.d("CarritoActivity", "Payment method saved successfully with ID: " + documentReference.getId());
                    Toast.makeText(this, "Método de pago guardado exitosamente", Toast.LENGTH_SHORT).show();
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    String errorMsg = e.getMessage();
                    android.util.Log.e("CarritoActivity", "Error al guardar método de pago: " + errorMsg, e);
                    
                    // Check if it's a permission error
                    if (errorMsg != null && (errorMsg.contains("permission") || errorMsg.contains("PERMISSION_DENIED") || errorMsg.contains("Missing or insufficient permissions"))) {
                        android.util.Log.e("CarritoActivity", "⚠️ ERROR DE PERMISOS: Las reglas de Firestore no permiten escribir en paymentMethods");
                        Toast.makeText(this, "Error de permisos al guardar método de pago. Verifica las reglas de Firestore.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error al guardar método de pago: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                    
                    // Continue with payment even if save fails
                    onSuccess.run();
                });
    }

    private void updateTotalDisplay(TextView tvTotal, double baseTotal, PaymentMethod paymentMethod, String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String gender = documentSnapshot.getString("gender");
                        String birthdate = documentSnapshot.getString("birthdate");
                        
                        boolean qualifiesSenior = DiscountHelper.qualifiesForDiscount(birthdate, gender);
                        double finalTotal = DiscountHelper.applyAllDiscounts(baseTotal, qualifiesSenior, paymentMethod);
                        double totalDiscount = DiscountHelper.getTotalDiscountAmount(baseTotal, qualifiesSenior, paymentMethod);
                        
                        StringBuilder totalText = new StringBuilder();
                        totalText.append("Total: $").append(String.format(Locale.getDefault(), "%.2f", finalTotal));
                        
                        if (totalDiscount > 0) {
                            totalText.append("\nDescuentos aplicados: -$").append(String.format(Locale.getDefault(), "%.2f", totalDiscount));
                        }
                        
                        if (paymentMethod != null && DiscountHelper.supportsInstallments(paymentMethod)) {
                            totalText.append("\n6 cuotas sin interés disponibles");
                        }
                        
                        tvTotal.setText(totalText.toString());
                    } else {
                        double finalTotal = paymentMethod != null ? 
                            DiscountHelper.applyPaymentMethodDiscount(baseTotal, paymentMethod) : baseTotal;
                        tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", finalTotal));
                    }
                })
                .addOnFailureListener(e -> {
                    double finalTotal = paymentMethod != null ? 
                        DiscountHelper.applyPaymentMethodDiscount(baseTotal, paymentMethod) : baseTotal;
                    tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", finalTotal));
                });
    }

    private void processPaymentWithMethod(PaymentMethod paymentMethod, double totalCalculado) {
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

        List<Product> productosPedido = new ArrayList<>();
        for (Product p : carrito) {
            Product copia = new Product(p.getImageUrl(), p.getName(), p.getPrice());
            copia.setQuantity(p.getQuantity());
            productosPedido.add(copia);
        }

        db.collection("users").document(user.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                double finalTotal = totalCalculado;
                boolean qualifiesSenior = false;
                
                if (documentSnapshot.exists()) {
                    String gender = documentSnapshot.getString("gender");
                    String birthdate = documentSnapshot.getString("birthdate");
                    qualifiesSenior = DiscountHelper.qualifiesForDiscount(birthdate, gender);
                    finalTotal = DiscountHelper.applyAllDiscounts(totalCalculado, qualifiesSenior, paymentMethod);
                } else {
                    finalTotal = DiscountHelper.applyPaymentMethodDiscount(totalCalculado, paymentMethod);
                }

                int installments = 0;
                if (DiscountHelper.supportsInstallments(paymentMethod)) {
                    // Show dialog for installments selection
                    showInstallmentsDialog(paymentMethod, productosPedido, finalTotal, user.getUid());
                } else {
                    // Process payment without installments
                    procesarPagoConMetodo(paymentMethod, productosPedido, finalTotal, user.getUid(), 0);
                }
            })
            .addOnFailureListener(e -> {
                double finalTotal = DiscountHelper.applyPaymentMethodDiscount(totalCalculado, paymentMethod);
                procesarPagoConMetodo(paymentMethod, productosPedido, finalTotal, user.getUid(), 0);
            });
    }

    private void showInstallmentsDialog(PaymentMethod paymentMethod, List<Product> productosPedido, double total, String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Cuotas");
        builder.setMessage("Visa Crédito permite pagar en 6 cuotas sin interés. ¿Deseas usar cuotas?");
        
        String[] options = {"Pago único", "6 cuotas sin interés"};
        builder.setItems(options, (dialog, which) -> {
            int installments = which == 1 ? 6 : 0;
            procesarPagoConMetodo(paymentMethod, productosPedido, total, userId, installments);
        });
        
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void procesarPagoConMetodo(PaymentMethod paymentMethod, List<Product> productosPedido, double finalTotal, String userId, int installments) {
        Order order = new Order();
        order.setUserId(userId);
        order.setProducts(productosPedido);
        order.setTotal(finalTotal);
        order.setStatus("en_curso");
        order.setPaymentMethodId(paymentMethod.getId());
        order.setPaymentMethodType(paymentMethod.getType());
        order.setPaymentMethodBrand(paymentMethod.getCardBrand());
        order.setInstallments(installments);
        
        if (PaymentMethod.TYPE_MERCADO_PAGO.equals(paymentMethod.getType())) {
            order.setCardHolder("Mercado Pago");
            order.setCardNumber("MP");
        } else {
            order.setCardNumber(paymentMethod.getCardNumber());
            order.setCardHolder(paymentMethod.getCardHolder());
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", order.getUserId());
        orderData.put("products", productosPedido);
        orderData.put("total", order.getTotal());
        orderData.put("status", order.getStatus());
        orderData.put("cardNumber", order.getCardNumber());
        orderData.put("cardHolder", order.getCardHolder());
        orderData.put("paymentMethodId", order.getPaymentMethodId());
        orderData.put("paymentMethodType", order.getPaymentMethodType());
        orderData.put("paymentMethodBrand", order.getPaymentMethodBrand());
        orderData.put("installments", order.getInstallments());
        orderData.put("createdAt", com.google.firebase.Timestamp.now());
        orderData.put("updatedAt", com.google.firebase.Timestamp.now());

        btnPagar.setEnabled(false);
        btnPagar.setText("Procesando...");

        db.collection("users").document(userId).collection("orders")
                .add(orderData)
                .addOnSuccessListener(documentReference -> {
                    order.setId(documentReference.getId());
                    android.util.Log.d("CarritoActivity", "Pedido guardado exitosamente: " + documentReference.getId());
                    
                    // Count keys in the order and update user's key count
                    int keysInOrder = 0;
                    for (Product product : productosPedido) {
                        if ("Llave".equals(product.getName())) {
                            keysInOrder += product.getQuantity();
                        }
                    }
                    
                    if (keysInOrder > 0) {
                        updateUserKeyCount(userId, keysInOrder);
                    }
                    
                    runOnUiThread(() -> {
                        adapter = new CarritoAdapter(CarritoActivity.this, new ArrayList<>());
                        recyclerCarrito.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", 0.0));
                        btnPagar.setEnabled(true);
                        btnPagar.setText("Pagar");
                    });
                    
                    CarritoManager.limpiarCarrito();
                    cargarCarrito();
                    
                    String message = installments > 0 ? 
                        String.format(Locale.getDefault(), "¡Pago realizado exitosamente! Total: $%.2f en %d cuotas sin interés.", finalTotal, installments) :
                        String.format(Locale.getDefault(), "¡Pago realizado exitosamente! Total: $%.2f", finalTotal);
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    
                    android.os.Handler handler = new android.os.Handler();
                    handler.postDelayed(() -> {
                        Intent intent = new Intent(CarritoActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }, 1500);
                })
                .addOnFailureListener(e -> {
                    String errorMsg = e.getMessage();
                    android.util.Log.e("CarritoActivity", "Error al guardar pedido: " + errorMsg, e);
                    
                    if (errorMsg != null && (errorMsg.contains("permission") || errorMsg.contains("PERMISSION_DENIED"))) {
                        Toast.makeText(this, "Error de permisos. Verifica las reglas de Firestore.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error al procesar el pago: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                    
                    btnPagar.setEnabled(true);
                    btnPagar.setText("Pagar");
                });
    }

    private void updateUserKeyCount(String userId, int keysToAdd) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    long currentKeys = 0;
                    if (documentSnapshot.exists()) {
                        Long keys = documentSnapshot.getLong("lootboxKeys");
                        currentKeys = keys != null ? keys : 0;
                    }
                    
                    long newKeyCount = currentKeys + keysToAdd;
                    
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("lootboxKeys", newKeyCount);
                    
                    db.collection("users").document(userId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                android.util.Log.d("CarritoActivity", "Key count updated: " + newKeyCount);
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e("CarritoActivity", "Error al actualizar cantidad de llaves: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CarritoActivity", "Error al obtener cantidad de llaves: " + e.getMessage());
                    // Try to set directly if document doesn't exist
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("lootboxKeys", keysToAdd);
                    db.collection("users").document(userId)
                            .set(updates)
                            .addOnFailureListener(e2 -> {
                                android.util.Log.e("CarritoActivity", "Error al crear cantidad de llaves: " + e2.getMessage());
                            });
                });
    }

}
