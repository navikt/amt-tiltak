package no.nav.amt.tiltak.tiltak.repositories.statements.insert

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.util.*

class DeltakerInsertStatement(
	private val template: NamedParameterJdbcTemplate,
	private val brukerId: Int,
	private val tiltaksinstansId: UUID,
	private val oppstartsdato: LocalDate?,
	private val sluttdato: LocalDate?,
	private val status: Deltaker.Status = Deltaker.Status.NY_BRUKER
) {

	//language=PostgreSQL
	private val sql = """
		INSERT INTO deltaker(external_id, bruker_id, tiltaksinstans_id, oppstart_dato, slutt_dato, status)
		VALUES (:externalId,
				:brukerId,
				(SELECT id from tiltaksinstans where external_id = :tiltaksinstansExternalId),
				:oppstartsdato,
				:sluttdato
				:status)
	""".trimIndent()

	fun execute(): UUID {
		val externalId = UUID.randomUUID()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"externalId" to externalId,
				"brukerId" to brukerId,
				"tiltaksinstansExternalId" to tiltaksinstansId,
				"oppstart_dato" to oppstartsdato,
				"slutt_dato" to sluttdato,
				"status" to status
			)
		)

		template.update(sql, parameters)

		return externalId
	}
}
