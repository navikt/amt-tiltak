package no.nav.amt.tiltak.ingestors.arena

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.amt.tiltak.core.port.ArenaIngestor
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.dto.StringArenaKafkaDto
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import org.springframework.stereotype.Service

@Service
class ArenaIngestorImpl(
    private val arenaDataRepository: ArenaDataRepository
) : ArenaIngestor {

    override fun ingest(data: String) {
        val pojo = toPojo(data)
        arenaDataRepository.upsert(pojo)
    }

    private fun toPojo(data: String): ArenaData {
        val mapper = jacksonObjectMapper()
        return mapper.readValue(data, StringArenaKafkaDto::class.java).toArenaData()
    }
}
