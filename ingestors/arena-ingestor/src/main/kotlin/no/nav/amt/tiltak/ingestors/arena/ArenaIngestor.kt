package no.nav.amt.tiltak.ingestors.arena

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class ArenaIngestor {

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
