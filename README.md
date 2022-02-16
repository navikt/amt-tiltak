# AMT Tiltak

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_amt-tiltak&metric=bugs)](https://sonarcloud.io/dashboard?id=navikt_amt-tiltak)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=navikt_amt-tiltak&metric=code_smells)](https://sonarcloud.io/dashboard?id=navikt_amt-tiltak)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=navikt_amt-tiltak&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=navikt_amt-tiltak)

[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=navikt_amt-tiltak&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=navikt_amt-tiltak)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=navikt_amt-tiltak&metric=security_rating)](https://sonarcloud.io/dashboard?id=navikt_amt-tiltak)

## Kjør opp lokalt

For å kjøre opp lokalt så må det startes en postgres database med følgende paramtere:

```
URL         = jdbc:postgresql://localhost:5454/amt-tiltak-db
USERNAME    = postgres
PASSWORD    = qwerty
```

Dette kan gjøres ved bruk av docker-compose. For å starte opp databasen så kjør `docker-compose up -d`.
Verifiser at databasen kjører med `docker ps`.

Start applikasjonen ved å kjøre **main** funksjonen i `application/src/test/kotlin/LocalApplication.kt`

### Token
For å kunne gjøre requests mot endepunktene lokalt så må man først ha et gyldig JWT token.

Etter å ha kjørt opp mock OAuth2 serveren med `docker-compose up -d` så gå til [http://localhost:8082/local-tokenx/debugger](http://localhost:8082/local-tokenx/debugger)

Trykk på "Get a token"-knappen, fyll inn vilkårlig "user/subject" og "acr" og trykk på "Sign in"-knappen.

Kopier verdien i feltet "access_token" fra "Token Response" og legg til headeren `Authorization: Bearer <TOKEN>` med access tokenet. Tokenet har en levetid på 1 år.

## Slett data i lokal database
1. Stopp postgres med `docker-compose down`.
2. Slett den lagrede dataen med `docker volume rm amt-tiltak_postgres-db-volume` 

## Technologies
* https://github.com/navikt/token-support
* https://github.com/navikt/mock-oauth2-server

## SonarCloud
https://sonarcloud.io/dashboard?id=navikt_amt-tiltak
