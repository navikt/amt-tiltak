package no.nav.amt.tiltak.deltaker.repositories

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDetaljerDbo
import no.nav.amt.tiltak.utils.getLocalDateTime
import no.nav.amt.tiltak.utils.getNullableLocalDate
import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class GetDeltakerDetaljerQuery(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		DeltakerDetaljerDbo(
			deltakerId = rs.getUUID("deltaker_id"),
			fornavn = rs.getString("fornavn"),
			mellomnavn = rs.getString("mellomnavn"),
			etternavn = rs.getString("etternavn"),
			fodselsnummer = rs.getString("fodselsnummer"),
			telefonnummer = rs.getString("telefonnummer"),
			epost = rs.getString("epost"),
			veilederNavn = rs.getString("veileder_navn"),
			veilederTelefonnummer = rs.getString("veileder_telefonnummer"),
			veilederEpost = rs.getString("veileder_epost"),
			startDato = rs.getNullableLocalDate("start_dato"),
			sluttDato = rs.getNullableLocalDate("slutt_dato"),
			registrertDato = rs.getLocalDateTime("registrert_dato"),
			status = Deltaker.Status.valueOf(rs.getString("status")),
			statusGyldigFra = rs.getLocalDateTime("status_gyldig_fra"),
			statusOpprettet = rs.getLocalDateTime("status_opprettet"),
			navEnhetNavn = rs.getString("nav_enhet_navn"),
			gjennomforingId = rs.getUUID("gjennomforing_id"),
			gjennomforingNavn = rs.getString("gjennomforing_navn"),
			gjennomforingStartDato = rs.getNullableLocalDate("gjennomforing_start_dato"),
			gjennomforingSluttDato = rs.getNullableLocalDate("gjennomforing_slutt_dato"),
			gjennomforingStatus = rs.getString("gjennomforing_status")?.let { Gjennomforing.Status.valueOf(it) },
			tiltakNavn = rs.getString("tiltak_navn"),
			tiltakKode = rs.getString("tiltak_kode"),
			virksomhetNavn = rs.getString("virksomhet_navn"),
			organisasjonNavn = rs.getString("organisasjon_navn")
		)
	}

	private val sql = """
		SELECT deltaker.id                  	AS deltaker_id,
			   deltaker.start_dato          	AS start_dato,
			   deltaker.slutt_dato          	AS slutt_dato,
			   deltaker_status.status       	AS status,
			   deltaker_status.gyldig_fra 		AS status_gyldig_fra,
			   deltaker_status.created_at		AS status_opprettet,
			   deltaker.registrert_dato     	AS registrert_dato,
			   bruker.fornavn               	AS fornavn,
			   bruker.mellomnavn            	AS mellomnavn,
			   bruker.etternavn             	AS etternavn,
			   bruker.fodselsnummer         	AS fodselsnummer,
			   bruker.telefonnummer         	AS telefonnummer,
			   bruker.epost                 	AS epost,
			   bruker.nav_enhet_id         		AS nav_enhet_id,
			   nav_enhet.navn					AS nav_enhet_navn,
			   nav_ansatt.navn           		AS veileder_navn,
			   nav_ansatt.telefonnummer     	AS veileder_telefonnummer,
			   nav_ansatt.epost             	AS veileder_epost,
			   gjennomforing.id             	AS gjennomforing_id,
			   gjennomforing.navn           	AS gjennomforing_navn,
			   gjennomforing.start_dato     	AS gjennomforing_start_dato,
			   gjennomforing.slutt_dato     	AS gjennomforing_slutt_dato,
			   gjennomforing.status         	AS gjennomforing_status,
			   tiltak.navn                  	AS tiltak_navn,
			   tiltak.type                  	AS tiltak_kode,
			   arrangor.navn					AS virksomhet_navn,
			   arrangor.overordnet_enhet_navn	AS organisasjon_navn
		FROM deltaker
				 LEFT JOIN bruker ON bruker.id = deltaker.bruker_id
				 LEFT JOIN nav_ansatt ON nav_ansatt.id = bruker.ansvarlig_veileder_id
				 LEFT JOIN nav_enhet ON nav_enhet.id = bruker.nav_enhet_id
				 LEFT JOIN gjennomforing ON gjennomforing.id = deltaker.gjennomforing_id
				 LEFT JOIN tiltak ON gjennomforing.tiltak_id = tiltak.id
				 LEFT JOIN deltaker_status ON deltaker_status.deltaker_id = deltaker.id
				 LEFT JOIN arrangor on gjennomforing.arrangor_id = arrangor.id
		WHERE deltaker.id = :deltakerId AND deltaker_status.aktiv
	""".trimIndent()

	fun query(deltakerId: UUID): DeltakerDetaljerDbo? {
		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"deltakerId" to deltakerId
			)
		)

		return template.query(
			sql,
			parameters,
			rowMapper
		).firstOrNull()
	}
}

