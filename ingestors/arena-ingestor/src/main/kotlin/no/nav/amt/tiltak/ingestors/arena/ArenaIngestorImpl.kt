package no.nav.amt.tiltak.ingestors.arena

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.amt.tiltak.core.port.ArenaIngestor
import no.nav.amt.tiltak.ingestors.arena.dto.StringArenaKafkaDto
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import org.springframework.stereotype.Service

@Service
internal class ArenaIngestorImpl(
	private val arenaDataRepository: ArenaDataRepository
) : ArenaIngestor {

	private val mapper = jacksonObjectMapper()

	override fun ingest(data: String) {
		val pojo = mapper.readValue(data, StringArenaKafkaDto::class.java).toArenaData()
		arenaDataRepository.upsert(pojo)
	}
}
