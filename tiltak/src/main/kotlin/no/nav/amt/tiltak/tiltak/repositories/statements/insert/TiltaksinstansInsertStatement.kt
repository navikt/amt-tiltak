package no.nav.amt.tiltak.tiltak.repositories.statements.insert

import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class TiltaksinstansInsertStatement(
	val template: NamedParameterJdbcTemplate,
	val arenaId: Int,
	val tiltakId: UUID,
	val tiltaksleverandorId: UUID,
	val navn: String,
	val status: TiltakInstans.Status? = null,
	val oppstartDato: LocalDate? = null,
	val sluttDato: LocalDate? = null,
	val registrertDato: LocalDateTime? = null,
	val fremmoteDato: LocalDateTime? = null
) {

	//language=PostgreSQL
	private val sql = """
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

	fun execute(): UUID {
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

		return externalId
	}
}
