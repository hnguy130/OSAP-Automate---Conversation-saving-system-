FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY ContextServer.java .
COPY postgresql-42.7.9.jar .

RUN javac -cp "postgresql-42.7.9.jar:." ContextServer.java

EXPOSE 8080

CMD ["java", "-cp", "postgresql-42.7.9.jar:.", "ContextServer"]
