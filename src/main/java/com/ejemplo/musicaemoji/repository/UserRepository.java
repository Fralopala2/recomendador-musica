package com.ejemplo.musicaemoji.repository;

import com.ejemplo.musicaemoji.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Indica que esta interfaz es un componente de repositorio de Spring
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository<TipoDeEntidad, TipoDeId>
    // Aquí puedes añadir métodos personalizados si los necesitas,
    // por ejemplo: Optional<User> findByUsername(String username);
}