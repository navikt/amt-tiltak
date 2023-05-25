package no.nav.amt.tiltak.data_publisher.publish

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getLocalDate
import no.nav.amt.tiltak.common.db_utils.getNullableLocalDate
import no.nav.amt.tiltak.common.db_utils.getNullableLocalDateTime
import no.nav.amt.tiltak.common.db_utils.getNullableString
import no.nav.amt.tiltak.common.db_utils.getNullableUUID
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.data_publisher.model.DeltakerKontaktinformasjonDto
import no.nav.amt.tiltak.data_publisher.model.DeltakerNavVeilederDto
import no.nav.amt.tiltak.data_publisher.model.DeltakerPersonaliaDto
import no.nav.amt.tiltak.data_publisher.model.DeltakerPublishDto
import no.nav.amt.tiltak.data_publisher.model.DeltakerSkjultDto
import no.nav.amt.tiltak.data_publisher.model.DeltakerStatusDto
import no.nav.amt.tiltak.data_publisher.model.Navn
import no.nav.amt.tiltak.data_publisher.model.PublishState
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class DeltakerPublishQuery(
	private val template: NamedParameterJdbcTemplate
) {

	fun get(id: UUID): Either<PublishState, DeltakerPublishDto> {
		val deltaker = getDeltaker(id) ?: return PublishState.PUBLISH_TOMBSTONE.left()

		if (deltaker.status == null) return PublishState.DONT_PUBLISH.left()

		return DeltakerPublishDto(
			deltaker.id,
			deltakerlisteId = deltaker.deltakerlisteId,
			personalia = DeltakerPersonaliaDto(
				personident = deltaker.personligIdent,
				navn = Navn(
					fornavn = deltaker.fornavn,
					mellomnavn = deltaker.mellomnavn,
					etternavn = deltaker.etternavn
				),
				kontaktinformasjon = DeltakerKontaktinformasjonDto(
					telefonnummer = deltaker.telefonnummer,
					epost = deltaker.epost
				),
				skjermet = deltaker.skjermet
			),
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
			oppstartsdato = deltaker.startDato,
			sluttdato = deltaker.sluttDato,
			innsoktDato = deltaker.registrertDato,
			bestillingTekst = deltaker.innsokBegrunnelse,
			navKontor = deltaker.navEnhetNavn,
			navVeileder = deltaker.navAnsattId?.let {
				DeltakerNavVeilederDto(
					id = deltaker.navAnsattId,
					navn = deltaker.navAnsattNavn!!,
					epost = deltaker.navAnsattEpost
				)
			},
			status = DeltakerStatusDto(
				type = DeltakerStatus.Type.valueOf(deltaker.status),
				aarsak = deltaker.statusAarsak?.let { DeltakerStatus.Aarsak.valueOf(it) },
				gyldigFra = deltaker.statusGyldigFra!!,
				opprettetDato = deltaker.statusCreatedAt!!
			),
			skjult = deltaker.skjultAvAnsattId?.let {
				DeltakerSkjultDto(
					skjultAvAnsattId = deltaker.skjultAvAnsattId,
					dato = deltaker.skjultCreatedAt!!
				)
			},
			deltarPaKurs = deltaker.deltarPaKurs
		).right()
	}

	private fun getDeltaker(deltakerId: UUID): DeltakerDbo? {
		val sql = """
		select deltaker.id                                  as deltakerId,
			   deltaker.gjennomforing_id                    as deltakerlisteId,
			   bruker.person_ident,
			   bruker.fornavn,
			   bruker.mellomnavn,
			   bruker.er_skjermet                           as er_skjermet,
			   bruker.etternavn,
			   bruker.telefonnummer,
			   bruker.epost,
			   deltaker.dager_per_uke,
			   deltaker.prosent_stilling,
			   deltaker.start_dato,
			   deltaker.slutt_dato,
			   deltaker.registrert_dato,
			   deltaker.innsok_begrunnelse,
			   nav_enhet.navn                               as nav_enhet_navn,
			   nav_ansatt.id                                as nav_ansatt_id,
			   nav_ansatt.navn                              as nav_ansatt_navn,
			   nav_ansatt.epost                             as nav_ansatt_epost,
			   status.status                                as status,
			   status.aarsak                                as status_aarsak,
			   status.gyldig_fra                            as status_gyldig_fra,
			   status.created_at                            as status_opprettet_dato,
			   skjult_deltaker.skjult_av_arrangor_ansatt_id as skjult_av_arrangor_id,
			   skjult_deltaker.created_at                   as skjult_pa_dato,
			   gjennomforing.er_kurs
		from deltaker
				 left join gjennomforing on deltaker.gjennomforing_id = gjennomforing.id
				 left join bruker on deltaker.bruker_id = bruker.id
				 left join nav_enhet on bruker.nav_enhet_id = nav_enhet.id
				 left join nav_ansatt on bruker.ansvarlig_veileder_id = nav_ansatt.id
				 left join (select deltaker_id, status, aarsak, gyldig_fra, created_at
							from deltaker_status
							where aktiv is true) status on status.deltaker_id = deltaker.id
				 left join skjult_deltaker on skjult_deltaker.deltaker_id = deltaker.id
		where deltaker.id = :deltakerId
		""".trimIndent()

		return template.query(
			sql,
			sqlParameters("deltakerId" to deltakerId),
			DeltakerDbo.rowMapper
		).firstOrNull()
	}


	private data class DeltakerDbo(
		val id: UUID,
		val deltakerlisteId: UUID,
		val personligIdent: String,
		val fornavn: String,
		val mellomnavn: String?,
		val etternavn: String,
		val skjermet: Boolean,
		val telefonnummer: String?,
		val epost: String?,
		val dagerPerUke: Int?,
		val prosentStilling: Double?,
		val startDato: LocalDate?,
		val sluttDato: LocalDate?,
		val registrertDato: LocalDate,
		val innsokBegrunnelse: String?,
		val navEnhetNavn: String?,
		val navAnsattId: UUID?,
		val navAnsattNavn: String?,
		val navAnsattEpost: String?,
		val status: String?,
		val statusAarsak: String?,
		val statusGyldigFra: LocalDateTime?,
		val statusCreatedAt: LocalDateTime?,
		val skjultAvAnsattId: UUID?,
		val skjultCreatedAt: LocalDateTime?,
		val deltarPaKurs: Boolean
	) {
		companion object {
			val rowMapper = RowMapper { rs, _ ->
				DeltakerDbo(
					id = rs.getUUID("deltakerId"),
					deltakerlisteId = rs.getUUID("deltakerlisteId"),
					personligIdent = rs.getString("person_ident"),
					fornavn = rs.getString("fornavn"),
					mellomnavn = rs.getNullableString("mellomnavn"),
					etternavn = rs.getString("etternavn"),
					skjermet = rs.getBoolean("er_skjermet"),
					telefonnummer = rs.getNullableString("telefonnummer"),
					epost = rs.getNullableString("epost"),
					dagerPerUke = rs.getInt("dager_per_uke"),
					prosentStilling = rs.getDouble("prosent_stilling"),
					startDato = rs.getNullableLocalDate("start_dato"),
					sluttDato = rs.getNullableLocalDate("slutt_dato"),
					registrertDato = rs.getLocalDate("registrert_dato"),
					innsokBegrunnelse = rs.getNullableString("innsok_begrunnelse"),
					navEnhetNavn = rs.getString("nav_enhet_navn"),
					navAnsattId = rs.getNullableUUID("nav_ansatt_id"),
					navAnsattNavn = rs.getNullableString("nav_ansatt_navn"),
					navAnsattEpost = rs.getNullableString("nav_ansatt_epost"),
					status = rs.getString("status"),
					statusAarsak = rs.getNullableString("status_aarsak"),
					statusGyldigFra = rs.getNullableLocalDateTime("status_gyldig_fra"),
					statusCreatedAt = rs.getNullableLocalDateTime("status_opprettet_dato"),
					skjultAvAnsattId = rs.getNullableUUID("skjult_av_arrangor_id"),
					skjultCreatedAt = rs.getNullableLocalDateTime("skjult_pa_dato"),
					deltarPaKurs = rs.getBoolean("er_kurs")
				)
			}
		}
	}
}
