package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class SubirRecetaActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_FILE = 1001;
    
    private Button btnSeleccionarArchivo;
    private Button btnSubirReceta;
    private Button btnEliminarArchivo;
    private LinearLayout llFileInfo;
    private TextView tvNombreArchivo;
    private ImageView ivPreview;
    private LinearLayout llPdfIcon;
    
    private Uri selectedFileUri;
    private String selectedFileName;
    private boolean isImageFile = false;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subir_receta);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initializeViews();
        setupListeners();
        setupBottomNavigation();
        setupTopNavigation();
        setupDrawer();
        // Highlight the menu tab since we're in SubirRecetaActivity
        navigateToTab(4); // TAB_MENU = 4
    }

    private void setupListeners() {
        btnSeleccionarArchivo.setOnClickListener(v -> seleccionarArchivo());
        btnSubirReceta.setOnClickListener(v -> subirReceta());
        btnEliminarArchivo.setOnClickListener(v -> eliminarArchivo());
    }

    private void seleccionarArchivo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"image/*", "application/pdf"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Selecciona un archivo"), REQUEST_CODE_SELECT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_SELECT_FILE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            
            if (selectedFileUri != null) {
                // Get file name
                String uriString = selectedFileUri.toString();
                if (uriString.contains("/")) {
                    selectedFileName = uriString.substring(uriString.lastIndexOf("/") + 1);
                } else {
                    selectedFileName = "archivo_seleccionado";
                }
                
                // Check if it's an image or PDF
                String mimeType = getContentResolver().getType(selectedFileUri);
                isImageFile = mimeType != null && mimeType.startsWith("image/");
                
                // Update UI
                mostrarArchivoSeleccionado();
            }
        }
    }

    private void mostrarArchivoSeleccionado() {
        if (selectedFileUri == null) return;
        
        llFileInfo.setVisibility(View.VISIBLE);
        tvNombreArchivo.setText(selectedFileName);
        btnSubirReceta.setEnabled(true);
        
        if (isImageFile) {
            // Show image preview
            ivPreview.setVisibility(View.VISIBLE);
            llPdfIcon.setVisibility(View.GONE);
            Glide.with(this)
                    .load(selectedFileUri)
                    .centerCrop()
                    .into(ivPreview);
        } else {
            // Show PDF icon
            ivPreview.setVisibility(View.GONE);
            llPdfIcon.setVisibility(View.VISIBLE);
        }
    }

    private void eliminarArchivo() {
        selectedFileUri = null;
        selectedFileName = null;
        llFileInfo.setVisibility(View.GONE);
        btnSubirReceta.setEnabled(false);
        ivPreview.setVisibility(View.GONE);
        llPdfIcon.setVisibility(View.GONE);
    }

    private void subirReceta() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para subir recetas", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedFileUri == null) {
            Toast.makeText(this, "Por favor selecciona un archivo", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Disable button during upload
        btnSubirReceta.setEnabled(false);
        btnSubirReceta.setText("Subiendo...");
        
        // Create storage reference
        String userId = currentUser.getUid();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileExtension = isImageFile ? 
            (selectedFileName.contains(".") ? selectedFileName.substring(selectedFileName.lastIndexOf(".")) : ".jpg") :
            ".pdf";
        
        StorageReference storageRef = storage.getReference();
        StorageReference recetaRef = storageRef.child("recetas")
                .child(userId)
                .child("receta_" + timestamp + fileExtension);
        
        // Upload file
        recetaRef.putFile(selectedFileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    recetaRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Save metadata to Firestore
                        Map<String, Object> recetaData = new HashMap<>();
                        recetaData.put("userId", userId);
                        recetaData.put("userEmail", currentUser.getEmail());
                        recetaData.put("fileName", selectedFileName);
                        recetaData.put("fileUrl", uri.toString());
                        recetaData.put("fileType", isImageFile ? "image" : "pdf");
                        recetaData.put("timestamp", System.currentTimeMillis());
                        recetaData.put("status", "pending"); // pending, approved, rejected
                        recetaData.put("reviewedBy", null);
                        recetaData.put("reviewedAt", null);
                        
                        db.collection("recetas")
                                .add(recetaData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "Receta subida exitosamente. Será revisada manualmente.", Toast.LENGTH_LONG).show();
                                    
                                    // Reset UI
                                    eliminarArchivo();
                                    btnSubirReceta.setText("Subir Receta");
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al guardar la información: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    btnSubirReceta.setEnabled(true);
                                    btnSubirReceta.setText("Subir Receta");
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir el archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSubirReceta.setEnabled(true);
                    btnSubirReceta.setText("Subir Receta");
                });
    }

    protected void setupBottomNavigation() {
        android.widget.LinearLayout tabHome = findViewById(R.id.tab_home);
        android.widget.LinearLayout tabDescuentos = findViewById(R.id.tab_descuentos);
        android.widget.LinearLayout tabTienda = findViewById(R.id.tab_tienda);
        android.widget.LinearLayout tabCuadrado = findViewById(R.id.tab_cuadrado);
        android.widget.LinearLayout tabMenu = findViewById(R.id.tab_menu);

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
                startActivity(new Intent(this, DescuentosActivity.class));
                finish();
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

        android.widget.LinearLayout tabHome = findViewById(R.id.tab_home);
        android.widget.LinearLayout tabDescuentos = findViewById(R.id.tab_descuentos);
        android.widget.LinearLayout tabTienda = findViewById(R.id.tab_tienda);
        android.widget.LinearLayout tabCuadrado = findViewById(R.id.tab_cuadrado);
        android.widget.LinearLayout tabMenu = findViewById(R.id.tab_menu);

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
        android.widget.LinearLayout tabHome = findViewById(R.id.tab_home);
        android.widget.LinearLayout tabDescuentos = findViewById(R.id.tab_descuentos);
        android.widget.LinearLayout tabTienda = findViewById(R.id.tab_tienda);
        android.widget.LinearLayout tabCuadrado = findViewById(R.id.tab_cuadrado);
        android.widget.LinearLayout tabMenu = findViewById(R.id.tab_menu);

        if (tabHome != null) tabHome.setAlpha(0.6f);
        if (tabDescuentos != null) tabDescuentos.setAlpha(0.6f);
        if (tabTienda != null) tabTienda.setAlpha(0.6f);
        if (tabCuadrado != null) tabCuadrado.setAlpha(0.6f);
        if (tabMenu != null) tabMenu.setAlpha(0.6f);
    }

    protected void setupTopNavigation() {
        ImageView ivProfile = findViewById(R.id.iv_profile);
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
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
                    // Already in SubirRecetaActivity
                    Toast.makeText(this, "Ya estás en la página de carga de recetas", Toast.LENGTH_SHORT).show();
                }
                
                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(navigationView);
                }
                return true;
            });
        }
    }
}

