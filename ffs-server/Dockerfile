FROM eclipse-temurin:17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew :ffs-server:shadowJar --info

FROM eclipse-temurin:17
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /home/gradle/src/ffs-server/build/libs/*.jar /app/ffs-server.jar
ENTRYPOINT ["java","-jar","/app/ffs-server.jar"]
