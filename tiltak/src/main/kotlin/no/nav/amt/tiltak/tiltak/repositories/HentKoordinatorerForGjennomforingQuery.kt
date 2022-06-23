package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class HentKoordinatorerForGjennomforingQuery(
	private val template: NamedParameterJdbcTemplate
) {

	private val sql = """
		SELECT na.navn as navn
			FROM tiltaksansavarlig_gjennomforing_tilgang tilgang
					 INNER JOIN nav_ansatt na ON na.id = tilgang.nav_ansatt_id
			WHERE tilgang.gjennomforing_id = :gjennomforingId
			  AND tilgang.gyldig_til > current_timestamp
	""".trimIndent()

	fun query(gjennomforingId: UUID): List<String> {
		return template.query(
			sql,
			sqlParameters("gjennomforingId" to gjennomforingId)
		) { rs, _ -> rs.getString("navn") }
	}

}
