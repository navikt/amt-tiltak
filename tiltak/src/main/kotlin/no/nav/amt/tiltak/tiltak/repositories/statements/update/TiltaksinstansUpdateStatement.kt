package no.nav.amt.tiltak.tiltak.repositories.statements.update

import no.nav.amt.tiltak.tiltak.dbo.TiltaksinstansDbo
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class TiltaksinstansUpdateStatement(
	val template: NamedParameterJdbcTemplate,
	val tiltaksinstans: TiltaksinstansDbo
) {

	//language=PostgreSQL
	private val sql = """
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

	fun exexute(): Int {
		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"navn" to tiltaksinstans.navn,
				"status" to tiltaksinstans.status?.name,
				"oppstart_dato" to tiltaksinstans.oppstartDato,
				"slutt_dato" to tiltaksinstans.sluttDato,
				"registrert_dato" to tiltaksinstans.registrertDato,
				"fremmote_dato" to tiltaksinstans.fremmoteDato,
				"modified_at" to tiltaksinstans.modifiedAt,
				"id" to tiltaksinstans.internalId
			)
		)

		return template.update(sql, parameters)
	}
}
