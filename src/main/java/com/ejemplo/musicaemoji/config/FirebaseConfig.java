package com.ejemplo.musicaemoji.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value; // Importa Value para inyectar la variable de entorno
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream; // Para leer el String como InputStream
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    // Inyecta el contenido completo del JSON de la clave de servicio desde una variable de entorno
    // Render inyectará el valor de la variable de entorno FIREBASE_SERVICE_ACCOUNT_KEY
    @Value("${FIREBASE_SERVICE_ACCOUNT_KEY}") // Nombre de la variable de entorno en Render
    private String firebaseServiceAccountKeyJson;

    private static final String FIREBASE_PROJECT_ID = "music-recommender-db1"; // <-- ¡TU ID DE PROYECTO DE FIREBASE!

    @Bean
    public FirebaseApp initializeFirebaseApp() throws IOException {
        // Convierte el String JSON de la variable de entorno en un InputStream
        InputStream serviceAccount = new ByteArrayInputStream(firebaseServiceAccountKeyJson.getBytes());

        // Configura las opciones de Firebase
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setProjectId(FIREBASE_PROJECT_ID)
                .build();

        // Inicializa la aplicación Firebase
        if (FirebaseApp.getApps().isEmpty()) {
            System.out.println("Inicializando Firebase App para el proyecto: " + FIREBASE_PROJECT_ID + " desde variable de entorno.");
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
