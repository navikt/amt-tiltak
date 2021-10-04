# TODOs for ArenaIngestor

- Dum ingestor for å legge data inn i en arena_ingest tabellen i databasen fra Kafka
- En Schedule jobb som en gang hvert 30 sekund henter alle felt i arena_ingest som ikke 
er INGESTED eller FAILED og prøver å legge dette inn i vår datastruktur
- Implementere ingestbit
- Implementere updatebit
- Implementere deletebit(?)
