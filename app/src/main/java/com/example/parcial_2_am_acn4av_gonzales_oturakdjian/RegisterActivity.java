package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etDni, etAddress, etEmail, etPhone, etPassword, etConfirmPassword, etBirthdate;
    private TextView tvNameError, tvDniError, tvAddressError, tvEmailError, tvPhoneError, tvPasswordError, tvConfirmPasswordError, tvGenderError, tvBirthdateError, tvSuccessMessage;
    private Spinner spinnerGender;
    private Button btnRegister, btnLoginBottom;
    private ScrollView scrollView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PHONE_PATTERN = "^[0-9]{10,15}$";
    private static final String DNI_PATTERN = "^[0-9]{7,8}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupListeners();
        setupTextWatchers();
    }

    private void initializeViews() {
        etName = findViewById(R.id.et_name);
        etDni = findViewById(R.id.et_dni);
        etAddress = findViewById(R.id.et_address);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etBirthdate = findViewById(R.id.et_birthdate);
        spinnerGender = findViewById(R.id.spinner_gender);
        tvNameError = findViewById(R.id.tv_name_error);
        tvDniError = findViewById(R.id.tv_dni_error);
        tvAddressError = findViewById(R.id.tv_address_error);
        tvEmailError = findViewById(R.id.tv_email_error);
        tvPhoneError = findViewById(R.id.tv_phone_error);
        tvPasswordError = findViewById(R.id.tv_password_error);
        tvConfirmPasswordError = findViewById(R.id.tv_confirm_password_error);
        tvGenderError = findViewById(R.id.tv_gender_error);
        tvBirthdateError = findViewById(R.id.tv_birthdate_error);
        tvSuccessMessage = findViewById(R.id.tv_success_message);
        btnRegister = findViewById(R.id.btn_register);
        btnLoginBottom = findViewById(R.id.btn_login_bottom);
        scrollView = findViewById(R.id.scroll_view);

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
            this, R.array.gender_options, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        etBirthdate.setOnClickListener(v -> showDatePicker());
    }
    
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, selectedYear, selectedMonth, selectedDay) -> {
                String dateString = String.format(Locale.getDefault(), 
                    "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                etBirthdate.setText(dateString);
                tvBirthdateError.setVisibility(View.GONE);
            },
            year, month, day
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> {
            if (validateForm()) {
                performRegister();
            }
        });

        if (btnLoginBottom != null) {
            btnLoginBottom.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }

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
        etDni.setOnFocusChangeListener(focusListener);
        etAddress.setOnFocusChangeListener(focusListener);
        etEmail.setOnFocusChangeListener(focusListener);
        etPhone.setOnFocusChangeListener(focusListener);
        etPassword.setOnFocusChangeListener(focusListener);
        etConfirmPassword.setOnFocusChangeListener(focusListener);
    }

    private void setupTextWatchers() {
        etName.addTextChangedListener(createTextWatcher(tvNameError));
        etDni.addTextChangedListener(createTextWatcher(tvDniError));
        etAddress.addTextChangedListener(createTextWatcher(tvAddressError));
        etEmail.addTextChangedListener(createTextWatcher(tvEmailError));
        etPhone.addTextChangedListener(createTextWatcher(tvPhoneError));
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvPasswordError.getVisibility() == View.VISIBLE) {
                    tvPasswordError.setVisibility(View.GONE);
                }
                checkPasswordMatch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvConfirmPasswordError.getVisibility() == View.VISIBLE) {
                    tvConfirmPasswordError.setVisibility(View.GONE);
                }
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
        String dni = etDni.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        hideAllErrors();

        if (name.isEmpty()) {
            showError(tvNameError, getString(R.string.error_name_required));
            scrollToView(etName);
            isValid = false;
        }

        if (dni.isEmpty()) {
            showError(tvDniError, getString(R.string.error_dni_required));
            if (isValid) scrollToView(etDni);
            isValid = false;
        } else if (!isValidDni(dni)) {
            showError(tvDniError, getString(R.string.error_dni_invalid));
            if (isValid) scrollToView(etDni);
            isValid = false;
        }

        if (address.isEmpty()) {
            showError(tvAddressError, getString(R.string.error_address_required));
            if (isValid) scrollToView(etAddress);
            isValid = false;
        }

        if (email.isEmpty()) {
            showError(tvEmailError, getString(R.string.error_email_required));
            if (isValid) scrollToView(etEmail);
            isValid = false;
        } else if (!isValidEmail(email)) {
            showError(tvEmailError, getString(R.string.error_email_invalid));
            if (isValid) scrollToView(etEmail);
            isValid = false;
        }

        if (phone.isEmpty()) {
            showError(tvPhoneError, getString(R.string.error_phone_required));
            if (isValid) scrollToView(etPhone);
            isValid = false;
        } else if (!isValidPhone(phone)) {
            showError(tvPhoneError, getString(R.string.error_phone_invalid));
            if (isValid) scrollToView(etPhone);
            isValid = false;
        }

        if (password.isEmpty()) {
            showError(tvPasswordError, getString(R.string.error_password_required));
            if (isValid) scrollToView(etPassword);
            isValid = false;
        } else if (password.length() < 6) {
            showError(tvPasswordError, getString(R.string.error_password_short));
            if (isValid) scrollToView(etPassword);
            isValid = false;
        }

        if (confirmPassword.isEmpty()) {
            showError(tvConfirmPasswordError, getString(R.string.error_password_required));
            if (isValid) scrollToView(etConfirmPassword);
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            showError(tvConfirmPasswordError, getString(R.string.error_passwords_not_match));
            if (isValid) scrollToView(etConfirmPassword);
            isValid = false;
        }

        String gender = spinnerGender.getSelectedItem().toString();
        if (gender.isEmpty() || gender.equals("Seleccionar")) {
            showError(tvGenderError, "Por favor selecciona un género");
            if (isValid) scrollToView(spinnerGender);
            isValid = false;
        }

        String birthdate = etBirthdate.getText().toString().trim();
        if (birthdate.isEmpty()) {
            showError(tvBirthdateError, "La fecha de nacimiento es requerida");
            if (isValid) scrollToView(etBirthdate);
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

    private boolean isValidDni(String dni) {
        Pattern pattern = Pattern.compile(DNI_PATTERN);
        return pattern.matcher(dni).matches();
    }

    private void showError(TextView errorView, String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    private void hideAllErrors() {
        tvNameError.setVisibility(View.GONE);
        tvDniError.setVisibility(View.GONE);
        tvAddressError.setVisibility(View.GONE);
        tvEmailError.setVisibility(View.GONE);
        tvPhoneError.setVisibility(View.GONE);
        tvPasswordError.setVisibility(View.GONE);
        tvConfirmPasswordError.setVisibility(View.GONE);
        tvGenderError.setVisibility(View.GONE);
        tvBirthdateError.setVisibility(View.GONE);
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
        String dni = etDni.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String birthdate = etBirthdate.getText().toString().trim();

        btnRegister.setEnabled(false);
        btnRegister.setText("Registrando...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {

                            saveUserDataToFirestore(user.getUid(), name, dni, address, email, phone, gender, birthdate);

                            CarritoManager.crearCarritoVacio(user.getUid());
                        } else {

                            android.util.Log.w("RegisterActivity", "User created but user object is null");
                            redirectToLogin(email);
                        }
                    } else {

                        btnRegister.setEnabled(true);
                        btnRegister.setText(getString(R.string.register_button));

                        String errorMessage = "Error al registrar usuario";
                        Exception exception = task.getException();
                        if (exception != null) {
                            String errorCode = exception.getMessage();
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
                                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }

                            android.util.Log.e("RegisterActivity", "Registration failed", exception);
                        } else {
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserDataToFirestore(String userId, String name, String dni, String address, String email, String phone, String gender, String birthdate) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("dni", dni);
        userData.put("address", address);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("gender", gender);
        userData.put("birthdate", birthdate);
        userData.put("createdAt", com.google.firebase.Timestamp.now());

        android.os.Handler timeoutHandler = new android.os.Handler();
        Runnable timeoutRunnable = () -> {
            redirectToLogin(email);
        };
        timeoutHandler.postDelayed(timeoutRunnable, 5000);

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    timeoutHandler.removeCallbacks(timeoutRunnable);

                    tvSuccessMessage.setText(getString(R.string.success_register));
                    tvSuccessMessage.setVisibility(View.VISIBLE);

                    scrollView.post(() -> {
                        int scrollY = tvSuccessMessage.getTop();
                        scrollView.smoothScrollTo(0, scrollY);
                    });

                    redirectToLogin(email);
                })
                .addOnFailureListener(e -> {
                    timeoutHandler.removeCallbacks(timeoutRunnable);

                    android.util.Log.e("RegisterActivity", "Error saving to Firestore: " + e.getMessage(), e);

                    tvSuccessMessage.setText(getString(R.string.success_register));
                    tvSuccessMessage.setVisibility(View.VISIBLE);

                    scrollView.post(() -> {
                        int scrollY = tvSuccessMessage.getTop();
                        scrollView.smoothScrollTo(0, scrollY);
                    });

                    redirectToLogin(email);
                });
    }

    private void redirectToLogin(String email) {
        btnRegister.setEnabled(true);
        btnRegister.setText(getString(R.string.register_button));
        
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            finish();
        }, 1500);
    }
}

