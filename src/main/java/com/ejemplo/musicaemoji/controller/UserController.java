package com.ejemplo.musicaemoji.controller;

import com.ejemplo.musicaemoji.model.User;
import com.ejemplo.musicaemoji.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/users") // Mapea todas las peticiones que empiezan por /api/users a este controlador
public class UserController {

    @Autowired // Inyecta una instancia de UserService
    private UserService userService;

    @GetMapping // Maneja peticiones GET a /api/users
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}") // Maneja peticiones GET a /api/users/{id}
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        // Devuelve el usuario si se encuentra (código 200 OK), o un 404 Not Found si no
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping // Maneja peticiones POST a /api/users
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // @RequestBody: El cuerpo de la petición HTTP se mapea a un objeto User
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED); // Devuelve 201 Created
    }

    @PutMapping("/{id}") // Maneja peticiones PUT a /api/users/{id}
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser); // Devuelve 200 OK
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // Devuelve 404 Not Found si el usuario no existe
        }
    }

    @DeleteMapping("/{id}") // Maneja peticiones DELETE a /api/users/{id}
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // Devuelve 204 No Content
    }
}