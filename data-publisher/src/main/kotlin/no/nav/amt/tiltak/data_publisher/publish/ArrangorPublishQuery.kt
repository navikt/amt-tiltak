package no.nav.amt.tiltak.data_publisher.publish

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getNullableString
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.data_publisher.model.ArrangorPublishDto
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class ArrangorPublishQuery(
	private val template: NamedParameterJdbcTemplate
) {

	fun execute(id: UUID): ArrangorPublishDto {
		val arrangor = getArrangor(id)
		val deltakerlister = getDeltakerlisterForArrangor(id)

		return ArrangorPublishDto(
			id = arrangor.id,
			navn = arrangor.navn,
			organisasjonsnummer = arrangor.organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = arrangor.overordnetEnhetOrganisasjonsnummer,
			overordnetEnhetNavn = arrangor.overordnetEnhetNavn,
			deltakerlister = deltakerlister
		)
	}

	private fun getArrangor(id: UUID): ArrangorDbo {
		return template.query(
			"SELECT * FROM arrangor WHERE id = :id",
			sqlParameters("id" to id),
			ArrangorDbo.rowMapper
		).first()
	}

	private fun getDeltakerlisterForArrangor(arrangorId: UUID): List<UUID> {
		return template.query(
			"SELECT id FROM gjennomforing where arrangor_id = :arrangorId",
			sqlParameters("arrangorId" to arrangorId)
		) { rs, _ -> rs.getUUID("id") }
	}

	private data class ArrangorDbo(
		val id: UUID,
		val navn: String,
		val organisasjonsnummer: String,
		val overordnetEnhetNavn: String?,
		val overordnetEnhetOrganisasjonsnummer: String?
	) {
		companion object {

			val rowMapper = RowMapper { rs, _ ->
				ArrangorDbo(
					id = rs.getUUID("id"),
					navn = rs.getString("navn"),
					organisasjonsnummer = rs.getString("organisasjonsnummer"),
					overordnetEnhetNavn = rs.getNullableString("overordnet_enhet_navn"),
					overordnetEnhetOrganisasjonsnummer = rs.getNullableString("overordnet_enhet_organisasjonsnummer")
				)
			}

		}
	}

}
