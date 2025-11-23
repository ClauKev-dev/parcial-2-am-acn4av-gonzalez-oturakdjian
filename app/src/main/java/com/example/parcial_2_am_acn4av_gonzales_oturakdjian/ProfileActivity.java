package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvUserPhone, tvUserDni, tvUserAddress;
    private EditText etUserName, etUserEmail, etUserPhone, etUserDni, etUserAddress;
    private Button btnLogout, btnEditProfile, btnSaveProfile, btnCancelEdit;
    private LinearLayout llEditButtons;
    private ScrollView scrollView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isEditMode = false;
    private boolean isSaving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupListeners();
        loadUserData();
    }

    private void initializeViews() {
        try {
            tvUserName = findViewById(R.id.tv_user_name);
            tvUserEmail = findViewById(R.id.tv_user_email);
            tvUserPhone = findViewById(R.id.tv_user_phone);
            tvUserDni = findViewById(R.id.tv_user_dni);
            tvUserAddress = findViewById(R.id.tv_user_address);
            etUserName = findViewById(R.id.et_user_name);
            etUserEmail = findViewById(R.id.et_user_email);
            etUserPhone = findViewById(R.id.et_user_phone);
            etUserDni = findViewById(R.id.et_user_dni);
            etUserAddress = findViewById(R.id.et_user_address);
            btnLogout = findViewById(R.id.btn_logout);
            btnEditProfile = findViewById(R.id.btn_edit_profile);
            btnSaveProfile = findViewById(R.id.btn_save_profile);
            btnCancelEdit = findViewById(R.id.btn_cancel_edit);
            llEditButtons = findViewById(R.id.ll_edit_buttons);
            scrollView = findViewById(R.id.scroll_view);
        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "Error initializing views", e);
            Toast.makeText(this, "Error al cargar la pantalla de perfil", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        btnEditProfile.setOnClickListener(v -> enterEditMode());
        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
        btnCancelEdit.setOnClickListener(v -> exitEditMode());
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        if (currentUser == null) {
            // User not logged in, redirect to login
            redirectToLogin();
            return;
        }

        // Set email from Auth
        tvUserEmail.setText(currentUser.getEmail());

        // Load additional user data from Firestore
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phone");
                        String dni = documentSnapshot.getString("dni");
                        String address = documentSnapshot.getString("address");

                        if (name != null && !name.isEmpty()) {
                            tvUserName.setText(name);
                        } else {
                            tvUserName.setText("No disponible");
                        }

                        if (phone != null && !phone.isEmpty()) {
                            tvUserPhone.setText(phone);
                        } else {
                            tvUserPhone.setText("No disponible");
                        }

                        if (dni != null && !dni.isEmpty()) {
                            tvUserDni.setText(dni);
                        } else {
                            tvUserDni.setText("No disponible");
                        }

                        if (address != null && !address.isEmpty()) {
                            tvUserAddress.setText(address);
                        } else {
                            tvUserAddress.setText("No disponible");
                        }
                    } else {
                        // Document doesn't exist, use default values
                        tvUserName.setText("Usuario");
                        tvUserPhone.setText("No disponible");
                        tvUserDni.setText("No disponible");
                        tvUserAddress.setText("No disponible");
                    }
                })
                .addOnFailureListener(e -> {
                    // Error loading data, use defaults
                    tvUserName.setText(currentUser.getDisplayName() != null ? 
                            currentUser.getDisplayName() : "Usuario");
                    tvUserPhone.setText("No disponible");
                    tvUserDni.setText("No disponible");
                    tvUserAddress.setText("No disponible");
                    Toast.makeText(this, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage(getString(R.string.logout_confirmation))
                .setPositiveButton("Sí", (dialog, which) -> performLogout())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void performLogout() {
        // Disable button during logout
        btnLogout.setEnabled(false);
        btnLogout.setText("Cerrando sesión...");

        // Sign out from Firebase
        mAuth.signOut();

        // Show success message
        Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show();

        // Redirect to LoginActivity
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void enterEditMode() {
        if (tvUserName == null || etUserName == null) {
            Toast.makeText(this, "Error: vistas no inicializadas", Toast.LENGTH_SHORT).show();
            return;
        }
        
        isEditMode = true;
        
        // Hide TextViews and show EditTexts
        tvUserName.setVisibility(View.GONE);
        tvUserEmail.setVisibility(View.GONE);
        tvUserPhone.setVisibility(View.GONE);
        tvUserDni.setVisibility(View.GONE);
        tvUserAddress.setVisibility(View.GONE);
        
        etUserName.setVisibility(View.VISIBLE);
        etUserEmail.setVisibility(View.VISIBLE);
        etUserPhone.setVisibility(View.VISIBLE);
        etUserDni.setVisibility(View.VISIBLE);
        etUserAddress.setVisibility(View.VISIBLE);
        
        // Populate EditTexts with current values
        etUserName.setText(tvUserName.getText().toString());
        etUserEmail.setText(tvUserEmail.getText().toString());
        etUserPhone.setText(tvUserPhone.getText().toString());
        etUserDni.setText(tvUserDni.getText().toString());
        etUserAddress.setText(tvUserAddress.getText().toString());
        
        // Show save/cancel buttons, hide edit button
        if (btnEditProfile != null) {
            btnEditProfile.setVisibility(View.GONE);
        }
        if (llEditButtons != null) {
            llEditButtons.setVisibility(View.VISIBLE);
        }
    }

    private void exitEditMode() {
        isEditMode = false;
        
        // Show TextViews and hide EditTexts
        if (tvUserName != null) tvUserName.setVisibility(View.VISIBLE);
        if (tvUserEmail != null) tvUserEmail.setVisibility(View.VISIBLE);
        if (tvUserPhone != null) tvUserPhone.setVisibility(View.VISIBLE);
        if (tvUserDni != null) tvUserDni.setVisibility(View.VISIBLE);
        if (tvUserAddress != null) tvUserAddress.setVisibility(View.VISIBLE);
        
        if (etUserName != null) etUserName.setVisibility(View.GONE);
        if (etUserEmail != null) etUserEmail.setVisibility(View.GONE);
        if (etUserPhone != null) etUserPhone.setVisibility(View.GONE);
        if (etUserDni != null) etUserDni.setVisibility(View.GONE);
        if (etUserAddress != null) etUserAddress.setVisibility(View.GONE);
        
        // Show edit button, hide save/cancel buttons
        if (btnEditProfile != null) {
            btnEditProfile.setVisibility(View.VISIBLE);
        }
        if (llEditButtons != null) {
            llEditButtons.setVisibility(View.GONE);
        }
        
        // Ensure save button is enabled for next time
        if (btnSaveProfile != null) {
            btnSaveProfile.setEnabled(true);
        }
    }

    private void saveProfileChanges() {
        // Prevent multiple simultaneous save operations
        if (isSaving) {
            return;
        }
        
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }
        
        isSaving = true;

        String name = etUserName.getText().toString().trim();
        String email = etUserEmail.getText().toString().trim();
        String phone = etUserPhone.getText().toString().trim();
        String dni = etUserDni.getText().toString().trim();
        String address = etUserAddress.getText().toString().trim();

        // Basic validation
        if (name.isEmpty()) {
            Toast.makeText(this, "El nombre es requerido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (email.isEmpty()) {
            Toast.makeText(this, "El correo electrónico es requerido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.isEmpty()) {
            Toast.makeText(this, "El teléfono es requerido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dni.isEmpty()) {
            Toast.makeText(this, "El DNI es requerido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (address.isEmpty()) {
            Toast.makeText(this, "La dirección es requerida", Toast.LENGTH_SHORT).show();
            return;
        }

        // CRITICAL: Ensure buttons container stays visible during save
        if (llEditButtons == null) {
            Toast.makeText(this, "Error: botones no inicializados", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Force visibility of the container and buttons
        llEditButtons.setVisibility(View.VISIBLE);
        llEditButtons.setAlpha(1.0f);
        
        // Disable button during save but keep it visible and ensure it's in the layout
        if (btnSaveProfile == null) {
            Toast.makeText(this, "Error: botón guardar no inicializado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnSaveProfile.setVisibility(View.VISIBLE);
        btnSaveProfile.setAlpha(1.0f);
        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Guardando...");
        
        // Disable cancel button during save but keep it visible
        if (btnCancelEdit != null) {
            btnCancelEdit.setEnabled(false);
            btnCancelEdit.setVisibility(View.VISIBLE);
        }

        // Update Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("dni", dni);
        updates.put("address", address);

        db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Firebase callbacks run on main thread, so we can update UI directly
                    // Update TextViews with new values
                    if (tvUserName != null) tvUserName.setText(name);
                    if (tvUserEmail != null) tvUserEmail.setText(email);
                    if (tvUserPhone != null) tvUserPhone.setText(phone);
                    if (tvUserDni != null) tvUserDni.setText(dni);
                    if (tvUserAddress != null) tvUserAddress.setText(address);

                    // Show success message first
                    Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
                    
                    // Reset saving flag
                    isSaving = false;
                    
                    // Small delay to show the success message, then exit edit mode
                    new android.os.Handler().postDelayed(() -> {
                        // Exit edit mode (this will show edit button and hide save/cancel buttons)
                        exitEditMode();
                    }, 500);
                })
                .addOnFailureListener(e -> {
                    // Reset saving flag
                    isSaving = false;
                    
                    // Re-enable buttons on failure
                    if (btnSaveProfile != null) {
                        btnSaveProfile.setEnabled(true);
                        btnSaveProfile.setText(getString(R.string.save_profile));
                        btnSaveProfile.setVisibility(View.VISIBLE);
                    }
                    if (btnCancelEdit != null) {
                        btnCancelEdit.setEnabled(true);
                        btnCancelEdit.setVisibility(View.VISIBLE);
                    }
                    // Ensure container is still visible
                    if (llEditButtons != null) {
                        llEditButtons.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(this, "Error al actualizar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is still logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
        }
    }
}

