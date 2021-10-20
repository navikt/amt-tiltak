package no.nav.amt.tiltak.ingestors.arena.processors

import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.dto.ArenaTiltak
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import org.springframework.stereotype.Component

@Component
class TiltakProcessor(
    repository: ArenaDataRepository,
    private val tiltakService: TiltakService
) : AbstractArenaProcessor(repository) {

    override fun insert(data: ArenaData) {
        val newFields = jsonObject(data.after, ArenaTiltak::class.java)

        tiltakService.addUpdateTiltak(
            newFields.TILTAKSKODE,
            newFields.TILTAKSNAVN,
            newFields.TILTAKSKODE
        )
    }

    override fun update(data: ArenaData) {
        TODO("Not yet implemented")
    }

    override fun delete(data: ArenaData) {
        TODO("Not yet implemented")
    }


}
