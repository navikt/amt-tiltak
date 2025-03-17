package no.nav.amt.tiltak.data_publisher.publish

import no.nav.amt.lib.models.deltaker.DeltakerHistorikk
import no.nav.amt.lib.models.deltaker.DeltakerVedImport
import no.nav.amt.lib.models.deltaker.ImportertFraArena
import no.nav.amt.lib.models.deltaker.VurderingFraArrangorData
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
import no.nav.amt.tiltak.core.domain.tiltak.*
import no.nav.amt.tiltak.core.port.UnleashService
import no.nav.amt.tiltak.data_publisher.model.DeltakerKontaktinformasjonDto
import no.nav.amt.tiltak.data_publisher.model.DeltakerNavVeilederDto
import no.nav.amt.tiltak.data_publisher.model.DeltakerPersonaliaDto
import no.nav.amt.tiltak.data_publisher.model.DeltakerPublishDto
import no.nav.amt.tiltak.data_publisher.model.DeltakerStatusDto
import no.nav.amt.tiltak.data_publisher.model.Navn
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class DeltakerPublishQuery(
	private val template: NamedParameterJdbcTemplate,
	private val unleashService: UnleashService
) {
	fun get(id: UUID, erKometDeltaker: Boolean?): Result<DeltakerPublishDto> {
		val deltaker = getDeltaker(id) ?: return Result.PublishTombstone()

		if (deltaker.status == null) return Result.DontPublish()

		if (deltaker.kilde == Kilde.KOMET || erKometDeltaker == true || unleashService.erKometMasterForTiltakstype(deltaker.tiltakstype)) {
			return Result.DontPublish()
		}

		val vurderinger = getVurderinger(id)
		val status = DeltakerStatusDto(
			id = deltaker.statusId,
			type = DeltakerStatus.Type.valueOf(deltaker.status),
			aarsak = deltaker.statusAarsak?.let { DeltakerStatus.Aarsak.valueOf(it) },
			aarsaksbeskrivelse = deltaker.statusAarsakBeskrivelse,
			gyldigFra = deltaker.statusGyldigFra!!,
			opprettetDato = deltaker.statusCreatedAt!!,
		)
		val historikk = byggHistorikk(vurderinger, deltaker, status)

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
				adresse = deltaker.adresse,
				adressebeskyttelse = deltaker.adressebeskyttelse
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
			status = status,
			deltarPaKurs = deltaker.deltarPaKurs,
			vurderingerFraArrangor = vurderinger,
			historikk = historikk,
			kilde = deltaker.kilde,
			forsteVedtakFattet = deltaker.forsteVedtakFattet,
			sistEndretAv = deltaker.sistEndretAv,
			sistEndretAvEnhet = deltaker.sistEndretAvEnhet,
			sistEndret = deltaker.sistEndret,
			erManueltDeltMedArrangor = deltaker.erManueltDeltMedArrangor
		).let { Result.OK(it) }
	}

	private fun byggHistorikk(vurderinger: List<Vurdering>, deltaker: DeltakerDbo, status: DeltakerStatusDto): List<DeltakerHistorikk> {
		val importertFraArena = DeltakerHistorikk.ImportertFraArena(
			importertFraArena = ImportertFraArena(
				deltakerId = deltaker.id,
				//Samme deltaker kan bli publisert flere ganger dersom identisk endringen kommer over 1 min senere
				importertDato = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES),
				deltakerVedImport = deltaker.toDeltakerVedImport(deltaker.registrertDato, status),
			)
		)
		val vurderingerHistorikk = vurderinger.map { vurdering ->
			DeltakerHistorikk.VurderingFraArrangor(
				data = VurderingFraArrangorData(
					id = vurdering.id,
					deltakerId = vurdering.deltakerId,
					vurderingstype = no.nav.amt.lib.models.arrangor.melding.Vurderingstype.valueOf(vurdering.vurderingstype.name),
					begrunnelse = vurdering.begrunnelse,
					opprettetAvArrangorAnsattId = vurdering.opprettetAvArrangorAnsattId,
					opprettet = vurdering.opprettet,
				)
			)
		}

		return vurderingerHistorikk
			.plus(importertFraArena)
			.sortedByDescending { it.sistEndret }

	}

	private fun DeltakerDbo.toDeltakerVedImport(innsoktDato: LocalDate, status: DeltakerStatusDto) = DeltakerVedImport(
		deltakerId = id,
		innsoktDato = innsoktDato,
		startdato = startDato,
		sluttdato = sluttDato,
		dagerPerUke = dagerPerUke,
		deltakelsesprosent = prosentStilling?.toFloat(),
		status = no.nav.amt.lib.models.deltaker.DeltakerStatus(
			id = status.id ?: throw IllegalStateException("Deltaker status ikke satt"),
			type = no.nav.amt.lib.models.deltaker.DeltakerStatus.Type.valueOf(status.type.name),
			aarsak = status.aarsak?.let {no.nav.amt.lib.models.deltaker.DeltakerStatus.Aarsak(
				type = no.nav.amt.lib.models.deltaker.DeltakerStatus.Aarsak.Type.valueOf(status.aarsak.name),
				beskrivelse = status.aarsaksbeskrivelse
			)},
			gyldigFra = status.gyldigFra,
			gyldigTil = null,
			opprettet = status.opprettetDato

		)
	)

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
				   bruker.adressebeskyttelse,
				   deltaker.dager_per_uke,
				   deltaker.prosent_stilling,
				   deltaker.start_dato,
				   deltaker.slutt_dato,
				   deltaker.registrert_dato,
				   deltaker.innsok_begrunnelse,
				   deltaker.kilde,
				   deltaker.forste_vedtak_fattet,
				   deltaker.sist_endret_av,
				   deltaker.sist_endret_av_enhet,
				   deltaker.er_manuelt_delt_med_arrangor,
				   deltaker.modified_at							as deltaker_sist_endret,
				   nav_enhet.navn                               as nav_enhet_navn,
				   nav_ansatt.id                                as nav_ansatt_id,
				   nav_ansatt.navn                              as nav_ansatt_navn,
				   nav_ansatt.epost                             as nav_ansatt_epost,
				   nav_ansatt.telefonnummer                     as nav_ansatt_telefonnummer,
				   status.id 									as status_id,
				   status.status                                as status,
				   status.aarsak                                as status_aarsak,
				   status.aarsaksbeskrivelse                    as status_aarsaksbeskrivelse,
				   status.gyldig_fra                            as status_gyldig_fra,
				   status.created_at                            as status_opprettet_dato,
				   gjennomforing.er_kurs,
				   tiltak.type									as tiltakstype
			from deltaker
					 left join gjennomforing on deltaker.gjennomforing_id = gjennomforing.id
					 left join tiltak on gjennomforing.tiltak_id = tiltak.id
					 left join bruker on deltaker.bruker_id = bruker.id
					 left join nav_enhet on bruker.nav_enhet_id = nav_enhet.id
					 left join nav_ansatt on bruker.ansvarlig_veileder_id = nav_ansatt.id
					 left join (select id, deltaker_id, status, aarsak, aarsaksbeskrivelse, gyldig_fra, created_at
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
			opprettet = rs.getLocalDateTime("gyldig_fra")
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
		val adressebeskyttelse: Adressebeskyttelse?,
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
		val statusId: UUID?,
		val status: String?,
		val statusAarsak: String?,
		val statusAarsakBeskrivelse: String?,
		val statusGyldigFra: LocalDateTime?,
		val statusCreatedAt: LocalDateTime?,
		val statusErManueltDeltMedArrangor: Boolean = false,
		val deltarPaKurs: Boolean,
		val kilde: Kilde,
		val forsteVedtakFattet: LocalDate?,
		val sistEndretAv: UUID?,
		val sistEndretAvEnhet: UUID?,
		val sistEndret: LocalDateTime,
		val tiltakstype: String,
		val erManueltDeltMedArrangor: Boolean,
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
					adressebeskyttelse = rs.getString("adressebeskyttelse")?.let { Adressebeskyttelse.valueOf(it) },
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
					statusId = rs.getNullableUUID("status_id"),
					status = rs.getString("status"),
					statusAarsak = rs.getNullableString("status_aarsak"),
					statusAarsakBeskrivelse = rs.getNullableString("status_aarsaksbeskrivelse"),
					statusGyldigFra = rs.getNullableLocalDateTime("status_gyldig_fra"),
					statusCreatedAt = rs.getNullableLocalDateTime("status_opprettet_dato"),
					deltarPaKurs = rs.getBoolean("er_kurs"),
					kilde = Kilde.valueOf(rs.getString("kilde")),
					forsteVedtakFattet = rs.getNullableLocalDate("forste_vedtak_fattet"),
					sistEndretAv = rs.getNullableUUID("sist_endret_av"),
					sistEndretAvEnhet = rs.getNullableUUID("sist_endret_av_enhet"),
					sistEndret = rs.getLocalDateTime("deltaker_sist_endret"),
					tiltakstype = rs.getString("tiltakstype"),
					erManueltDeltMedArrangor = rs.getBoolean("er_manuelt_delt_med_arrangor"),
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
