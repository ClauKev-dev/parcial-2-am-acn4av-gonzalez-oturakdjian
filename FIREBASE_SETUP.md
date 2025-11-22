# Configuración de Firebase

## Pasos para configurar Firebase en la aplicación

### 1. Crear un proyecto en Firebase Console

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Haz clic en "Agregar proyecto" o selecciona un proyecto existente
3. Sigue los pasos para crear/configurar el proyecto

### 2. Agregar una aplicación Android

1. En el panel del proyecto, haz clic en el ícono de Android
2. Ingresa el **Package name** de tu aplicación:
   - `com.example.parcial_1_am_acn4av_gonzales_oturakdjian`
   - (Puedes verificar esto en `app/build.gradle` en `applicationId`)
3. Opcionalmente, agrega un nombre para la app y un certificado SHA-1
4. Haz clic en "Registrar app"

### 3. Descargar el archivo google-services.json

1. Descarga el archivo `google-services.json`
2. **IMPORTANTE**: Coloca este archivo en la carpeta:
   ```
   app/
   └── google-services.json
   ```
   (Debe estar en la raíz del módulo `app`, al mismo nivel que `build.gradle`)

### 4. Habilitar Authentication en Firebase Console

1. En Firebase Console, ve a **Authentication** en el menú lateral
2. Haz clic en "Comenzar"
3. En la pestaña "Sign-in method", habilita:
   - **Email/Password** (método de autenticación por correo electrónico)

### 5. Habilitar Firestore (opcional, para guardar datos adicionales del usuario)

1. En Firebase Console, ve a **Firestore Database**
2. Haz clic en "Crear base de datos"
3. Selecciona el modo:
   - **Modo de prueba** (para desarrollo)
   - O **Modo de producción** (para producción)
4. Selecciona una ubicación para la base de datos
5. Haz clic en "Habilitar"

### 6. Sincronizar el proyecto

1. En Android Studio, haz clic en **Sync Now** cuando aparezca la notificación
2. O ve a **File > Sync Project with Gradle Files**

### 7. Verificar la configuración

Una vez completados los pasos anteriores, la aplicación debería:
- ✅ Conectarse a Firebase Authentication
- ✅ Permitir registro de nuevos usuarios
- ✅ Permitir inicio de sesión
- ✅ Guardar datos adicionales en Firestore (nombre, teléfono)

## Notas importantes

- **El archivo `google-services.json` es único para cada proyecto Firebase**
- **No subas el archivo `google-services.json` a repositorios públicos** (debería estar en `.gitignore`)
- Si cambias el `applicationId` en `build.gradle`, necesitarás actualizar el proyecto en Firebase Console

## Solución de problemas

### Error: "File google-services.json is missing"
- Asegúrate de que el archivo esté en `app/google-services.json`
- Verifica que el nombre del archivo sea exactamente `google-services.json`

### Error: "Default FirebaseApp is not initialized"
- Verifica que el plugin `com.google.gms.google-services` esté aplicado en `app/build.gradle`
- Asegúrate de que el archivo `google-services.json` esté correctamente configurado

### Error de autenticación
- Verifica que Email/Password esté habilitado en Firebase Console > Authentication > Sign-in method

