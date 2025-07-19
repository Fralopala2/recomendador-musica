package com.ejemplo.musicaemoji.service;

import com.ejemplo.musicaemoji.model.User;
import com.ejemplo.musicaemoji.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service // Indica que esta clase es un componente de servicio de Spring
public class UserService {

    @Autowired // Inyecta una instancia de UserRepository
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll(); // Obtiene todos los usuarios
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id); // Obtiene un usuario por ID
    }

    public User createUser(User user) {
        return userRepository.save(user); // Guarda un nuevo usuario
    }

    public User updateUser(Long id, User userDetails) {
        // Busca el usuario existente o lanza una excepción si no se encuentra
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        user.setUsername(userDetails.getUsername()); // Actualiza el nombre de usuario
        user.setEmail(userDetails.getEmail());     // Actualiza el email
        return userRepository.save(user); // Guarda los cambios
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id); // Elimina un usuario por ID
    }
}