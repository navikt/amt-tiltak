package no.nav.amt.tiltak.tiltak.repositories.statements.update

import no.nav.amt.tiltak.tiltak.dbo.TiltakDbo
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class TiltakUpdateStatement(
	val template: NamedParameterJdbcTemplate,
	val tiltak: TiltakDbo
) {

	//language=PostgreSQL
	private val sql = """
		UPDATE tiltak
		SET navn        = :navn,
			type        = :type,
			modified_at = :modifiedAt
		WHERE id = :id
	""".trimIndent()

	fun execute(): Int {
		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"navn" to tiltak.navn,
				"type" to tiltak.type,
				"modifiedAt" to tiltak.modifiedAt
			)
		)

		return template.update(sql, parameters)

	}

}
