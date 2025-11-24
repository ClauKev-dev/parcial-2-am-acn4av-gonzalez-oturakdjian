package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarritoManager {
    private static final String TAG = "CarritoManager";
    private static List<Product> carrito = new ArrayList<>();
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static CarritoLoadListener loadListener;
    private static boolean carritoCargando = false;
    private static boolean carritoCargado = false;

    public interface CarritoLoadListener {
        void onCarritoLoaded(List<Product> carrito);
        void onError(String error);
    }

    /**
     * Carga el carrito del usuario actual desde Firestore
     */
    public static void cargarCarrito(CarritoLoadListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            carrito.clear();
            carritoCargado = false;
            carritoCargando = false;
            if (listener != null) {
                listener.onCarritoLoaded(new ArrayList<>());
            }
            return;
        }

        String userId = user.getUid();
        loadListener = listener;
        carritoCargando = true;

        Log.d(TAG, "Iniciando carga del carrito para usuario: " + userId);

        db.collection("users").document(userId).collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Guardar productos locales temporalmente antes de limpiar
                    List<Product> productosLocales = new ArrayList<>(carrito);
                    carrito.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        product.setId(document.getId());
                        carrito.add(product);
                    }
                    
                    // Si hay productos locales que no están en Firestore, agregarlos
                    for (Product productoLocal : productosLocales) {
                        boolean existeEnFirestore = false;
                        for (Product productoFirestore : carrito) {
                            if (productoFirestore.getName() != null && 
                                productoLocal.getName() != null &&
                                productoFirestore.getName().equals(productoLocal.getName())) {
                                existeEnFirestore = true;
                                break;
                            }
                        }
                        if (!existeEnFirestore && productoLocal.getId() == null) {
                            // Producto local sin ID, agregarlo a Firestore
                            carrito.add(productoLocal);
                            agregarProductoEnFirestore(userId, productoLocal);
                        }
                    }
                    
                    carritoCargado = true;
                    carritoCargando = false;
                    Log.d(TAG, "Carrito cargado: " + carrito.size() + " productos");
                    if (loadListener != null) {
                        loadListener.onCarritoLoaded(carrito);
                    }
                })
                .addOnFailureListener(e -> {
                    carritoCargado = false;
                    carritoCargando = false;
                    String errorMsg = e.getMessage();
                    Log.e(TAG, "Error al cargar carrito: " + errorMsg, e);
                    
                    // Verificar si es un error de permisos
                    if (errorMsg != null && (errorMsg.contains("permission") || errorMsg.contains("PERMISSION_DENIED"))) {
                        Log.e(TAG, "⚠️ ERROR DE PERMISOS al cargar carrito. Verifica las reglas de Firestore.");
                    }
                    
                    if (loadListener != null) {
                        loadListener.onError(errorMsg);
                    }
                });
    }

    /**
     * Agrega un producto al carrito del usuario actual
     */
    public static void agregarProducto(Product product) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Usuario no autenticado, no se puede agregar producto");
            android.util.Log.e(TAG, "ERROR: Usuario no autenticado al intentar agregar: " + (product != null ? product.getName() : "null"));
            return;
        }

        if (product == null || product.getName() == null) {
            Log.e(TAG, "Producto nulo o sin nombre");
            return;
        }

        String userId = user.getUid();
        Log.d(TAG, "=== AGREGANDO PRODUCTO ===");
        Log.d(TAG, "Producto: " + product.getName());
        Log.d(TAG, "Usuario: " + userId);
        Log.d(TAG, "Carrito cargando: " + carritoCargando);
        Log.d(TAG, "Carrito cargado: " + carritoCargado);
        Log.d(TAG, "Productos en carrito local: " + carrito.size());
        
        // Verificar si el producto ya existe en el carrito local
        Product productoExistente = null;
        for (Product p : carrito) {
            if (p.getName() != null && p.getName().equals(product.getName())) {
                productoExistente = p;
                Log.d(TAG, "Producto encontrado en carrito local: " + p.getName() + " - Cantidad actual: " + p.getQuantity());
                break;
            }
        }
        
        if (productoExistente != null) {
            // Producto ya existe en el carrito local, aumentar cantidad
            productoExistente.increaseQuantity();
            Log.d(TAG, "Producto existente, cantidad aumentada: " + productoExistente.getName() + " - Nueva cantidad: " + productoExistente.getQuantity());
            // Actualizar en Firestore
            actualizarProductoEnFirestore(userId, productoExistente);
        } else {
            // Crear una copia del producto para evitar referencias compartidas
            Product nuevoProducto = new Product(product.getImageUrl(), product.getName(), product.getPrice());
            nuevoProducto.setQuantity(1);
            if (product.getId() != null) {
                nuevoProducto.setId(product.getId());
            }
            carrito.add(nuevoProducto);
            Log.d(TAG, "Nuevo producto agregado al carrito local: " + nuevoProducto.getName());
            Log.d(TAG, "Total productos en carrito local ahora: " + carrito.size());
            // Agregar a Firestore
            agregarProductoEnFirestore(userId, nuevoProducto);
        }
    }

    /**
     * Actualiza la cantidad de un producto en el carrito
     */
    public static void actualizarCantidadProducto(Product product, int nuevaCantidad) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w(TAG, "Usuario no autenticado, no se puede actualizar producto");
            return;
        }

        String userId = user.getUid();
        
        if (nuevaCantidad <= 0) {
            // Eliminar producto
            eliminarProducto(product);
        } else {
            product.setQuantity(nuevaCantidad);
            actualizarProductoEnFirestore(userId, product);
        }
    }

    /**
     * Elimina un producto del carrito
     */
    public static void eliminarProducto(Product product) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w(TAG, "Usuario no autenticado, no se puede eliminar producto");
            return;
        }

        String userId = user.getUid();
        carrito.remove(product);
        
        if (product.getId() != null && !product.getId().isEmpty()) {
            db.collection("users").document(userId).collection("cart")
                    .document(product.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Producto eliminado del carrito"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error al eliminar producto: " + e.getMessage()));
        }
    }

    /**
     * Limpia el carrito del usuario actual
     */
    public static void limpiarCarrito() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            carrito.clear();
            return;
        }

        String userId = user.getUid();
        carrito.clear();
        
        // Eliminar todos los productos del carrito en Firestore
        db.collection("users").document(userId).collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    Log.d(TAG, "Carrito limpiado");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al limpiar carrito: " + e.getMessage()));
    }

    /**
     * Crea un carrito vacío para un nuevo usuario
     */
    public static void crearCarritoVacio(String userId) {
        // El carrito se crea automáticamente cuando se agrega el primer producto
        // Solo necesitamos asegurarnos de que la estructura esté lista
        Log.d(TAG, "Carrito vacío creado para usuario: " + userId);
    }

    /**
     * Obtiene el carrito actual (puede estar desactualizado si no se ha cargado desde Firestore)
     */
    public static List<Product> getCarrito() {
        return new ArrayList<>(carrito); // Retornar copia para evitar modificaciones externas
    }

    /**
     * Obtiene el total de items en el carrito
     */
    public static int getTotalItems() {
        int total = 0;
        for (Product p : carrito) {
            total += p.getQuantity();
        }
        return total;
    }

    // Métodos privados para interactuar con Firestore

    private static void agregarProductoEnFirestore(String userId, Product product) {
        Map<String, Object> productData = new HashMap<>();
        productData.put("name", product.getName());
        productData.put("price", product.getPrice());
        productData.put("quantity", product.getQuantity());
        productData.put("imageUrl", product.getImageUrl() != null ? product.getImageUrl() : "");

        Log.d(TAG, "Agregando producto a Firestore: " + product.getName() + " - Cantidad: " + product.getQuantity());
        Log.d(TAG, "Ruta: users/" + userId + "/cart");

        db.collection("users").document(userId).collection("cart")
                .add(productData)
                .addOnSuccessListener(documentReference -> {
                    product.setId(documentReference.getId());
                    Log.d(TAG, "✓ Producto agregado exitosamente a Firestore: " + product.getName() + " (ID: " + documentReference.getId() + ")");
                    android.util.Log.d(TAG, "✓ ÉXITO: Producto guardado en Firestore");
                })
                .addOnFailureListener(e -> {
                    String errorMsg = e.getMessage();
                    Log.e(TAG, "✗ ERROR al agregar producto a Firestore: " + product.getName(), e);
                    Log.e(TAG, "Mensaje de error completo: " + errorMsg);
                    Log.e(TAG, "Tipo de error: " + e.getClass().getSimpleName());
                    
                    // Verificar si es un error de permisos
                    if (errorMsg != null && (errorMsg.contains("permission") || errorMsg.contains("PERMISSION_DENIED"))) {
                        Log.e(TAG, "⚠️ ERROR DE PERMISOS: Las reglas de Firestore no permiten escribir. Verifica las reglas de seguridad en Firebase Console.");
                        android.util.Log.e(TAG, "⚠️ Necesitas configurar las reglas de Firestore para permitir escritura en users/{userId}/cart");
                    } else {
                        android.util.Log.e(TAG, "✗ Error al guardar: " + errorMsg);
                    }
                    // NO eliminar de la lista local si falla, mejor intentar de nuevo más tarde
                    // carrito.remove(product);
                });
    }

    private static void actualizarProductoEnFirestore(String userId, Product product) {
        if (product.getId() == null || product.getId().isEmpty()) {
            // Si no tiene ID, buscar por nombre y actualizar o crear
            db.collection("users").document(userId).collection("cart")
                    .whereEqualTo("name", product.getName())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Actualizar documento existente
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            product.setId(doc.getId());
                            actualizarDocumento(userId, product);
                        } else {
                            // Crear nuevo documento
                            agregarProductoEnFirestore(userId, product);
                        }
                    });
        } else {
            actualizarDocumento(userId, product);
        }
    }

    private static void actualizarDocumento(String userId, Product product) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("quantity", product.getQuantity());
        updates.put("price", product.getPrice());

        db.collection("users").document(userId).collection("cart")
                .document(product.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Producto actualizado en Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al actualizar producto en Firestore: " + e.getMessage()));
    }
}

