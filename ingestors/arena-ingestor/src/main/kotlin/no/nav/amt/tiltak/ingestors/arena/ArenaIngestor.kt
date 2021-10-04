package no.nav.amt.tiltak.ingestors.arena

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.amt.tiltak.ingestors.arena.dto.ArenaDataDTO
import org.springframework.stereotype.Service

@Service
class ArenaIngestor {

    private val objectMapper = ObjectMapper()

    fun ingest(data: String) {
        val dto = objectMapper.readValue(data, ArenaDataDTO::class.java)

        /**
         * - Deserialize
         * - Store
         */

    }


}
