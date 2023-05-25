package no.nav.amt.tiltak.data_publisher.publish

import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getNullableString
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.data_publisher.model.ArrangorPublishDto
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.UUID

class ArrangorPublishQuery(
	private val template: NamedParameterJdbcTemplate,
	private val amtArrangorClient: AmtArrangorClient
) {

	fun get(id: UUID): ArrangorPublishDto {
		val arrangor = getArrangor(id)

		val overordnetArrangorId = arrangor.overordnetEnhetOrganisasjonsnummer?.let { getOverordnetArrangorId(it) }

		return ArrangorPublishDto(
			id = arrangor.id,
			navn = arrangor.navn,
			organisasjonsnummer = arrangor.organisasjonsnummer,
			overordnetArrangorId = overordnetArrangorId
		)
	}

	private fun getArrangor(id: UUID): ArrangorDbo {
		return template.query(
			"SELECT * FROM arrangor WHERE id = :id",
			sqlParameters("id" to id),
			ArrangorDbo.rowMapper
		).first()
	}

	private fun getOverordnetArrangorId(organisasjonsnummer: String): UUID {
		return getArrangorByOrgNr(organisasjonsnummer)?.id
			?: createArrangor(organisasjonsnummer)
	}

	private fun createArrangor(orgNr: String): UUID {
		val sql = """
			INSERT INTO arrangor(id, navn, organisasjonsnummer, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn)
			VALUES (:id,
					:navn,
					:organisasjonsnummer,
					:overordnetEnhetOrganisasjonsnummer,
					:overordnetEnhetNavn)
			ON CONFLICT (organisasjonsnummer) DO UPDATE SET navn = :navn,
															overordnet_enhet_navn = :overordnetEnhetNavn,
															overordnet_enhet_organisasjonsnummer = :overordnetEnhetOrganisasjonsnummer
		""".trimIndent()

		amtArrangorClient.hentArrangor(orgNr)?.let { arrangor ->
			val params = sqlParameters(
				"id" to arrangor.id,
				"navn" to arrangor.navn,
				"organisasjonsnummer" to arrangor.organisasjonsnummer,
				"overordnetEnhetOrganisasjonsnummer" to arrangor.overordnetArrangor?.organisasjonsnummer,
				"overordnetEnhetNavn" to arrangor.overordnetArrangor?.navn
			)

			template.update(sql, params)
		}

		return getArrangorByOrgNr(orgNr)?.id
			?: throw IllegalStateException("Forventet at organisasjon med $orgNr eksisterer")
	}

	private fun getArrangorByOrgNr(orgNr: String): ArrangorDbo? {
		return template.query(
			"SELECT * FROM arrangor WHERE organisasjonsnummer = :orgNr",
			sqlParameters("orgNr" to orgNr),
			ArrangorDbo.rowMapper
		).firstOrNull()
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
