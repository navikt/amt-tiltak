package no.nav.amt.tiltak.tiltak.deltaker.queries

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.tiltak.utils.getLocalDate
import no.nav.amt.tiltak.tiltak.utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
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
			veilederFornavn = rs.getString("veileder_fornavn"),
			veilederEtternavn = rs.getString("veileder_etternavn"),
			veilederTelefonnummer = rs.getString("veileder_telefonnummer"),
			veilederEpost = rs.getString("veileder_epost"),
			oppstartDato = rs.getLocalDate("oppstart_dato"),
			sluttDato = rs.getLocalDate("slutt_dato"),
			status = rs.getString("status")?.let { Deltaker.Status.valueOf(it) },
			tiltakInstansId = rs.getUUID("tiltak_instans_id"),
			tiltakInstansNavn = rs.getString("tiltak_instans_navn"),
			tiltakInstansOppstartDato = rs.getLocalDate("tiltak_instans_oppstart_dato"),
			tiltakInstansSluttDato = rs.getLocalDate("tiltak_instans_slutt_dato"),
			tiltakInstansStatus = rs.getString("tiltak_instans_status").let { TiltakInstans.Status.valueOf(it) },
			tiltakNavn = rs.getString("tiltak_navn"),
			tiltakKode = rs.getString("tiltak_kode"),
		)
	}

	private val sql = """
		SELECT deltaker.id                  AS deltaker_id,
			   bruker.fornavn               AS fornavn,
			   bruker.mellomnavn            AS mellomnavn,
			   bruker.etternavn             AS etternavn,
			   bruker.fodselsnummer         AS fodselsnummer,
			   bruker.telefonnummer         AS telefonnummer,
			   bruker.epost                 AS epost,
			   nav_ansatt.fornavn           AS veileder_fornavn,
			   nav_ansatt.etternavn         AS veileder_etternavn,
			   nav_ansatt.telefonnummer     AS veileder_telefonnummer,
			   nav_ansatt.epost             AS veileder_epost,
			   deltaker.oppstart_dato       AS oppstart_dato,
			   deltaker.slutt_dato          AS slutt_dato,
			   deltaker.status              AS status,
			   tiltaksinstans.id            AS tiltak_instans_id,
			   tiltaksinstans.navn          AS tiltak_instans_navn,
			   tiltaksinstans.oppstart_dato AS tiltak_instans_oppstart_dato,
			   tiltaksinstans.slutt_dato    AS tiltak_instans_slutt_dato,
			   tiltaksinstans.status        AS tiltak_instans_status,
			   tiltak.navn                  AS tiltak_navn,
			   tiltak.type                  AS tiltak_kode
		FROM deltaker
				 JOIN bruker ON bruker.id = deltaker.bruker_id
				 JOIN nav_ansatt ON nav_ansatt.id = bruker.ansvarlig_veileder_id
				 JOIN tiltaksinstans ON tiltaksinstans.id = deltaker.tiltaksinstans_id
				 JOIN tiltak ON tiltaksinstans.tiltak_id = tiltak.id
		WHERE deltaker.id = :deltakerId
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

data class DeltakerDetaljerDbo(
	val deltakerId: UUID,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val fodselsnummer: String,
	val telefonnummer: String?,
	val epost: String?,
	val veilederFornavn: String?,
	val veilederEtternavn: String?,
	val veilederTelefonnummer: String?,
	val veilederEpost: String?,
	val oppstartDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: Deltaker.Status?,
	val tiltakInstansId: UUID,
	val tiltakInstansNavn: String,
	val tiltakInstansOppstartDato: LocalDate?,
	val tiltakInstansSluttDato: LocalDate?,
	val tiltakInstansStatus: TiltakInstans.Status,
	val tiltakNavn: String,
	val tiltakKode: String
)

