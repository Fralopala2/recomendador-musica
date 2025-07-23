#!/usr/bin/env bash

# Establece JAVA_HOME a la ruta de OpenJDK 21 en Render
# Render suele instalar OpenJDK en /usr/local/openjdk/VERSION
# Si tu proyecto usa Java 17, cambia 21 por 17.
export JAVA_HOME=/usr/local/openjdk-21
export PATH=$JAVA_HOME/bin:$PATH

# Da permisos de ejecución a mvnw
chmod +x ./mvnw

# Ejecuta el Maven Wrapper para construir el proyecto
./mvnw clean install