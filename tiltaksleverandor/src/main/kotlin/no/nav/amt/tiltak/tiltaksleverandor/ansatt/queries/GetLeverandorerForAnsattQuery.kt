package no.nav.amt.tiltak.tiltaksleverandor.ansatt.queries

import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class GetLeverandorerForAnsattQuery(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		LeverandorForAnsattDbo(
			id = rs.getUUID("leverandor_id"),
			navn = rs.getString("leverandor_navn"),
			organisasjonsnummer = rs.getString("leverandor_organisasjonsnummer"),
			overordnetEnhetNavn = rs.getString("leverandor_overordnet_enhet_navn"),
			overordnetEnhetOrganisasjonsnummer = rs.getString("leverandor_overordnet_enhet_organisasjonsnummer"),
			rolle = rs.getString("rolle")
		)
	}

	private val sql = """
		SELECT leverandor.id                                   AS leverandor_id,
		       leverandor.navn                                 AS leverandor_navn,
		       leverandor.organisasjonsnummer                  AS leverandor_organisasjonsnummer,
		       leverandor.overordnet_enhet_navn                AS leverandor_overordnet_enhet_navn,
		       leverandor.overordnet_enhet_organisasjonsnummer AS leverandor_overordnet_enhet_organisasjonsnummer,
		       rolle.rolle                                     AS rolle
		FROM tiltaksleverandor_ansatt ansatt
		         JOIN tiltaksleverandor_ansatt_rolle rolle ON ansatt.id = rolle.ansatt_id
		         JOIN tiltaksleverandor leverandor ON rolle.tiltaksleverandor_id = leverandor.id
		where ansatt.personlig_ident = :personligIdent
	""".trimIndent()

	fun query(personIdent: String): List<LeverandorForAnsattDbo> {
		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"personligIdent" to personIdent
			)
		)

		return template.query(
			sql,
			parameters,
			rowMapper
		)
	}

}

data class LeverandorForAnsattDbo(
	val id: UUID,
	val navn: String,
	val organisasjonsnummer: String,
	val overordnetEnhetNavn: String?,
	val overordnetEnhetOrganisasjonsnummer: String?,
	val rolle: String
)
