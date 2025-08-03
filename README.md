# Recomendador de Música por Emoji 🎶

Este proyecto es una aplicación de recomendación de música que permite a los usuarios obtener sugerencias de canciones basadas en los emojis que introducen. Combina un potente backend de Spring Boot con una app Android moderna.

## ✨ Características

- **Recomendación por Emojis**: Introduce uno o varios emojis para obtener sugerencias de géneros musicales y canciones
- **Integración con Spotify**: Utiliza la API de Spotify para buscar y mostrar canciones relevantes
- **Vista Previa de Canciones**: Escucha fragmentos de 30 segundos de las canciones directamente en la aplicación (si están disponibles en Spotify)
- **Enlaces Directos a Spotify**: Abre la canción completa en la aplicación de Spotify con un solo toque
- **Recomendaciones de Géneros**: Muestra el/los género(s) musical(es) asociado(s) a los emojis introducidos
- **Splash Screen Animada**: Una pantalla de carga inicial con el logo de la aplicación
- **Backend Escalable**: Desarrollado con Spring Boot y **Firebase Firestore** como base de datos NoSQL (sin SQL relacional)
- **Frontend Moderno**: Aplicación Android nativa construida con Jetpack Compose para una interfaz de usuario fluida y reactiva

## 🛠️ Tecnologías Utilizadas

### Backend (Spring Boot)
- Java 21
- Spring Boot 3.x
- Spring WebFlux (para llamadas no bloqueantes a la API de Spotify)
- **Google Firebase Firestore** (base de datos NoSQL principal)
- [firebase-admin](https://mvnrepository.com/artifact/com.google.firebase/firebase-admin) (SDK Java)
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
- **Cuenta de desarrollador de Spotify**: Necesitarás crear una aplicación en el [Dashboard de Desarrolladores de Spotify](https://developer.spotify.com/dashboard) para obtener tu Client ID y Client Secret.
- **Proyecto de Firebase**: Debes crear un proyecto en [Firebase Console](https://console.firebase.google.com/), habilitar Firestore y generar una clave de servicio (JSON).

### 1. Configuración del Backend

1. **Clona el repositorio**:
   ```bash
   git clone https://github.com/Fralopala2/recomendador-musica.git
   cd recomendador-musica
   ```

2. **Configura las credenciales de Spotify y Firebase**:
   - Crea o edita `src/main/resources/application.properties`
   - Añade tus credenciales de Spotify:
     ```properties
     spotify.client.id=TU_CLIENT_ID_DE_SPOTIFY
     spotify.client.secret=TU_CLIENT_SECRET_DE_SPOTIFY
     ```
   - Exporta como variable de entorno el contenido del JSON de la clave de servicio de Firebase:
     ```bash
     export FIREBASE_SERVICE_ACCOUNT_KEY='{"type":...}' # TODO: Pega aquí el JSON completo como string
     ```
   - En despliegues Render o similares, usa la variable de entorno `FIREBASE_SERVICE_ACCOUNT_KEY`.

3. **Construye el proyecto Maven**:
   ```bash
   ./mvnw clean install
   ```

4. **Ejecuta la aplicación Spring Boot**:
   ```bash
   ./mvnw spring-boot:run
   ```
   El backend se iniciará en `http://localhost:8080`.

### 2. Configuración del Frontend (Aplicación Android)

1. **Abre el proyecto en Android Studio**:
   - Selecciona "Open an existing Android Studio project" y navega a `recomendador-musica/android-app`.

2. **Añade el logo a drawable**:
   - Copia el archivo `logo_compressed.png` a `android-app/app/src/main/res/drawable`.

3. **Configura la URL base del backend**:
   - Edita `android-app/app/src/main/java/com/ejemplo/musicaemoji/androidapp/api/MusicApi.kt`
   - Modifica la constante `BASE_URL` según tu caso:
     - Emulador: `http://10.0.2.2:8080/`
     - Dispositivo físico: `http://<TU_IP_LOCAL>:8080/`
     - Backend en la nube: `https://<TU_DOMINIO_PUBLICO>/`

4. **Sincroniza y ejecuta**:
   - Sincroniza Gradle y ejecuta la app en emulador o dispositivo.

### 3. Dockerización del Backend

Se incluye un archivo `Dockerfile` para facilitar el despliegue:

```dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN chmod +x mvnw && ./mvnw clean install -DskipTests
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/recomendador-musica-0.0.1-SNAPSHOT.jar"]
```

### 4. Despliegue en la Nube (Render)

1. Conecta tu repositorio de GitHub a Render
2. Crea un nuevo servicio web y selecciona tu repositorio
3. Configura variables de entorno:
   - `SPOTIFY_CLIENT_ID`
   - `SPOTIFY_CLIENT_SECRET`
   - `FIREBASE_SERVICE_ACCOUNT_KEY`
   - `SPRING_PROFILES_ACTIVE=production`
4. El Dockerfile está preparado para el despliegue de la app.

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

---

**Notas:**
- El backend ahora usa exclusivamente Firestore para almacenar y consultar los datos de emojis y géneros.
- Ya no se usa ni H2 ni PostgreSQL.
- Para ver detalles de la implementación Firestore, revisa los archivos [`FirebaseConfig.java`](https://github.com/Fralopala2/recomendador-musica/blob/main/src/main/java/com/ejemplo/musicaemoji/config/FirebaseConfig.java) y [`EmojiMoodFirestoreRepository.java`](https://github.com/Fralopala2/recomendador-musica/blob/main/src/main/java/com/ejemplo/musicaemoji/repository/EmojiMoodFirestoreRepository.java).
- Consulta la [búsqueda de código en GitHub](https://github.com/Fralopala2/recomendador-musica/search?q=firestore) para más información.