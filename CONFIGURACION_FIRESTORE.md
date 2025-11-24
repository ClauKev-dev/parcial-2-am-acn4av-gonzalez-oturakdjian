# Configuración de Firestore para el Carrito

## Estructura de Datos

El carrito se guarda en Firestore con la siguiente estructura:

```
users/
  {userId}/
    cart/
      {productId}/
        name: string
        price: number
        quantity: number
        imageUrl: string
```

## Reglas de Seguridad de Firestore

Para que el carrito funcione correctamente, necesitas configurar las reglas de seguridad en Firebase Console.

### Pasos para configurar las reglas:

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto
3. Ve a **Firestore Database** → **Reglas**
4. Reemplaza las reglas con el siguiente código:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Reglas para usuarios
    match /users/{userId} {
      // Permitir lectura y escritura del documento del usuario solo si está autenticado y es su propio documento
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Reglas para el carrito del usuario
      match /cart/{cartItemId} {
        // Permitir lectura y escritura del carrito solo si el usuario está autenticado y es su propio carrito
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

### Reglas más permisivas (solo para desarrollo/testing):

Si quieres reglas más permisivas para desarrollo (NO recomendado para producción):

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

## Verificación

Después de configurar las reglas:

1. **Publica las reglas** haciendo clic en "Publicar"
2. Espera unos segundos para que se propaguen los cambios
3. Prueba agregar un producto al carrito en la app
4. Verifica en Firebase Console → Firestore Database que se haya creado:
   - La colección `users`
   - El documento con el `userId` del usuario
   - La subcolección `cart`
   - Los documentos de productos dentro de `cart`

## Solución de Problemas

### Error: "PERMISSION_DENIED"
- **Causa**: Las reglas de Firestore no permiten la operación
- **Solución**: Verifica que las reglas estén configuradas correctamente y publicadas

### Error: "Missing or insufficient permissions"
- **Causa**: El usuario no está autenticado o las reglas son muy restrictivas
- **Solución**: Asegúrate de que el usuario esté logueado y que las reglas permitan la operación

### El carrito no se guarda
- Verifica que el usuario esté autenticado
- Revisa los logs en Logcat buscando "CarritoManager"
- Verifica en Firebase Console que las reglas estén publicadas
- Asegúrate de tener conexión a internet

## Nota Importante

Firestore crea automáticamente las colecciones y documentos cuando escribes datos por primera vez. No necesitas crear manualmente la estructura en Firebase Console, solo necesitas configurar las reglas de seguridad correctamente.

