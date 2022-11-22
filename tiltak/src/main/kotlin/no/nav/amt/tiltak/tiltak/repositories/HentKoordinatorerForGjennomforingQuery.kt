package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.core.port.Person
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class HentKoordinatorerForGjennomforingQuery(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		 Person(
			fornavn = rs.getString("fornavn"),
			mellomnavn = rs.getString("mellomnavn"),
			etternavn = rs.getString("etternavn"),
			telefonnummer = null,
			diskresjonskode = null
		)
	}

	private val sql = """
		SELECT aagt.gjennomforing_id AS gjennomforingId,
			   a.fornavn    AS fornavn,
			   a.mellomnavn AS mellomnavn,
			   a.etternavn  AS etternavn
		FROM arrangor_ansatt a
				 INNER JOIN arrangor_ansatt_rolle aar on a.id = aar.ansatt_id
				 INNER JOIN arrangor_ansatt_gjennomforing_tilgang aagt on aar.ansatt_id = aagt.ansatt_id
		WHERE aagt.gjennomforing_id = :gjennomforingId
		  AND aar.rolle = 'KOORDINATOR'
		  AND aagt.gyldig_fra < CURRENT_TIMESTAMP
		  AND aagt.gyldig_til > CURRENT_TIMESTAMP
	""".trimIndent()

	fun query(gjennomforingId: UUID): Set<Person> {
		return template.query(
			sql,
			sqlParameters("gjennomforingId" to gjennomforingId),
			rowMapper
		).toSet()
	}
}
