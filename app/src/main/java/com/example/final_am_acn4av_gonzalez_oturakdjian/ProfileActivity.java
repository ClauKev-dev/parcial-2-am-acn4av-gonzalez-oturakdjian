package com.example.final_am_acn4av_gonzalez_oturakdjian;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvUserPhone, tvUserDni, tvUserAddress, tvUserGender, tvUserBirthdate;
    private EditText etUserName, etUserEmail, etUserPhone, etUserDni, etUserAddress, etUserBirthdate;
    private Spinner spinnerUserGender;
    private Button btnLogout, btnEditProfile, btnSaveProfile, btnCancelEdit;
    private LinearLayout llEditButtons;
    private ScrollView scrollView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isEditMode = false;
    private boolean isSaving = false;
    private ArrayAdapter<CharSequence> genderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupListeners();
        setupBottomNavigation();
        setupTopNavigation();
        setupDrawer();
        navigateToTab(4);
        loadUserData();
    }

    private void initializeViews() {
        try {
            tvUserName = findViewById(R.id.tv_user_name);
            tvUserEmail = findViewById(R.id.tv_user_email);
            tvUserPhone = findViewById(R.id.tv_user_phone);
            tvUserDni = findViewById(R.id.tv_user_dni);
            tvUserAddress = findViewById(R.id.tv_user_address);
            tvUserGender = findViewById(R.id.tv_user_gender);
            tvUserBirthdate = findViewById(R.id.tv_user_birthdate);
            etUserName = findViewById(R.id.et_user_name);
            etUserEmail = findViewById(R.id.et_user_email);
            etUserPhone = findViewById(R.id.et_user_phone);
            etUserDni = findViewById(R.id.et_user_dni);
            etUserAddress = findViewById(R.id.et_user_address);
            etUserBirthdate = findViewById(R.id.et_user_birthdate);
            spinnerUserGender = findViewById(R.id.spinner_user_gender);
            btnLogout = findViewById(R.id.btn_logout);
            btnEditProfile = findViewById(R.id.btn_edit_profile);
            btnSaveProfile = findViewById(R.id.btn_save_profile);
            btnCancelEdit = findViewById(R.id.btn_cancel_edit);
            llEditButtons = findViewById(R.id.ll_edit_buttons);
            scrollView = findViewById(R.id.scroll_view);

            genderAdapter = ArrayAdapter.createFromResource(
                this, R.array.gender_options, android.R.layout.simple_spinner_item);
            genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerUserGender.setAdapter(genderAdapter);

            etUserBirthdate.setOnClickListener(v -> showDatePicker());
        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "Error initializing views", e);
            Toast.makeText(this, "Error al cargar la pantalla de perfil", Toast.LENGTH_SHORT).show();
            finish();
        }
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
                etUserBirthdate.setText(dateString);
            },
            year, month, day
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
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
            redirectToLogin();
            return;
        }

        tvUserEmail.setText(currentUser.getEmail());

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
                        
                        String gender = documentSnapshot.getString("gender");
                        String birthdate = documentSnapshot.getString("birthdate");
                        
                        if (gender != null && !gender.isEmpty()) {
                            tvUserGender.setText(gender);
                        } else {
                            tvUserGender.setText("No disponible");
                        }
                        
                        if (birthdate != null && !birthdate.isEmpty()) {
                            tvUserBirthdate.setText(birthdate);
                        } else {
                            tvUserBirthdate.setText("No disponible");
                        }
                    } else {
                        // Document doesn't exist, use default values
                        tvUserName.setText("Usuario");
                        tvUserPhone.setText("No disponible");
                        tvUserDni.setText("No disponible");
                        tvUserAddress.setText("No disponible");
                        tvUserGender.setText("No disponible");
                        tvUserBirthdate.setText("No disponible");
                    }
                })
                .addOnFailureListener(e -> {
                    tvUserName.setText(currentUser.getDisplayName() != null ? 
                            currentUser.getDisplayName() : "Usuario");
                    tvUserPhone.setText("No disponible");
                    tvUserDni.setText("No disponible");
                    tvUserAddress.setText("No disponible");
                    tvUserGender.setText("No disponible");
                    tvUserBirthdate.setText("No disponible");
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
        btnLogout.setEnabled(false);
        btnLogout.setText("Cerrando sesión...");

        CarritoManager.limpiarCarrito();

        mAuth.signOut();

        Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show();

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

        tvUserName.setVisibility(View.GONE);
        tvUserEmail.setVisibility(View.GONE);
        tvUserPhone.setVisibility(View.GONE);
        tvUserDni.setVisibility(View.GONE);
        tvUserAddress.setVisibility(View.GONE);
        tvUserGender.setVisibility(View.GONE);
        tvUserBirthdate.setVisibility(View.GONE);
        
        etUserName.setVisibility(View.VISIBLE);
        etUserEmail.setVisibility(View.VISIBLE);
        etUserPhone.setVisibility(View.VISIBLE);
        etUserDni.setVisibility(View.VISIBLE);
        etUserAddress.setVisibility(View.VISIBLE);
        spinnerUserGender.setVisibility(View.VISIBLE);
        etUserBirthdate.setVisibility(View.VISIBLE);

        etUserName.setText(tvUserName.getText().toString());
        etUserEmail.setText(tvUserEmail.getText().toString());
        etUserPhone.setText(tvUserPhone.getText().toString());
        etUserDni.setText(tvUserDni.getText().toString());
        etUserAddress.setText(tvUserAddress.getText().toString());

        String currentGender = tvUserGender.getText().toString();
        if (!currentGender.equals("No disponible") && !currentGender.isEmpty()) {
            int position = genderAdapter.getPosition(currentGender);
            if (position >= 0) {
                spinnerUserGender.setSelection(position);
            }
        }

        String currentBirthdate = tvUserBirthdate.getText().toString();
        if (!currentBirthdate.equals("No disponible") && !currentBirthdate.isEmpty()) {
            etUserBirthdate.setText(currentBirthdate);
        }

        if (btnEditProfile != null) {
            btnEditProfile.setVisibility(View.GONE);
        }
        if (llEditButtons != null) {
            llEditButtons.setVisibility(View.VISIBLE);
        }
    }

    private void exitEditMode() {
        isEditMode = false;

        if (tvUserName != null) tvUserName.setVisibility(View.VISIBLE);
        if (tvUserEmail != null) tvUserEmail.setVisibility(View.VISIBLE);
        if (tvUserPhone != null) tvUserPhone.setVisibility(View.VISIBLE);
        if (tvUserDni != null) tvUserDni.setVisibility(View.VISIBLE);
        if (tvUserAddress != null) tvUserAddress.setVisibility(View.VISIBLE);
        if (tvUserGender != null) tvUserGender.setVisibility(View.VISIBLE);
        if (tvUserBirthdate != null) tvUserBirthdate.setVisibility(View.VISIBLE);
        
        if (etUserName != null) etUserName.setVisibility(View.GONE);
        if (etUserEmail != null) etUserEmail.setVisibility(View.GONE);
        if (etUserPhone != null) etUserPhone.setVisibility(View.GONE);
        if (etUserDni != null) etUserDni.setVisibility(View.GONE);
        if (etUserAddress != null) etUserAddress.setVisibility(View.GONE);
        if (spinnerUserGender != null) spinnerUserGender.setVisibility(View.GONE);
        if (etUserBirthdate != null) etUserBirthdate.setVisibility(View.GONE);

        if (btnEditProfile != null) {
            btnEditProfile.setVisibility(View.VISIBLE);
        }
        if (llEditButtons != null) {
            llEditButtons.setVisibility(View.GONE);
        }

        if (btnSaveProfile != null) {
            btnSaveProfile.setEnabled(true);
        }
    }

    private void saveProfileChanges() {
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
        String gender = spinnerUserGender.getSelectedItem().toString();
        String birthdate = etUserBirthdate.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "El nombre es requerido", Toast.LENGTH_SHORT).show();
            isSaving = false;
            return;
        }
        if (email.isEmpty()) {
            Toast.makeText(this, "El correo electrónico es requerido", Toast.LENGTH_SHORT).show();
            isSaving = false;
            return;
        }
        if (phone.isEmpty()) {
            Toast.makeText(this, "El teléfono es requerido", Toast.LENGTH_SHORT).show();
            isSaving = false;
            return;
        }
        if (dni.isEmpty()) {
            Toast.makeText(this, "El DNI es requerido", Toast.LENGTH_SHORT).show();
            isSaving = false;
            return;
        }
        if (address.isEmpty()) {
            Toast.makeText(this, "La dirección es requerida", Toast.LENGTH_SHORT).show();
            isSaving = false;
            return;
        }
        if (gender.isEmpty() || gender.equals("Seleccionar")) {
            Toast.makeText(this, "Por favor selecciona un género", Toast.LENGTH_SHORT).show();
            isSaving = false;
            return;
        }
        if (birthdate.isEmpty()) {
            Toast.makeText(this, "La fecha de nacimiento es requerida", Toast.LENGTH_SHORT).show();
            isSaving = false;
            return;
        }

        if (llEditButtons == null) {
            Toast.makeText(this, "Error: botones no inicializados", Toast.LENGTH_SHORT).show();
            return;
        }

        llEditButtons.setVisibility(View.VISIBLE);
        llEditButtons.setAlpha(1.0f);

        if (btnSaveProfile == null) {
            Toast.makeText(this, "Error: botón guardar no inicializado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnSaveProfile.setVisibility(View.VISIBLE);
        btnSaveProfile.setAlpha(1.0f);
        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Guardando...");

        if (btnCancelEdit != null) {
            btnCancelEdit.setEnabled(false);
            btnCancelEdit.setVisibility(View.VISIBLE);
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("dni", dni);
        updates.put("address", address);
        updates.put("gender", gender);
        updates.put("birthdate", birthdate);

        db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (tvUserName != null) tvUserName.setText(name);
                    if (tvUserEmail != null) tvUserEmail.setText(email);
                    if (tvUserPhone != null) tvUserPhone.setText(phone);
                    if (tvUserDni != null) tvUserDni.setText(dni);
                    if (tvUserAddress != null) tvUserAddress.setText(address);
                    if (tvUserGender != null) tvUserGender.setText(gender);
                    if (tvUserBirthdate != null) tvUserBirthdate.setText(birthdate);

                    Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();

                    isSaving = false;

                    new android.os.Handler().postDelayed(() -> {
                        exitEditMode();
                    }, 500);
                })
                .addOnFailureListener(e -> {
                    isSaving = false;

                    if (btnSaveProfile != null) {
                        btnSaveProfile.setEnabled(true);
                        btnSaveProfile.setText(getString(R.string.save_profile));
                        btnSaveProfile.setVisibility(View.VISIBLE);
                    }
                    if (btnCancelEdit != null) {
                        btnCancelEdit.setEnabled(true);
                        btnCancelEdit.setVisibility(View.VISIBLE);
                    }

                    if (llEditButtons != null) {
                        llEditButtons.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(this, "Error al actualizar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
        }
    }

    protected void setupBottomNavigation() {
        LinearLayout tabHome = findViewById(R.id.tab_home);
        LinearLayout tabDescuentos = findViewById(R.id.tab_descuentos);
        LinearLayout tabTienda = findViewById(R.id.tab_tienda);
        LinearLayout tabCuadrado = findViewById(R.id.tab_cuadrado);
        LinearLayout tabMenu = findViewById(R.id.tab_menu);

        final int TAB_HOME = 0;
        final int TAB_DESCUENTOS = 1;
        final int TAB_TIENDA = 2;
        final int TAB_CUADRADO = 3;
        final int TAB_MENU = 4;

        View.OnClickListener listener = v -> {
            int tabIndex = -1;

            int id = v.getId();

            if (id == R.id.tab_home) {
                tabIndex = TAB_HOME;
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else if (id == R.id.tab_descuentos) {
                tabIndex = TAB_DESCUENTOS;

            } else if (id == R.id.tab_tienda) {
                tabIndex = TAB_TIENDA;
                startActivity(new Intent(this, CarritoActivity.class));
                finish();
            } else if (id == R.id.tab_cuadrado) {
                tabIndex = TAB_CUADRADO;

            } else if (id == R.id.tab_menu) {
                tabIndex = TAB_MENU;
                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                NavigationView navView = findViewById(R.id.nav_view);
                if (drawerLayout != null && navView != null) {
                    drawerLayout.openDrawer(navView);
                }
            }

            if (tabIndex != -1) {
                navigateToTab(tabIndex);
            }
        };

        if (tabHome != null) tabHome.setOnClickListener(listener);
        if (tabDescuentos != null) tabDescuentos.setOnClickListener(listener);
        if (tabTienda != null) tabTienda.setOnClickListener(listener);
        if (tabCuadrado != null) tabCuadrado.setOnClickListener(listener);
        if (tabMenu != null) tabMenu.setOnClickListener(listener);
    }

    protected void navigateToTab(int tabIndex) {
        resetTabs();

        LinearLayout tabHome = findViewById(R.id.tab_home);
        LinearLayout tabDescuentos = findViewById(R.id.tab_descuentos);
        LinearLayout tabTienda = findViewById(R.id.tab_tienda);
        LinearLayout tabCuadrado = findViewById(R.id.tab_cuadrado);
        LinearLayout tabMenu = findViewById(R.id.tab_menu);

        switch (tabIndex) {
            case 0:
                if (tabHome != null) tabHome.setAlpha(1f);
                break;
            case 1:
                if (tabDescuentos != null) tabDescuentos.setAlpha(1f);
                break;
            case 2:
                if (tabTienda != null) tabTienda.setAlpha(1f);
                break;
            case 3:
                if (tabCuadrado != null) tabCuadrado.setAlpha(1f);
                break;
            case 4:
                if (tabMenu != null) tabMenu.setAlpha(1f);
                break;
        }
    }

    protected void resetTabs() {
        LinearLayout tabHome = findViewById(R.id.tab_home);
        LinearLayout tabDescuentos = findViewById(R.id.tab_descuentos);
        LinearLayout tabTienda = findViewById(R.id.tab_tienda);
        LinearLayout tabCuadrado = findViewById(R.id.tab_cuadrado);
        LinearLayout tabMenu = findViewById(R.id.tab_menu);

        if (tabHome != null) tabHome.setAlpha(0.6f);
        if (tabDescuentos != null) tabDescuentos.setAlpha(0.6f);
        if (tabTienda != null) tabTienda.setAlpha(0.6f);
        if (tabCuadrado != null) tabCuadrado.setAlpha(0.6f);
        if (tabMenu != null) tabMenu.setAlpha(0.6f);
    }

    protected void setupTopNavigation() {
        android.widget.ImageView ivProfile = findViewById(R.id.iv_profile);
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> {
            });
            ivProfile.setClickable(true);
            ivProfile.setFocusable(true);
        }
    }

    protected void setupDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                
                if (itemId == R.id.nav_pedidos_curso) {
                    Intent intent = new Intent(this, PedidosEnCursoActivity.class);
                    startActivity(intent);
                } else if (itemId == R.id.nav_configuracion) {
                    Toast.makeText(this, "Configuración - Próximamente", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.nav_carga_documentos) {
                    Intent intent = new Intent(this, SubirRecetaActivity.class);
                    startActivity(intent);
                }

                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(navigationView);
                }
                return true;
            });
        }
    }
}

