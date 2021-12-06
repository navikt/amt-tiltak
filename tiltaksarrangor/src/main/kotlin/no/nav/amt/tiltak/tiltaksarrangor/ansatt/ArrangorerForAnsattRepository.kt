package no.nav.amt.tiltak.tiltaksarrangor.ansatt.queries

import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class ArrangorerForAnsattRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		ArrangorForAnsattDbo(
			id = rs.getUUID("arrangor_id"),
			navn = rs.getString("arrangor_navn"),
			organisasjonsnummer = rs.getString("arrangor_organisasjonsnummer"),
			overordnetEnhetNavn = rs.getString("arrangor_overordnet_enhet_navn"),
			overordnetEnhetOrganisasjonsnummer = rs.getString("arrangor_overordnet_enhet_organisasjonsnummer"),
			rolle = rs.getString("rolle")
		)
	}

	private val sql = """
		SELECT arrangor.id                                   AS arrangor_id,
		       arrangor.navn                                 AS arrangor_navn,
		       arrangor.organisasjonsnummer                  AS arrangor_organisasjonsnummer,
		       arrangor.overordnet_enhet_navn                AS arrangor_overordnet_enhet_navn,
		       arrangor.overordnet_enhet_organisasjonsnummer AS arrangor_overordnet_enhet_organisasjonsnummer,
		       rolle.rolle                                     AS rolle
		FROM tiltaksarrangor_ansatt ansatt
		         JOIN tiltaksarrangor_ansatt_rolle rolle ON ansatt.id = rolle.ansatt_id
		         JOIN tiltaksarrangor arrangor ON rolle.tiltaksarrangor_id = arrangor.id
		where ansatt.personlig_ident = :personligIdent
	""".trimIndent()

	fun query(personIdent: String): List<ArrangorForAnsattDbo> {
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

data class ArrangorForAnsattDbo(
	val id: UUID,
	val navn: String,
	val organisasjonsnummer: String,
	val overordnetEnhetNavn: String?,
	val overordnetEnhetOrganisasjonsnummer: String?,
	val rolle: String
)
