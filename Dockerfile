FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app
ARG JAR_FILE=application/build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENV TZ="Europe/Oslo"
EXPOSE 8080
CMD ["app.jar"]