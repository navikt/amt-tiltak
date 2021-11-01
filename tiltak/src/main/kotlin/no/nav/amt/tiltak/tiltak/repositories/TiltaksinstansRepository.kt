package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.tiltak.dbo.TiltaksinstansDbo
import no.nav.amt.tiltak.tiltak.repositories.statements.parts.tiltaksinstans.TiltaksinstansArenaIdEqualsQueryPart
import no.nav.amt.tiltak.tiltak.repositories.statements.parts.tiltaksinstans.TiltaksinstansExternalIdEqualQueryPart
import no.nav.amt.tiltak.tiltak.repositories.statements.queries.GetTiltaksinstansQueryStatement
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Repository
open class TiltaksinstansRepository(private val template: NamedParameterJdbcTemplate) {

    fun insert(
        arenaId: Int,
        tiltakId: UUID,
        tiltaksleverandorId: UUID,
        navn: String,
        status: TiltakInstans.Status?,
        oppstartDato: LocalDate?,
        sluttDato: LocalDate?,
        registrertDato: LocalDateTime?,
        fremmoteDato: LocalDateTime?
    ): TiltaksinstansDbo {

        //language=PostgreSQL
        val sql = """
		INSERT INTO tiltaksinstans(external_id, arena_id, tiltak_id, tiltaksleverandor_id, navn, status, oppstart_dato,
                           slutt_dato, registrert_dato, fremmote_dato)
		VALUES (:externalId,
				:arenaId,
				(SELECT id FROM tiltak where tiltak.external_id = :tiltakId),
				(SELECT id FROM tiltaksleverandor where tiltaksleverandor.external_id = :tiltaksleverandorId),
				:navn,
				:status,
				:oppstartDato,
				:sluttDato,
				:registrertDato,
				:fremmoteDato)
	""".trimIndent()

        val externalId = UUID.randomUUID()

        val parameters = MapSqlParameterSource().addValues(
            mapOf(
                "externalId" to externalId,
                "arenaId" to arenaId,
                "tiltakId" to tiltakId,
                "tiltaksleverandorId" to tiltaksleverandorId,
                "navn" to navn,
                "status" to status?.name,
                "oppstartDato" to oppstartDato,
                "sluttDato" to sluttDato,
                "registrertDato" to registrertDato,
                "fremmoteDato" to fremmoteDato
            )
        )

        template.update(sql, parameters)

        return get(externalId)
            ?: throw NoSuchElementException("Tiltak med id $externalId finnes ikke")
    }

    fun update(tiltaksinstans: TiltaksinstansDbo): TiltaksinstansDbo {

        //language=PostgreSQL
        val sql = """
			UPDATE tiltaksinstans
			SET navn            = :navn,
				status          = :status,
				oppstart_dato   = :oppstart_dato,
				slutt_dato      = :slutt_dato,
				registrert_dato = :registrert_dato,
				fremmote_dato   = :fremmote_dato,
				modified_at     = :modified_at
			WHERE id = :id
		""".trimIndent()

        val parameters = MapSqlParameterSource().addValues(
            mapOf(
                "navn" to tiltaksinstans.navn,
                "status" to tiltaksinstans.status?.name,
                "oppstart_dato" to tiltaksinstans.oppstartDato,
                "slutt_dato" to tiltaksinstans.sluttDato,
                "registrert_dato" to tiltaksinstans.registrertDato,
                "fremmote_dato" to tiltaksinstans.fremmoteDato,
                "modified_at" to tiltaksinstans.modifiedAt,
                "id" to tiltaksinstans.id
            )
        )

        template.update(sql, parameters)

        return get(tiltaksinstans.externalId)
            ?: throw NoSuchElementException("Tiltak med id ${tiltaksinstans.externalId} finnes ikke")
    }


    fun get(id: UUID): TiltaksinstansDbo? {
        return GetTiltaksinstansQueryStatement(template)
            .addPart(TiltaksinstansExternalIdEqualQueryPart(id))
            .execute()
            .firstOrNull()
    }

    fun getByArenaId(arenaId: Int): TiltaksinstansDbo? {
        return GetTiltaksinstansQueryStatement(template)
            .addPart(TiltaksinstansArenaIdEqualsQueryPart(arenaId))
            .execute()
            .firstOrNull()
    }
}
