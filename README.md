# Recomendador de Música por Emoji 🎶

Este proyecto es una aplicación de recomendación de música que permite a los usuarios obtener sugerencias de canciones basadas en los emojis que introducen. Combina un potente backend de Spring Boot con un frontend moderno de Android usando Jetpack Compose, integrándose con la API de Spotify para ofrecer resultados relevantes.

## ✨ Características

- **Recomendación por Emojis**: Introduce uno o varios emojis para obtener sugerencias de géneros musicales y canciones
- **Integración con Spotify**: Utiliza la API de Spotify para buscar y mostrar canciones relevantes
- **Vista Previa de Canciones**: Escucha fragmentos de 30 segundos de las canciones directamente en la aplicación (si están disponibles en Spotify)
- **Enlaces Directos a Spotify**: Abre la canción completa en la aplicación de Spotify con un solo toque
- **Recomendaciones de Géneros**: Muestra el/los género(s) musical(es) asociado(s) a los emojis introducidos
- **Splash Screen Animada**: Una pantalla de carga inicial con el logo de la aplicación
- **Backend Escalable**: Desarrollado con Spring Boot para manejar la lógica de negocio y la comunicación con Spotify
- **Frontend Moderno**: Aplicación Android nativa construida con Jetpack Compose para una interfaz de usuario fluida y reactiva

## 🛠️ Tecnologías Utilizadas

### Backend (Spring Boot)
- Java 21
- Spring Boot 3.x
- Spring Data JPA
- Spring WebFlux (para llamadas no bloqueantes a la API de Spotify)
- H2 Database (para desarrollo local y pruebas)
- PostgreSQL (para despliegue en producción, ej. Render)
- Lombok
- Maven
- API de Spotify

### Frontend (Android)
- Kotlin
- Jetpack Compose
- Android SDK
- Retrofit (cliente HTTP para la API del backend)
- Gson (conversor JSON)
- Coroutines (para manejo de asincronía)
- ViewModel (para gestión del estado de la UI)

## 🚀 Configuración y Ejecución

### Requisitos Previos
- Java Development Kit (JDK) 21 o superior
- Maven (para el backend)
- Android Studio (para el frontend)
- **Cuenta de desarrollador de Spotify**: Necesitarás crear una aplicación en el [Dashboard de Desarrolladores de Spotify](https://developer.spotify.com/dashboard) para obtener tu Client ID y Client Secret

### 1. Configuración del Backend

1. **Clona el repositorio**:
   ```bash
   git clone https://github.com/Fralopala2/recomendador-musica.git
   cd recomendador-musica
   ```

2. **Configura las credenciales de Spotify**:
   - Crea un archivo `src/main/resources/application.properties` (si no existe) o edita el existente
   - Añade tus credenciales de Spotify:
   ```properties
   spotify.client.id=TU_CLIENT_ID_DE_SPOTIFY
   spotify.client.secret=TU_CLIENT_SECRET_DE_SPOTIFY
   ```

3. **Configuración de la Base de Datos (H2 para desarrollo local)**:
   El proyecto está configurado para usar H2 Database en memoria por defecto (`application.properties`). No necesitas configuración adicional para empezar. Los datos iniciales de emojis se cargarán desde `src/main/resources/import.sql`.

4. **Construye el proyecto Maven**:
   ```bash
   ./mvnw clean install
   ```

5. **Ejecuta la aplicación Spring Boot**:
   ```bash
   ./mvnw spring-boot:run
   ```

El backend se iniciará en `http://localhost:8080`.

### 2. Configuración del Frontend (Aplicación Android)

1. **Abre el proyecto en Android Studio**:
   En Android Studio, selecciona "Open an existing Android Studio project" y navega a la carpeta `recomendador-musica/android-app`.

2. **Añade el logo a drawable**:
   Copia el archivo `logo_compressed.png` (que se encuentra en la raíz de tu repositorio o en la carpeta assets si la tienes) a la carpeta `android-app/app/src/main/res/drawable`.

3. **Configura la URL base del backend**:
   - Abre `android-app/app/src/main/java/com/ejemplo/musicaemoji/androidapp/api/MusicApi.kt`
   - Modifica la `BASE_URL` en el objeto `RetrofitInstance`:
     - **Para emulador de Android**: `private const val BASE_URL = "http://10.0.2.2:8080/"`
     - **Para dispositivo Android físico**: Reemplaza `TU_IP_LOCAL` con la dirección IP de tu máquina de desarrollo (ej. `http://192.168.1.X:8080/`)
     - **Para backend desplegado en la nube**: Reemplaza `TU_DOMINIO_PUBLICO` con la URL de tu servicio desplegado (ej. `https://tu-servicio.onrender.com/`)

4. **Sincroniza y ejecuta**:
   - Sincroniza el proyecto Gradle (File → Sync Project with Gradle Files)
   - Ejecuta la aplicación en un emulador o dispositivo Android

## ☁️ Despliegue en la Nube (Render)

El backend de Spring Boot puede ser fácilmente desplegado en plataformas como Render:

1. Conecta tu repositorio de GitHub a Render
2. Crea un nuevo servicio web y selecciona tu repositorio
3. **Configura las variables de entorno en Render**:
   - `SPOTIFY_CLIENT_ID`: Tu Client ID de Spotify
   - `SPOTIFY_CLIENT_SECRET`: Tu Client Secret de Spotify
   - `DATABASE_URL`: La URL de conexión a tu base de datos PostgreSQL (Render la inyectará automáticamente si usas su servicio de PostgreSQL)
   - `SPRING_PROFILES_ACTIVE`: `production` (o el perfil que uses para producción)

4. Asegúrate de que tu `pom.xml` incluya la dependencia de PostgreSQL y que `application.properties` esté configurado para PostgreSQL si no usas H2 en producción
5. Asegúrate de que tu Dockerfile sea correcto para construir y ejecutar tu aplicación Spring Boot en Render

## 📄 Licencia

Este proyecto está licenciado bajo [Creative Commons Attribution-NonCommercial 4.0 International License](http://creativecommons.org/licenses/by-nc/4.0/).

[![CC BY-NC 4.0](https://img.shields.io/badge/License-CC%20BY--NC%204.0-lightgrey.svg)](http://creativecommons.org/licenses/by-nc/4.0/)

**Esto significa que puedes:**
- ✅ Usar el código para proyectos personales
- ✅ Modificar y adaptar el código
- ✅ Compartir el código

**Con las siguientes condiciones:**
- 📝 Debes dar crédito apropiado
- 🚫 **No puedes usar el código con fines comerciales**
- 🔄 Si modificas el código, debes indicar los cambios realizados

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor, abre un issue o envía un pull request.

## 📧 Contacto

[pacoaldev@gmail.com]
