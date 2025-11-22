package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private TextView tvNameError, tvEmailError, tvPhoneError, tvPasswordError, tvConfirmPasswordError, tvSuccessMessage, tvLoginLink;
    private Button btnRegister;
    private ScrollView scrollView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PHONE_PATTERN = "^[0-9]{10,15}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupListeners();
        setupTextWatchers();
    }

    private void initializeViews() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        tvNameError = findViewById(R.id.tv_name_error);
        tvEmailError = findViewById(R.id.tv_email_error);
        tvPhoneError = findViewById(R.id.tv_phone_error);
        tvPasswordError = findViewById(R.id.tv_password_error);
        tvConfirmPasswordError = findViewById(R.id.tv_confirm_password_error);
        tvSuccessMessage = findViewById(R.id.tv_success_message);
        tvLoginLink = findViewById(R.id.tv_login_link);
        btnRegister = findViewById(R.id.btn_register);
        scrollView = findViewById(R.id.scroll_view);
    }

    private void setupListeners() {
        // Register button click
        btnRegister.setOnClickListener(v -> {
            if (validateForm()) {
                performRegister();
            }
        });

        // Login link click
        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Focus change listeners for dynamic background change
        setupFocusListeners();
    }

    private void setupFocusListeners() {
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                ((EditText) v).setBackgroundResource(R.drawable.input_background_focused);
            } else {
                ((EditText) v).setBackgroundResource(R.drawable.input_background);
            }
        };

        etName.setOnFocusChangeListener(focusListener);
        etEmail.setOnFocusChangeListener(focusListener);
        etPhone.setOnFocusChangeListener(focusListener);
        etPassword.setOnFocusChangeListener(focusListener);
        etConfirmPassword.setOnFocusChangeListener(focusListener);
    }

    private void setupTextWatchers() {
        // Name text watcher
        etName.addTextChangedListener(createTextWatcher(tvNameError));

        // Email text watcher
        etEmail.addTextChangedListener(createTextWatcher(tvEmailError));

        // Phone text watcher
        etPhone.addTextChangedListener(createTextWatcher(tvPhoneError));

        // Password text watcher
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvPasswordError.getVisibility() == View.VISIBLE) {
                    tvPasswordError.setVisibility(View.GONE);
                }
                // Also check confirm password match dynamically
                checkPasswordMatch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Confirm password text watcher
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvConfirmPasswordError.getVisibility() == View.VISIBLE) {
                    tvConfirmPasswordError.setVisibility(View.GONE);
                }
                // Check password match dynamically
                checkPasswordMatch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private TextWatcher createTextWatcher(TextView errorView) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (errorView.getVisibility() == View.VISIBLE) {
                    errorView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
    }

    private void checkPasswordMatch() {
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (!password.isEmpty() && !confirmPassword.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                tvConfirmPasswordError.setText(getString(R.string.error_passwords_not_match));
                tvConfirmPasswordError.setVisibility(View.VISIBLE);
            } else {
                tvConfirmPasswordError.setVisibility(View.GONE);
            }
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Clear previous errors
        hideAllErrors();

        // Validate name
        if (name.isEmpty()) {
            showError(tvNameError, getString(R.string.error_name_required));
            scrollToView(etName);
            isValid = false;
        }

        // Validate email
        if (email.isEmpty()) {
            showError(tvEmailError, getString(R.string.error_email_required));
            if (isValid) scrollToView(etEmail);
            isValid = false;
        } else if (!isValidEmail(email)) {
            showError(tvEmailError, getString(R.string.error_email_invalid));
            if (isValid) scrollToView(etEmail);
            isValid = false;
        }

        // Validate phone
        if (phone.isEmpty()) {
            showError(tvPhoneError, getString(R.string.error_phone_required));
            if (isValid) scrollToView(etPhone);
            isValid = false;
        } else if (!isValidPhone(phone)) {
            showError(tvPhoneError, getString(R.string.error_phone_invalid));
            if (isValid) scrollToView(etPhone);
            isValid = false;
        }

        // Validate password
        if (password.isEmpty()) {
            showError(tvPasswordError, getString(R.string.error_password_required));
            if (isValid) scrollToView(etPassword);
            isValid = false;
        } else if (password.length() < 6) {
            showError(tvPasswordError, getString(R.string.error_password_short));
            if (isValid) scrollToView(etPassword);
            isValid = false;
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            showError(tvConfirmPasswordError, getString(R.string.error_password_required));
            if (isValid) scrollToView(etConfirmPassword);
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            showError(tvConfirmPasswordError, getString(R.string.error_passwords_not_match));
            if (isValid) scrollToView(etConfirmPassword);
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        return pattern.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        Pattern pattern = Pattern.compile(PHONE_PATTERN);
        return pattern.matcher(phone).matches();
    }

    private void showError(TextView errorView, String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    private void hideAllErrors() {
        tvNameError.setVisibility(View.GONE);
        tvEmailError.setVisibility(View.GONE);
        tvPhoneError.setVisibility(View.GONE);
        tvPasswordError.setVisibility(View.GONE);
        tvConfirmPasswordError.setVisibility(View.GONE);
        tvSuccessMessage.setVisibility(View.GONE);
    }

    private void scrollToView(View view) {
        scrollView.post(() -> {
            int scrollY = view.getTop();
            scrollView.smoothScrollTo(0, scrollY);
        });
    }

    private void performRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Disable button during registration
        btnRegister.setEnabled(false);
        btnRegister.setText("Registrando...");

        // Create user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save additional user data to Firestore
                            saveUserDataToFirestore(user.getUid(), name, email, phone);
                        }
                    } else {
                        // Registration failed
                        btnRegister.setEnabled(true);
                        btnRegister.setText(getString(R.string.register_button));

                        String errorMessage = "Error al registrar usuario";
                        if (task.getException() != null) {
                            String errorCode = task.getException().getMessage();
                            if (errorCode != null) {
                                if (errorCode.contains("email-already-in-use")) {
                                    errorMessage = "Este correo electrónico ya está registrado";
                                    showError(tvEmailError, errorMessage);
                                } else if (errorCode.contains("invalid-email")) {
                                    errorMessage = "Correo electrónico inválido";
                                    showError(tvEmailError, errorMessage);
                                } else if (errorCode.contains("weak-password")) {
                                    errorMessage = "La contraseña es muy débil";
                                    showError(tvPasswordError, errorMessage);
                                } else {
                                    errorMessage = "Error: " + errorCode;
                                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserDataToFirestore(String userId, String name, String email, String phone) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("createdAt", com.google.firebase.Timestamp.now());

        // Save user data to Firestore
        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Show success message
                    tvSuccessMessage.setText(getString(R.string.success_register));
                    tvSuccessMessage.setVisibility(View.VISIBLE);

                    // Scroll to success message
                    scrollView.post(() -> {
                        int scrollY = tvSuccessMessage.getTop();
                        scrollView.smoothScrollTo(0, scrollY);
                    });

                    // Navigate to LoginActivity after successful registration
                    android.os.Handler handler = new android.os.Handler();
                    handler.postDelayed(() -> {
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    }, 2000);
                })
                .addOnFailureListener(e -> {
                    // Even if Firestore save fails, user is created in Auth
                    // Show success but log the error
                    Toast.makeText(RegisterActivity.this, "Usuario creado, pero hubo un error al guardar datos adicionales", Toast.LENGTH_SHORT).show();
                    
                    tvSuccessMessage.setText(getString(R.string.success_register));
                    tvSuccessMessage.setVisibility(View.VISIBLE);

                    scrollView.post(() -> {
                        int scrollY = tvSuccessMessage.getTop();
                        scrollView.smoothScrollTo(0, scrollY);
                    });

                    android.os.Handler handler = new android.os.Handler();
                    handler.postDelayed(() -> {
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    }, 2000);
                });
    }
}

