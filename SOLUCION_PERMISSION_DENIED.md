# Solución: Error "PERMISSION_DENIED" al Pagar

## Problema

Cuando intentas pagar, recibes el error **"PERMISSION_DENIED"** o **"Missing or insufficient permissions"**.

## Causa

Las reglas de seguridad de Firestore no están configuradas para permitir escribir en la colección de pedidos (`orders`).

## Solución Rápida

### Paso 1: Ve a Firebase Console

1. Abre tu navegador y ve a: https://console.firebase.google.com/
2. Selecciona tu proyecto

### Paso 2: Configura las Reglas de Firestore

1. En el menú lateral, haz clic en **Firestore Database**
2. Ve a la pestaña **Reglas** (en la parte superior)
3. Reemplaza **TODO** el contenido con el siguiente código:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Reglas para usuarios
    match /users/{userId} {
      // Permitir lectura y escritura del documento del usuario
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Reglas para el carrito del usuario
      match /cart/{cartItemId} {
        // Permitir lectura y escritura del carrito
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      // Reglas para los pedidos del usuario
      match /orders/{orderId} {
        // Permitir lectura y escritura de pedidos
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

### Paso 3: Publica las Reglas

1. Haz clic en el botón **"Publicar"** (arriba a la derecha)
2. Espera a que aparezca el mensaje de confirmación
3. Espera 10-30 segundos para que los cambios se propaguen

### Paso 4: Prueba de Nuevo

1. Vuelve a la app
2. Intenta pagar nuevamente
3. Debería funcionar correctamente

## Verificación

Para verificar que las reglas están correctas:

1. En Firebase Console → Firestore Database → Reglas
2. Deberías ver las reglas que copiaste arriba
3. Debe aparecer "Publicado" en verde

## Reglas Alternativas (Solo para Desarrollo)

Si quieres reglas más permisivas **SOLO PARA DESARROLLO/TESTING** (NO recomendado para producción):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

⚠️ **ADVERTENCIA**: Estas reglas permiten que cualquier usuario autenticado lea y escriba en cualquier colección. Úsalas solo para desarrollo.

## Estructura de Datos en Firestore

Después de configurar las reglas, la estructura debería verse así:

```
users/
  {userId}/
    cart/
      {productId}/
        name, price, quantity, imageUrl
    orders/
      {orderId}/
        userId, products, total, status, cardNumber, cardHolder, createdAt, updatedAt
```

## Si el Problema Persiste

1. **Verifica que estés logueado**: Asegúrate de haber iniciado sesión en la app
2. **Revisa los logs**: En Android Studio, busca en Logcat mensajes que contengan "CarritoActivity" o "PERMISSION_DENIED"
3. **Verifica la conexión**: Asegúrate de tener conexión a internet
4. **Espera unos minutos**: A veces las reglas tardan unos minutos en propagarse completamente

## Nota Importante

Firestore crea automáticamente las colecciones cuando escribes datos por primera vez. No necesitas crear manualmente la estructura en Firebase Console, solo necesitas configurar las reglas de seguridad.

