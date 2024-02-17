package no.nav.amt.tiltak.deltaker.repositories

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
open class SkjultDeltakerRepository(
	private val template: NamedParameterJdbcTemplate
) {
	fun slett(deltakerId: UUID) {
		val sql = """
			DELETE from skjult_deltaker WHERE deltaker_id = :deltakerId
		""".trimIndent()

		val parameters = sqlParameters(
			"deltakerId" to deltakerId,
		)

		template.update(sql, parameters)
	}
}
