# Recomendador de Música por Emojis 🎶

Este es un proyecto de Spring Boot que actúa como un servicio de recomendación de música basado en emojis y estados de ánimo. Permite a los usuarios obtener sugerencias de géneros musicales asociadas a las emociones expresadas a través de emojis.

## Características

* **Recomendación por Emoji:** Obtén géneros musicales recomendados al introducir uno o varios emojis.
* **Gestión de Emojis y Estados de Ánimo:** Configuración flexible de asociaciones entre emojis, descripciones de estado de ánimo y géneros musicales.
* **Base de Datos en Memoria (H2):** Utiliza una base de datos H2 para la persistencia de datos durante la ejecución.
* **API RESTful:** Interfaz clara y sencilla para interactuar con el servicio.

## Tecnologías Utilizadas

* **Spring Boot:** Framework para el desarrollo rápido de aplicaciones Java.
* **Spring Data JPA:** Para la gestión de la base de datos y el mapeo de objetos.
* **H2 Database:** Base de datos en memoria para desarrollo y pruebas.
* **Maven:** Herramienta de gestión de proyectos y dependencias.
* **JUnit 5 & Mockito:** Para las pruebas unitarias y de integración.

## Cómo Ejecutar el Proyecto

1.  **Clonar el Repositorio:**
    ```bash
    git clone [https://github.com/TU_USUARIO/NOMBRE_REPOSITORIO.git](https://github.com/TU_USUARIO/NOMBRE_REPOSITORIO.git)
    cd NOMBRE_REPOSITORIO
    ```

2.  **Compilar y Ejecutar:**
    Asegúrate de tener Maven y Java (JDK 21 o superior) instalados.
    ```bash
    mvnw spring-boot:run
    ```
    La aplicación se iniciará en `http://localhost:8080`.

## Endpoints de la API

* **`GET /api/recommendations/emojimoods`**:
    Devuelve la lista completa de todas las asociaciones de emoji-estado de ánimo-género.
    Ejemplo de respuesta:
    ```json
    [
        {"id": 1, "emoji": "😄", "moodDescription": "Alegre", "genreHint": "Pop"},
        {"id": 2, "emoji": "🎉", "moodDescription": "Fiesta", "genreHint": "Dance"},
        // ... otros emojis
    ]
    ```

* **`GET /api/recommendations/by-emojis?emojis={listaDeEmojis}`**:
    Devuelve recomendaciones de canciones/artistas/géneros basadas en uno o más emojis.
    Ejemplo: `http://localhost:8080/api/recommendations/by-emojis?emojis=😄`
    Ejemplo de respuesta:
    ```json
    ["Song J - Artist 10 (Indie)"]
    ```
    (Nota: Las canciones y artistas son datos de ejemplo generados por la lógica de recomendación).

## Estructura del Proyecto

src/
├── main/
│   ├── java/
│   │   └── com/ejemplo/musicaemoji/
│   │       ├── controller/        # Controladores REST
│   │       ├── model/             # Entidades de la base de datos
│   │       ├── repository/        # Repositorios JPA
│   │       ├── service/           # Lógica de negocio
│   │       └── RecomendadorMusicaApplication.java
│   └── resources/
│       ├── application.properties # Configuración de la aplicación
│       ├── schema.sql             # Definición de la estructura de la base de datos
│       └── data.sql               # Datos iniciales para la base de datos
└── test/
└── java/
└── com/ejemplo/musicaemoji/
├── controller/        # Tests de integración de controladores
├── service/           # Tests de lógica de negocio
└── RecomendadorMusicaApplicationTests.java

## Contribuciones

Siéntete libre de clonar este repositorio, hacer tus propias mejoras y experimentar.

---
