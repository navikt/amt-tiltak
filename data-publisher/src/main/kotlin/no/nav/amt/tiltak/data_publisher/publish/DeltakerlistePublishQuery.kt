package no.nav.amt.tiltak.data_publisher.publish

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getLocalDate
import no.nav.amt.tiltak.common.db_utils.getNullableLocalDate
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.data_publisher.model.DeltakerlisteArrangorDto
import no.nav.amt.tiltak.data_publisher.model.DeltakerlistePublishDto
import no.nav.amt.tiltak.data_publisher.model.DeltakerlisteStatus
import no.nav.amt.tiltak.data_publisher.model.TiltakDto
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.util.UUID

class DeltakerlistePublishQuery(
	private val template: NamedParameterJdbcTemplate
) {

	fun get(id: UUID): DeltakerlistePublishDto? {
		val deltakerliste = getDeltakerliste(id) ?: return null

		return DeltakerlistePublishDto(
			id = deltakerliste.id,
			navn = deltakerliste.navn,
			status = toDeltakerlisteStatus(deltakerliste.status),
			tiltak = TiltakDto(
				navn = deltakerliste.tiltakNavn,
				type = deltakerliste.tiltakType
			),
			arrangor = DeltakerlisteArrangorDto(
				id = deltakerliste.arrangorId,
				organisasjonsnummer = deltakerliste.arrangorOrgNr,
				navn = deltakerliste.arrangorNavn
			),
			startDato = deltakerliste.startDato,
			sluttDato = deltakerliste.sluttDato,
			erKurs = deltakerliste.erKurs
		)
	}

	private fun getDeltakerliste(gjennomforingId: UUID): Deltakerliste? {
		val sql = """
			select gjennomforing.id,
				   gjennomforing.arrangor_id,
				   gjennomforing.navn           as navn,
				   gjennomforing.status,
				   tiltak.type                  as tiltak_type,
				   tiltak.navn                  as tiltak_navn,
				   arrangor.organisasjonsnummer as arrangor_organisasjonsnummer,
				   arrangor.navn                as arrangor_navn,
				   gjennomforing.start_dato,
				   gjennomforing.slutt_dato,
				   gjennomforing.er_kurs
			from gjennomforing
					 left join tiltak on gjennomforing.tiltak_id = tiltak.id
					 left join arrangor on gjennomforing.arrangor_id = arrangor.id
			where gjennomforing.id = :gjennomforingId
		""".trimIndent()

		return template.query(
			sql,
			sqlParameters("gjennomforingId" to gjennomforingId),
			Deltakerliste.rowMapper
		).firstOrNull()
	}

	private fun toDeltakerlisteStatus(status: String): DeltakerlisteStatus {
		return when (status) {
			"PLANLAGT" -> DeltakerlisteStatus.PLANLAGT
			"GJENNOMFORES" -> DeltakerlisteStatus.GJENNOMFORES
			"AVSLUTTET" -> DeltakerlisteStatus.AVSLUTTET
			else -> throw IllegalStateException("Ukjent status: $status")
		}
	}

	private data class Deltakerliste(
		val id: UUID,
		val arrangorId: UUID,
		val navn: String,
		val status: String,
		val tiltakType: String,
		val tiltakNavn: String,
		val arrangorOrgNr: String,
		val arrangorNavn: String,
		val startDato: LocalDate,
		val sluttDato: LocalDate?,
		val erKurs: Boolean
	) {
		companion object {
			val rowMapper = RowMapper { rs, _ ->
				Deltakerliste(
					id = rs.getUUID("id"),
					arrangorId = rs.getUUID("arrangor_id"),
					navn = rs.getString("navn"),
					status = rs.getString("status"),
					tiltakType = rs.getString("tiltak_type"),
					tiltakNavn = rs.getString("tiltak_navn"),
					arrangorOrgNr = rs.getString("arrangor_organisasjonsnummer"),
					arrangorNavn = rs.getString("arrangor_navn"),
					startDato = rs.getLocalDate("start_dato"),
					sluttDato = rs.getNullableLocalDate("slutt_dato"),
					erKurs = rs.getBoolean("er_kurs")
				)
			}
		}
	}
}
