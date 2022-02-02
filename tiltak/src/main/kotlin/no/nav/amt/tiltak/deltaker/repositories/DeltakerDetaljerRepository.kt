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
			statusEndretDato = rs.getLocalDateTime("status_endret_dato"),
			navKontorNavn = rs.getString("nav_kontor_navn"),
			gjennomforingId = rs.getUUID("gjennomforing_id"),
			gjennomforingNavn = rs.getString("gjennomforing_navn"),
			gjennomforingStartDato = rs.getNullableLocalDate("gjennomforing_start_dato"),
			gjennomforingSluttDato = rs.getNullableLocalDate("gjennomforing_slutt_dato"),
			gjennomforingStatus = rs.getString("gjennomforing_status")?.let { Gjennomforing.Status.valueOf(it) },
			tiltakNavn = rs.getString("tiltak_navn"),
			tiltakKode = rs.getString("tiltak_kode"),
		)
	}

	private val sql = """
		SELECT deltaker.id                  AS deltaker_id,
			   deltaker.start_dato          AS start_dato,
			   deltaker.slutt_dato          AS slutt_dato,
			   deltaker_status.status       AS status,
			   deltaker_status.endret_dato 	AS status_endret_dato,
			   deltaker.registrert_dato     AS registrert_dato,
			   bruker.fornavn               AS fornavn,
			   bruker.mellomnavn            AS mellomnavn,
			   bruker.etternavn             AS etternavn,
			   bruker.fodselsnummer         AS fodselsnummer,
			   bruker.telefonnummer         AS telefonnummer,
			   bruker.epost                 AS epost,
			   bruker.nav_kontor_id         AS nav_kontor_id,
			   nav_kontor.navn				AS nav_kontor_navn,
			   nav_ansatt.navn           	AS veileder_navn,
			   nav_ansatt.telefonnummer     AS veileder_telefonnummer,
			   nav_ansatt.epost             AS veileder_epost,
			   gjennomforing.id             AS gjennomforing_id,
			   gjennomforing.navn           AS gjennomforing_navn,
			   gjennomforing.start_dato     AS gjennomforing_start_dato,
			   gjennomforing.slutt_dato     AS gjennomforing_slutt_dato,
			   gjennomforing.status         AS gjennomforing_status,
			   tiltak.navn                  AS tiltak_navn,
			   tiltak.type                  AS tiltak_kode
		FROM deltaker
				 LEFT JOIN bruker ON bruker.id = deltaker.bruker_id
				 LEFT JOIN nav_ansatt ON nav_ansatt.id = bruker.ansvarlig_veileder_id
				 LEFT JOIN nav_kontor ON nav_kontor.id = bruker.nav_kontor_id
				 LEFT JOIN gjennomforing ON gjennomforing.id = deltaker.gjennomforing_id
				 LEFT JOIN tiltak ON gjennomforing.tiltak_id = tiltak.id
				 LEFT JOIN deltaker_status ON deltaker_status.deltaker_id = deltaker.id
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

