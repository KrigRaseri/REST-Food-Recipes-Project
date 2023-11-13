FROM openjdk:21

WORKDIR /app

COPY build/libs/REST-Recipes-Project-1.2.3.jar .

EXPOSE 8080

CMD ["java", "-jar", "REST-Recipes-Project-1.2.3.jar"]