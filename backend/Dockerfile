# builda o projeto pra gerar o .jar
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# copia somente o necessario das dependencias
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline

#  copia o codigo e gera o jar
COPY src src
RUN ./mvnw clean package -DskipTests

# executa o projeto
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar arquivo.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "arquivo.jar"]