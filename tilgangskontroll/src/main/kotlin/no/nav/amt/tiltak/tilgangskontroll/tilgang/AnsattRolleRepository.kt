package no.nav.amt.tiltak.tilgangskontroll.tilgang

import no.nav.amt.tiltak.common.db_utils.getUUID
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class AnsattRolleRepository(
	private val template: NamedParameterJdbcTemplate
) {

	fun hentArrangorIderForAnsatt(ansattId: UUID): List<UUID> {
		val parameters = MapSqlParameterSource().addValues(mapOf(
			"ansattId" to ansattId
		))

		return template.query(
			"SELECT arrangor_id FROM arrangor_ansatt_rolle WHERE ansatt_id = :ansattId",
			parameters
		) { rs, _ ->
			rs.getUUID("arrangor_id")
		}
	}

}
