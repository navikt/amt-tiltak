package no.nav.amt.tiltak.tiltak.repositories.statements.update

import no.nav.amt.tiltak.tiltak.deltaker.dbo.DeltakerDbo
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class DeltakerUpdateStatement(
	private val template: NamedParameterJdbcTemplate,
	private val deltaker: DeltakerDbo
) {

	//language=PostgreSQL
	private val sql = """
		UPDATE deltaker
		SET status        = :deltakerStatus,
			oppstart_dato = :oppstartDato,
			slutt_dato    = :sluttDato,
			modified_at   = :modifiedAt
		WHERE id = :deltakerInternalId
	""".trimIndent()

	fun execute(): Int {
		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"deltakerStatus" to deltaker.status,
				"oppstartDato" to deltaker.deltakerOppstartsdato,
				"sluttDato" to deltaker.deltakerSluttdato,
				"modifiedAt" to deltaker.modifiedAt,
				"deltakerInternalId" to deltaker.internalId
			)
		)

		return template.update(sql, parameters)

	}

}
