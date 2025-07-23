# Usa una imagen base de OpenJDK 21 (o la versión de Java que uses)
FROM openjdk:21-jdk-slim

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el Maven Wrapper y el pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copia el código fuente de tu aplicación
COPY src src

# Construye la aplicación usando Maven Wrapper
# El comando 'chmod +x mvnw' es para asegurar que mvnw sea ejecutable
RUN chmod +x mvnw && ./mvnw clean install -DskipTests

# Expone el puerto que usa tu aplicación Spring Boot
EXPOSE 8080

# Comando para ejecutar la aplicación cuando el contenedor se inicie
ENTRYPOINT ["java", "-jar", "target/*.jar"]