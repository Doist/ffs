FROM eclipse-temurin:17 AS build
ARG SERVER_HOST
ARG SERVER_PORT
ENV SERVER_HOST $SERVER_HOST
ENV SERVER_PORT $SERVER_PORT
COPY . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew :ffs-dashboard:browserProductionWebpack

FROM nginx:stable
EXPOSE 3000:3000
COPY --from=build /home/gradle/src/ffs-dashboard/build/distributions/* /usr/share/nginx/html/
ENTRYPOINT ["nginx","-g","daemon off;"]
