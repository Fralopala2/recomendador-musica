package com.ejemplo.musicaemoji.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity // Indica que es una entidad JPA (se mapea a una tabla en la BD)
@Data // Anotación de Lombok: Genera getters, setters, toString, equals y hashCode
@NoArgsConstructor // Anotación de Lombok: Genera constructor sin argumentos
@AllArgsConstructor // Anotación de Lombok: Genera constructor con todos los argumentos
@Table(name = "app_user") // Especifica el nombre de la tabla en la BD para evitar conflicto con 'user'
public class User {

    @Id // Marca este campo como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura la generación automática del ID
    private Long id;
    private String username;
    private String email;
    // Podrías añadir campos como password (encriptado), rol, fecha de registro, etc.
}