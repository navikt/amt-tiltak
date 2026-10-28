FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25
WORKDIR /app
COPY application/build/libs/*-app.jar app.jar
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER 1000:1000
CMD ["-jar","app.jar"]