package com.ejemplo.musicaemoji.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource; // Importa esta clase

import java.io.IOException;
import java.io.InputStream; // Usa InputStream para leer el recurso

@Configuration
public class FirebaseConfig {

    // Nombre del archivo JSON de tu clave de servicio de Firebase
    // Asegúrate de que este archivo esté en src/main/resources/firebase/
    // Y que el nombre sea exacto.
    private static final String SERVICE_ACCOUNT_KEY_FILENAME = "music-recommender-db1-firebase-adminsdk-fbsvc-535e1ef730.json"; // <-- ¡CAMBIA ESTO AL NOMBRE REAL DE TU ARCHIVO!
    private static final String FIREBASE_PROJECT_ID = "music-recommender-db1"; // <-- ¡TU ID DE PROYECTO DE FIREBASE!

    @Bean
    public FirebaseApp initializeFirebaseApp() throws IOException {
        // Carga el archivo de la clave de servicio desde el classpath
        // Esto es crucial para que funcione cuando la aplicación se empaqueta en un JAR
        InputStream serviceAccount = new ClassPathResource("firebase/" + SERVICE_ACCOUNT_KEY_FILENAME).getInputStream();

        // Configura las opciones de Firebase
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setProjectId(FIREBASE_PROJECT_ID) // Establece el ID del proyecto explícitamente
                .build();

        // Inicializa la aplicación Firebase
        if (FirebaseApp.getApps().isEmpty()) { // Evita inicializar si ya existe una instancia
            System.out.println("Inicializando Firebase App para el proyecto: " + FIREBASE_PROJECT_ID);
            return FirebaseApp.initializeApp(options);
        } else {
            System.out.println("Firebase App ya inicializada para el proyecto: " + FIREBASE_PROJECT_ID);
            return FirebaseApp.getInstance();
        }
    }

    @Bean
    public Firestore getFirestore(FirebaseApp firebaseApp) {
        System.out.println("Obteniendo instancia de Firestore...");
        return FirestoreClient.getFirestore(firebaseApp);
    }
}