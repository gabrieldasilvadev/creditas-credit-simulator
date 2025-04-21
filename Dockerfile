# syntax=docker/dockerfile:1

FROM gradle:8.5-jdk21

WORKDIR /app

# Copia tudo do projeto para dentro da imagem
COPY . .

# Compila o projeto gerando o bootJar (com nome fixo opcional)
RUN ./gradlew :container:bootJar -x test --no-daemon

# Expõe a porta da aplicação
EXPOSE 7000

# Roda diretamente o JAR gerado
CMD ["java", "-jar", "container/build/libs/app.jar"]
