package com.ejemplo.musicaemoji.controller;

import com.ejemplo.musicaemoji.model.User;
import com.ejemplo.musicaemoji.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Carga el contexto completo de Spring Boot para pruebas de integración
@AutoConfigureMockMvc // Configura MockMvc para simular peticiones HTTP
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Objeto para simular las peticiones HTTP

    @Autowired
    private UserRepository userRepository; // Para interactuar directamente con la BD de prueba (H2)

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos Java a JSON y viceversa

    @BeforeEach // Se ejecuta antes de cada método de prueba
    void setUp() {
        userRepository.deleteAll(); // Limpia la base de datos H2 antes de cada test para asegurar un estado limpio
    }

    @Test
    void getAllUsers_shouldReturnEmptyList_initially() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk()) // Espera un código de estado 200 OK
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty()); // Espera una lista JSON vacía
    }

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        User newUser = new User(null, "testuser", "test@example.com");
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON) // Establece el tipo de contenido a JSON
                        .content(objectMapper.writeValueAsString(newUser))) // Convierte el objeto User a JSON
                .andExpect(status().isCreated()) // Espera un código de estado 201 Created
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("testuser")) // Verifica el nombre de usuario en la respuesta JSON
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getUserById_shouldReturnUser_whenFound() throws Exception {
        User user = userRepository.save(new User(null, "existinguser", "existing@example.com")); // Guarda un usuario en la BD de prueba

        mockMvc.perform(get("/api/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("existinguser"));
    }

    @Test
    void getUserById_shouldReturnNotFound_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999")) // Un ID que no existe
                .andExpect(status().isNotFound()); // Espera un código de estado 404 Not Found
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        User existingUser = userRepository.save(new User(null, "oldname", "old@example.com"));
        User updatedDetails = new User(null, "newname", "new@example.com");

        mockMvc.perform(put("/api/users/" + existingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("newname"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void deleteUser_shouldDeleteUser() throws Exception {
        User userToDelete = userRepository.save(new User(null, "todelete", "todelete@example.com"));

        mockMvc.perform(delete("/api/users/" + userToDelete.getId()))
                .andExpect(status().isNoContent()); // Espera un código de estado 204 No Content

        assertFalse(userRepository.findById(userToDelete.getId()).isPresent()); // Verifica que el usuario ya no está en la BD
    }
}