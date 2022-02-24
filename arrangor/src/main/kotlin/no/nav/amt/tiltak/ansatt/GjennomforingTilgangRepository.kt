package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class GjennomforingTilgangRepository(
	private val template: NamedParameterJdbcTemplate
) {

	fun hentGjennomforingerForAnsatt(ansattId: UUID): List<UUID> {
		return template.query(
			"SELECT gjennomforing_id FROM arrangor_ansatt_gjennomforing_tilgang WHERE ansatt_id = :ansattId",
			MapSqlParameterSource().addValue("ansattId", ansattId),
		) { rs, _ -> rs.getUUID("gjennomforing_id") }
	}

}
