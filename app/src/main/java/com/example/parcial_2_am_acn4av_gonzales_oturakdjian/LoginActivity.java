package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView tvEmailError, tvPasswordError, tvSuccessMessage, tvForgotPassword;
    private Button btnLogin, btnRegisterBottom;
    private FirebaseAuth mAuth;
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupListeners();
        setupTextWatchers();

        // Pre-fill email if coming from RegisterActivity
        String email = getIntent().getStringExtra("email");
        if (email != null && !email.isEmpty()) {
            etEmail.setText(email);
        }

        // Check if user is already logged in
        checkCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in when activity starts
        checkCurrentUser();
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Cargar carrito del usuario antes de redirigir
            CarritoManager.cargarCarrito(new CarritoManager.CarritoLoadListener() {
                @Override
                public void onCarritoLoaded(List<Product> carrito) {
                    // User is already logged in, redirect to MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String error) {
                    // Aún así redirigir aunque haya error al cargar el carrito
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        tvEmailError = findViewById(R.id.tv_email_error);
        tvPasswordError = findViewById(R.id.tv_password_error);
        tvSuccessMessage = findViewById(R.id.tv_success_message);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegisterBottom = findViewById(R.id.btn_register_bottom);
    }

    private void setupListeners() {
        // Login button click (inside card)
        btnLogin.setOnClickListener(v -> {
            if (validateForm()) {
                performLogin();
            }
        });

        // Register button click (bottom)
        if (btnRegisterBottom != null) {
            btnRegisterBottom.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }

        // Forgot password click
        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Por favor ingresa tu correo electrónico", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidEmail(email)) {
                Toast.makeText(LoginActivity.this, "Por favor ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show();
                return;
            }
            sendPasswordResetEmail(email);
        });

        // Focus change listeners for dynamic background change
        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                etEmail.setBackgroundResource(R.drawable.input_background_focused);
            } else {
                etEmail.setBackgroundResource(R.drawable.input_background);
            }
        });

        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                etPassword.setBackgroundResource(R.drawable.input_background_focused);
            } else {
                etPassword.setBackgroundResource(R.drawable.input_background);
            }
        });
    }

    private void setupTextWatchers() {
        // Email text watcher - clears error when user types
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvEmailError.getVisibility() == View.VISIBLE) {
                    tvEmailError.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Password text watcher - clears error when user types
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvPasswordError.getVisibility() == View.VISIBLE) {
                    tvPasswordError.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateForm() {
        boolean isValid = true;
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Clear previous errors
        hideAllErrors();

        // Validate email
        if (email.isEmpty()) {
            showError(tvEmailError, getString(R.string.error_email_required));
            isValid = false;
        } else if (!isValidEmail(email)) {
            showError(tvEmailError, getString(R.string.error_email_invalid));
            isValid = false;
        }

        // Validate password
        if (password.isEmpty()) {
            showError(tvPasswordError, getString(R.string.error_password_required));
            isValid = false;
        } else if (password.length() < 6) {
            showError(tvPasswordError, getString(R.string.error_password_short));
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        return pattern.matcher(email).matches();
    }

    private void showError(TextView errorView, String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    private void hideAllErrors() {
        tvEmailError.setVisibility(View.GONE);
        tvPasswordError.setVisibility(View.GONE);
        tvSuccessMessage.setVisibility(View.GONE);
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Disable button during login
        btnLogin.setEnabled(false);
        btnLogin.setText("Iniciando sesión...");

        // Sign in with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Show success message
                            tvSuccessMessage.setText(getString(R.string.success_login));
                            tvSuccessMessage.setVisibility(View.VISIBLE);

                            // Cargar carrito del usuario antes de navegar
                            CarritoManager.cargarCarrito(new CarritoManager.CarritoLoadListener() {
                                @Override
                                public void onCarritoLoaded(List<Product> carrito) {
                                    // Navigate to MainActivity after successful login
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onError(String error) {
                                    // Aún así navegar aunque haya error al cargar el carrito
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    } else {
                        // Login failed
                        btnLogin.setEnabled(true);
                        btnLogin.setText(getString(R.string.login_button));
                        
                        String errorMessage = "Error al iniciar sesión";
                        if (task.getException() != null) {
                            String errorCode = task.getException().getMessage();
                            if (errorCode != null) {
                                if (errorCode.contains("user-not-found")) {
                                    errorMessage = "No existe una cuenta con este correo electrónico";
                                    showError(tvEmailError, errorMessage);
                                } else if (errorCode.contains("wrong-password")) {
                                    errorMessage = "Contraseña incorrecta";
                                    showError(tvPasswordError, errorMessage);
                                } else if (errorCode.contains("invalid-email")) {
                                    errorMessage = "Correo electrónico inválido";
                                    showError(tvEmailError, errorMessage);
                                } else if (errorCode.contains("too-many-requests")) {
                                    errorMessage = "Demasiados intentos fallidos. Intenta más tarde";
                                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                } else {
                                    errorMessage = "Error: " + errorCode;
                                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Se ha enviado un correo para restablecer tu contraseña", Toast.LENGTH_LONG).show();
                    } else {
                        String errorMessage = "Error al enviar el correo";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

