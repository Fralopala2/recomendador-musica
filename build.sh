#!/usr/bin/env bash

# Instala OpenJDK 21 (o la versión de Java que uses)
# Render suele tener Java preinstalado, pero esto lo asegura.
# Si tu proyecto usa Java 17, cambia 21 por 17.
# Si Render ya tiene Java, esta línea simplemente no hará nada.
apt-get update && apt-get install -y openjdk-21-jdk

# Ejecuta el Maven Wrapper para construir el proyecto
./mvnw clean install