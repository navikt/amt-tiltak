FROM ghcr.io/navikt/poao-baseimages/java:17
COPY /application/kafka/target/amt-tiltak.jar app.jar