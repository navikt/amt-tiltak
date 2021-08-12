# AMT Tiltak

## Kjør opp lokalt

For å kjøre opp lokalt så må det startes en postgres database med følgende paramtere:

```
URL         = jdbc:postgresql://localhost:5454/amt-tiltak-db
USERNAME    = postgres
PASSWORD    = qwerty
```

Dette kan gjøres ved bruk av docker-compose. For å starte opp databasen så kjør `docker-compose up -d`.
Verifiser at databasen kjører med `docker ps`.

Start applikasjonen ved å kjøre **main** funksjonen i `application/src/test/kotlin/LocalApplication.kt
`

## Slett data i lokal database
1. Stopp postgres med `docker-compose down`.
2. Slett den lagrede dataen med `docker volume rm amt-tiltak_postgres-db-volume` 