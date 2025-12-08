# App Móvil - Medipocket

Aplicación Android (Java) con autenticación Firebase, catálogo de productos y carrito, más pantalla de Lootbox con compra de llaves y conteo de llaves por usuario.

## Requisitos
- Android Studio o superior.
- Java 11 (configurado en `app/build.gradle` como `sourceCompatibility` / `targetCompatibility`).
- Dispositivo o emulador con API 29+ (minSdk 29, targetSdk 36).
- Cuenta y proyecto Firebase con Firestore y Auth habilitados.

## Configuración rápida
1. Clonar el repo.
2. Abrir en Android Studio y dejar que sincronice Gradle.
3. Revisar `local.properties` para que apunte al SDK de Android correcto.
4. (Opcional) Ajustar reglas de Firestore según tus necesidades; la app escribe en `users/{uid}/cart` y `users/{uid}` (campo `lootboxKeys`).

## Datos y fuentes
- Productos: se cargan desde `products.json` en assets; el home usa el JSON remoto configurado en `MainActivity` (`https://raw.githubusercontent.com/ClauKev-dev/final-am-acn4av-gonzalez-oturakdjian/refs/heads/main/products.json`).
- Imagen de lootbox: `https://i.imgur.com/SMNYpRE.png`.
- Llave de lootbox: se crea localmente al comprar desde Lootbox (`Llave de Lootbox`, $2500) y se agrega al carrito; el contador de llaves se guarda en Firestore (`lootboxKeys`).

## Flujo principal
- **Home**: muestra productos cargados del JSON remoto; búsqueda y agregado al carrito.
- **Carrito**: persiste en Firestore bajo el usuario; al pagar, si hay llaves compradas, actualiza `lootboxKeys`.
- **Lootbox**: muestra premios de muestra y permite comprar llave; el botón “ABRIR CAJA (N)” refleja `lootboxKeys` del usuario.
- **Navegación**: barra inferior y drawer lateral (configuración y subir receta).

## Construir y ejecutar
- Desde Android Studio: Build > Make Project, luego Run.
- Por terminal: `./gradlew assembleDebug` y luego instalar el APK generado en `app/build/outputs/apk/debug/`.

## Pruebas
- Instrumentadas: `./gradlew connectedAndroidTest` (requiere emulador/dispositivo).
- Unitarias: `./gradlew test`.

## Notas sobre Firebase
- Asegura reglas que permitan lectura/escritura para usuarios autenticados en:
  - `users/{uid}`
  - `users/{uid}/cart`
  - `users/{uid}/orders` (si usas pedidos)
- Si cambias el proyecto de Firebase, reemplaza `app/google-services.json`.

## Estructura rápida
- `app/src/main/java/com/example/final_am_acn4av_gonzalez_oturakdjian/`
  - `MainActivity`: home y carga de productos.
  - `CarritoActivity`: carrito y pago; actualiza llaves.
  - `LootboxActivity`: compra de llave y conteo de llaves.
  - `CarritoManager`: lógica de carrito + Firestore.
- `app/src/main/res/layout/`: pantallas y navegación.
- `products.json`: catálogo base en assets.

Hecho para fines educativos por: Manuela Gonzalez y Claudio Kevin Oturakdjian.
