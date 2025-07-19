package com.ejemplo.musicaemoji.service;

import com.ejemplo.musicaemoji.model.User;
import com.ejemplo.musicaemoji.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock // Simula el UserRepository para que no interactúe con la BD real
    private UserRepository userRepository;

    @InjectMocks // Inyecta los mocks en UserService
    private UserService userService;

    @BeforeEach // Se ejecuta antes de cada test
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa los mocks
    }

    @Test // Marca este método como un test
    void getAllUsers_shouldReturnAllUsers() {
        // Define el comportamiento del mock: cuando se llame a findAll(), devuelve esta lista
        List<User> users = Arrays.asList(
                new User(1L, "user1", "user1@example.com"),
                new User(2L, "user2", "user2@example.com")
        );
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size()); // Verifica que el tamaño sea el esperado
        assertEquals("user1", result.get(0).getUsername());
        verify(userRepository, times(1)).findAll(); // Verifica que findAll() se llamó una vez
    }

    @Test
    void getUserById_shouldReturnUser_whenFound() {
        User user = new User(1L, "testuser", "test@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_shouldReturnEmpty_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(99L);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void createUser_shouldReturnSavedUser() {
        User newUser = new User(null, "newuser", "new@example.com");
        User savedUser = new User(3L, "newuser", "new@example.com");
        when(userRepository.save(newUser)).thenReturn(savedUser);

        User result = userService.createUser(newUser);

        assertNotNull(result.getId());
        assertEquals("newuser", result.getUsername());
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    void updateUser_shouldReturnUpdatedUser_whenFound() {
        User existingUser = new User(1L, "oldname", "old@example.com");
        User updatedDetails = new User(null, "newname", "new@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser); // any(User.class) porque el objeto `user` será modificado

        User result = userService.updateUser(1L, updatedDetails);

        assertEquals("newname", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUser_shouldThrowException_whenNotFound() {
        User updatedDetails = new User(null, "newname", "new@example.com");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateUser(99L, updatedDetails));
        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).save(any(User.class)); // Asegura que save() nunca se llamó
    }

    @Test
    void deleteUser_shouldCallDeleteById() {
        doNothing().when(userRepository).deleteById(1L); // Define que deleteById() no hace nada
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L); // Verifica que deleteById() se llamó
    }
}