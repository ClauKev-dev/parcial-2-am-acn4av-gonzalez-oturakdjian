package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvUserPhone;
    private Button btnLogout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvUserPhone = findViewById(R.id.tv_user_phone);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
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
                    } else {
                        // Document doesn't exist, use default values
                        tvUserName.setText("Usuario");
                        tvUserPhone.setText("No disponible");
                    }
                })
                .addOnFailureListener(e -> {
                    // Error loading data, use defaults
                    tvUserName.setText(currentUser.getDisplayName() != null ? 
                            currentUser.getDisplayName() : "Usuario");
                    tvUserPhone.setText("No disponible");
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

