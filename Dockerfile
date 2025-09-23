FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21
WORKDIR /app
COPY /application/target/amt-tiltak-app.jar app.jar
ENV TZ="Europe/Oslo"
EXPOSE 8080
CMD ["-jar","app.jar"]