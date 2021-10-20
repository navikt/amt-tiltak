package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.tiltak.dbo.TiltakDbo
import no.nav.amt.tiltak.tiltak.repositories.statements.insert.TiltakInsertStatement
import no.nav.amt.tiltak.tiltak.repositories.statements.parts.tiltak.TiltakArenaIdEqualsQueryPart
import no.nav.amt.tiltak.tiltak.repositories.statements.parts.tiltak.TiltakExternalIdEqualsQueryPart
import no.nav.amt.tiltak.tiltak.repositories.statements.queries.GetTiltakQueryStatement
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*

@Repository
open class TiltakRepository(
    private val template: NamedParameterJdbcTemplate
) {

    fun insert(arenaId: String, navn: String, kode: String): TiltakDbo {
        val externalId = TiltakInsertStatement(
            template = template,
            arenaId = arenaId,
            navn = navn,
            kode = kode
        ).execute()

        return get(externalId)
            ?: throw NoSuchElementException("Tiltak med id $externalId finnes ikke")
    }

    fun get(id: UUID): TiltakDbo? {
        return GetTiltakQueryStatement(template)
            .addPart(TiltakExternalIdEqualsQueryPart(id))
            .execute()
            .firstOrNull()
    }

    fun getByArenaId(arenaId: String): TiltakDbo? {
        return GetTiltakQueryStatement(template)
            .addPart(TiltakArenaIdEqualsQueryPart(arenaId))
            .execute()
            .firstOrNull()
    }
}
