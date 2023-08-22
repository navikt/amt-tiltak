package no.nav.amt.tiltak.data_publisher.publish

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getLocalDate
import no.nav.amt.tiltak.common.db_utils.getLocalDateTime
import no.nav.amt.tiltak.common.db_utils.getNullableDouble
import no.nav.amt.tiltak.common.db_utils.getNullableFloat
import no.nav.amt.tiltak.common.db_utils.getNullableLocalDate
import no.nav.amt.tiltak.common.db_utils.getNullableLocalDateTime
import no.nav.amt.tiltak.common.db_utils.getNullableString
import no.nav.amt.tiltak.common.db_utils.getNullableUUID
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.domain.tiltak.Adresse
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Vurdering
import no.nav.amt.tiltak.core.domain.tiltak.Vurderingstype
import no.nav.amt.tiltak.data_publisher.model.DeltakerKontaktinformasjonDto
import no.nav.amt.tiltak.data_publisher.model.DeltakerNavVeilederDto
import no.nav.amt.tiltak.data_publisher.model.DeltakerPersonaliaDto
import no.nav.amt.tiltak.data_publisher.model.DeltakerPublishDto
import no.nav.amt.tiltak.data_publisher.model.DeltakerSkjultDto
import no.nav.amt.tiltak.data_publisher.model.DeltakerStatusDto
import no.nav.amt.tiltak.data_publisher.model.Navn
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class DeltakerPublishQuery(
	private val template: NamedParameterJdbcTemplate
) {

	fun get(id: UUID): Result<DeltakerPublishDto> {
		val deltaker = getDeltaker(id) ?: return Result.PublishTombstone()

		if (deltaker.status == null) return Result.DontPublish()

		val skjult = getSkjuling(id)
			.maxByOrNull { it.createdAt }
			?.let {
				if (it.skjultTil.isAfter(LocalDateTime.now())) {
					DeltakerSkjultDto(
						skjultAvAnsattId = it.skjultAvAnsattId,
						dato = it.createdAt
					)

				} else null
			}
		val vurderinger = getVurderinger(id)

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
				skjermet = deltaker.skjermet,
				adresse = deltaker.adresse
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
					epost = deltaker.navAnsattEpost,
					telefonnummer = deltaker.navAnsattTelefonnummer
				)
			},
			status = DeltakerStatusDto(
				type = DeltakerStatus.Type.valueOf(deltaker.status),
				aarsak = deltaker.statusAarsak?.let { DeltakerStatus.Aarsak.valueOf(it) },
				gyldigFra = deltaker.statusGyldigFra!!,
				opprettetDato = deltaker.statusCreatedAt!!
			),
			skjult = skjult,
			deltarPaKurs = deltaker.deltarPaKurs,
			vurderingerFraArrangor = vurderinger
		).let { Result.OK(it) }
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
				   bruker.adresse,
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
				   nav_ansatt.telefonnummer                     as nav_ansatt_telefonnummer,
				   status.status                                as status,
				   status.aarsak                                as status_aarsak,
				   status.gyldig_fra                            as status_gyldig_fra,
				   status.created_at                            as status_opprettet_dato,
				   gjennomforing.er_kurs
			from deltaker
					 left join gjennomforing on deltaker.gjennomforing_id = gjennomforing.id
					 left join bruker on deltaker.bruker_id = bruker.id
					 left join nav_enhet on bruker.nav_enhet_id = nav_enhet.id
					 left join nav_ansatt on bruker.ansvarlig_veileder_id = nav_ansatt.id
					 left join (select deltaker_id, status, aarsak, gyldig_fra, created_at
								from deltaker_status
								where aktiv is true) status on status.deltaker_id = deltaker.id
			where deltaker.id = :deltakerId
		""".trimIndent()

		return template.query(
			sql,
			sqlParameters("deltakerId" to deltakerId),
			DeltakerDbo.rowMapper
		).firstOrNull()
	}

	private fun getSkjuling(deltakerId: UUID): List<SkjultDeltakerDbo> = template.query(
		"SELECT * FROM skjult_deltaker WHERE deltaker_id = :deltaker_id",
		sqlParameters("deltaker_id" to deltakerId),
		SkjultDeltakerDbo.rowMapper
	)

	private data class SkjultDeltakerDbo(
		val id: UUID,
		val deltakerId: UUID,
		val skjultAvAnsattId: UUID,
		val skjultTil: LocalDateTime,
		val createdAt: LocalDateTime
	) {
		companion object {
			val rowMapper = RowMapper { rs, _ ->
				SkjultDeltakerDbo(
					rs.getUUID("id"),
					rs.getUUID("deltaker_id"),
					rs.getUUID("skjult_av_arrangor_ansatt_id"),
					rs.getLocalDateTime("skjult_til"),
					rs.getLocalDateTime("created_at")
				)
			}
		}
	}

	private fun getVurderinger(deltakerId: UUID): List<Vurdering> {
		val sql = """
			SELECT *
			FROM vurdering
			WHERE deltaker_id = :deltakerId;
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf("deltakerId" to deltakerId)
		)

		return template.query(sql, parameters, vurderingRowMapper)
	}

	private val vurderingRowMapper = RowMapper { rs, _ ->
		Vurdering(
			id = rs.getUUID("id"),
			deltakerId = rs.getUUID("deltaker_id"),
			vurderingstype = Vurderingstype.valueOf(rs.getString("vurderingstype")),
			begrunnelse = rs.getString("begrunnelse"),
			opprettetAvArrangorAnsattId = rs.getUUID("opprettet_av_arrangor_ansatt_id"),
			gyldigFra = rs.getLocalDateTime("gyldig_fra"),
			gyldigTil = rs.getNullableLocalDateTime("gyldig_til")
		)
	}

	private data class DeltakerDbo(
		val id: UUID,
		val deltakerlisteId: UUID,
		val personligIdent: String,
		val fornavn: String,
		val mellomnavn: String?,
		val etternavn: String,
		val skjermet: Boolean,
		val adresse: Adresse?,
		val telefonnummer: String?,
		val epost: String?,
		val dagerPerUke: Float?,
		val prosentStilling: Double?,
		val startDato: LocalDate?,
		val sluttDato: LocalDate?,
		val registrertDato: LocalDate,
		val innsokBegrunnelse: String?,
		val navEnhetNavn: String?,
		val navAnsattId: UUID?,
		val navAnsattNavn: String?,
		val navAnsattEpost: String?,
		val navAnsattTelefonnummer: String?,
		val status: String?,
		val statusAarsak: String?,
		val statusGyldigFra: LocalDateTime?,
		val statusCreatedAt: LocalDateTime?,
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
					adresse = rs.getString("adresse")?.let { JsonUtils.fromJsonString<Adresse>(it) },
					telefonnummer = rs.getNullableString("telefonnummer"),
					epost = rs.getNullableString("epost"),
					dagerPerUke = rs.getNullableFloat("dager_per_uke"),
					prosentStilling = rs.getNullableDouble("prosent_stilling"),
					startDato = rs.getNullableLocalDate("start_dato"),
					sluttDato = rs.getNullableLocalDate("slutt_dato"),
					registrertDato = rs.getLocalDate("registrert_dato"),
					innsokBegrunnelse = rs.getNullableString("innsok_begrunnelse"),
					navEnhetNavn = rs.getString("nav_enhet_navn"),
					navAnsattId = rs.getNullableUUID("nav_ansatt_id"),
					navAnsattNavn = rs.getNullableString("nav_ansatt_navn"),
					navAnsattEpost = rs.getNullableString("nav_ansatt_epost"),
					navAnsattTelefonnummer = rs.getNullableString("nav_ansatt_telefonnummer"),
					status = rs.getString("status"),
					statusAarsak = rs.getNullableString("status_aarsak"),
					statusGyldigFra = rs.getNullableLocalDateTime("status_gyldig_fra"),
					statusCreatedAt = rs.getNullableLocalDateTime("status_opprettet_dato"),
					deltarPaKurs = rs.getBoolean("er_kurs")
				)
			}
		}
	}

	sealed class Result<T> {
		data class OK<T>(val result: T) : Result<T>()
		class PublishTombstone<T> : Result<T>()
		class DontPublish<T> : Result<T>()
	}
}
