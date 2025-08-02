package com.ejemplo.musicaemoji.repository;

import com.ejemplo.musicaemoji.model.EmojiMood;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class EmojiMoodFirestoreRepository {

    private final CollectionReference emojiMoodsCollection;

    @Autowired
    public EmojiMoodFirestoreRepository(Firestore firestore) {
        // Inicializa la referencia a la colección "emojiMoods" en Firestore
        this.emojiMoodsCollection = firestore.collection("emojiMoods");
        System.out.println("FirestoreRepository: Colección 'emojiMoods' inicializada.");
    }

    /**
     * Guarda un EmojiMood en Firestore. Si el ID es nulo, Firestore generará uno.
     * Si el ID existe, actualizará el documento.
     * @param emojiMood El objeto EmojiMood a guardar.
     * @return El EmojiMood guardado con su ID de Firestore.
     */
    public EmojiMood save(EmojiMood emojiMood) {
        DocumentReference docRef;
        if (emojiMood.getId() == null || emojiMood.getId().isEmpty()) {
            // Si no hay ID, Firestore genera uno nuevo
            docRef = emojiMoodsCollection.document();
            emojiMood.setId(docRef.getId()); // Asigna el ID generado al objeto
        } else {
            // Si ya tiene un ID, usa ese para actualizar
            docRef = emojiMoodsCollection.document(emojiMood.getId());
        }

        ApiFuture<WriteResult> result = docRef.set(emojiMood); // Guarda el objeto
        try {
            System.out.println("FirestoreRepository: EmojiMood guardado en Firestore en: " + result.get().getUpdateTime());
            return emojiMood;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("FirestoreRepository: Error al guardar EmojiMood: " + e.getMessage());
            throw new RuntimeException("Error al guardar EmojiMood en Firestore", e);
        }
    }

    /**
     * Busca un EmojiMood por su ID de documento de Firestore.
     * @param id El ID del documento.
     * @return Optional que contiene el EmojiMood si se encuentra.
     */
    public Optional<EmojiMood> findById(String id) { // El ID es String para Firestore
        DocumentReference docRef = emojiMoodsCollection.document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                // Convierte el documento de Firestore a un objeto EmojiMood
                EmojiMood emojiMood = document.toObject(EmojiMood.class);
                emojiMood.setId(document.getId()); // Asegura que el ID del objeto coincida con el del documento
                return Optional.ofNullable(emojiMood);
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("FirestoreRepository: Error al buscar EmojiMood por ID: " + e.getMessage());
            throw new RuntimeException("Error al buscar EmojiMood por ID en Firestore", e);
        }
    }

    /**
     * Busca un EmojiMood por su campo 'emoji'.
     * @param emoji El emoji a buscar.
     * @return Optional que contiene el EmojiMood si se encuentra.
     */
    public Optional<EmojiMood> findByEmoji(String emoji) {
        ApiFuture<QuerySnapshot> future = emojiMoodsCollection.whereEqualTo("emoji", emoji).get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (!documents.isEmpty()) {
                // Debería haber solo uno ya que 'emoji' es único
                EmojiMood emojiMood = documents.get(0).toObject(EmojiMood.class);
                emojiMood.setId(documents.get(0).getId());
                return Optional.ofNullable(emojiMood);
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("FirestoreRepository: Error al buscar EmojiMood por emoji: " + e.getMessage());
            throw new RuntimeException("Error al buscar EmojiMood por emoji en Firestore", e);
        }
    }

    /**
     * Obtiene todos los EmojiMoods de la colección.
     * @return Lista de todos los EmojiMoods.
     */
    public List<EmojiMood> findAll() {
        ApiFuture<QuerySnapshot> future = emojiMoodsCollection.get();
        List<EmojiMood> emojiMoods = new ArrayList<>();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                EmojiMood emojiMood = document.toObject(EmojiMood.class);
                emojiMood.setId(document.getId());
                emojiMoods.add(emojiMood);
            }
            System.out.println("FirestoreRepository: Encontrados " + emojiMoods.size() + " EmojiMoods.");
            return emojiMoods;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("FirestoreRepository: Error al obtener todos los EmojiMoods: " + e.getMessage());
            throw new RuntimeException("Error al obtener todos los EmojiMoods de Firestore", e);
        }
    }

    /**
     * Elimina un EmojiMood por su ID de documento de Firestore.
     * @param id El ID del documento a eliminar.
     */
    public void deleteById(String id) { // El ID es String para Firestore
        ApiFuture<WriteResult> writeResult = emojiMoodsCollection.document(id).delete();
        try {
            System.out.println("FirestoreRepository: EmojiMood con ID " + id + " eliminado en: " + writeResult.get().getUpdateTime());
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("FirestoreRepository: Error al eliminar EmojiMood por ID: " + e.getMessage());
            throw new RuntimeException("Error al eliminar EmojiMood de Firestore", e);
        }
    }

    /**
     * Cuenta el número de documentos en la colección emojiMoods.
     * @return El número de documentos.
     */
    public long count() {
        ApiFuture<QuerySnapshot> future = emojiMoodsCollection.get();
        try {
            return future.get().size();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("FirestoreRepository: Error al contar documentos: " + e.getMessage());
            throw new RuntimeException("Error al contar documentos en Firestore", e);
        }
    }

    /**
     * Guarda una lista de EmojiMoods en Firestore.
     * @param emojiMoods La lista de EmojiMoods a guardar.
     */
    public void saveAll(List<EmojiMood> emojiMoods) {
        for (EmojiMood emojiMood : emojiMoods) {
            save(emojiMood); // Reutiliza el método save para cada elemento
        }
        System.out.println("FirestoreRepository: Se han guardado " + emojiMoods.size() + " EmojiMoods en Firestore.");
    }
}