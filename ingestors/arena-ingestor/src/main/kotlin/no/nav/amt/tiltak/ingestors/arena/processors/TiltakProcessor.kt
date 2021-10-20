package no.nav.amt.tiltak.ingestors.arena.processors

import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TiltakProcessor(
    repository: ArenaDataRepository,
    val tiltakService: TiltakService
) : AbstractArenaProcessor(repository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun insert(data: ArenaData) {
        TODO("Not yet implemented")
    }

    override fun update(data: ArenaData) {
        TODO("Not yet implemented")
    }

    override fun delete(data: ArenaData) {
        TODO("Not yet implemented")
    }


}
