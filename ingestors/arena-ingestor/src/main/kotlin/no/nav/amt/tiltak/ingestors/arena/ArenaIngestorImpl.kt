package no.nav.amt.tiltak.ingestors.arena

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.amt.tiltak.core.port.ArenaIngestor
import org.springframework.stereotype.Service

@Service
class ArenaIngestorImpl: ArenaIngestor {

    private val objectMapper = ObjectMapper()

    fun ingest(data: String) {
		println("Ingesting data: $data")
//        val dto = objectMapper.readValue(data, ArenaDataDTO::class.java)

        /**
         * - Deserialize
         * - Store
         */
    }


}
