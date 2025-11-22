package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import java.util.ArrayList;
import java.util.List;

public class CarritoManager {
    private static List<Product> carrito = new ArrayList<>();

    public static void agregarProducto(Product product) {
        boolean encontrado = false;
        for (Product p : carrito) {
            if (p.getName().equals(product.getName())) {
                p.increaseQuantity();
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            carrito.add(product);
        }
    }

    public static List<Product> getCarrito() {
        return carrito;
    }

    public static void limpiarCarrito() {
        carrito.clear();
    }
}

