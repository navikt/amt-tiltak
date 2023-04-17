package no.nav.amt.tiltak.data_publisher.publish

import no.nav.amt.tiltak.common.db_utils.*
import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.data_publisher.model.*
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.util.*

class DeltakerPublishQuery(
	private val template: NamedParameterJdbcTemplate
) {

	fun execute(id: UUID): DeltakerPublishDto? {
		val deltaker = getDeltaker(id) ?: return null

		val navVeileder = if (deltaker.navAnsattId != null) {
			DeltakerNavVeilederDto(
				id = deltaker.navAnsattId,
				navn = deltaker.navAnsattNavn!!,
				epost = deltaker.navAnsattEpost!!
			)
		} else null

		return DeltakerPublishDto(
			deltaker.id,
			deltakerlisteId = deltaker.deltakerlisteId,
			personalia = DeltakerPersonaliaDto(
				personligIdent = deltaker.personligIdent,
				navn = Navn(
					fornavn = deltaker.fornavn,
					mellomnevn = deltaker.mellomnavn,
					etternavn = deltaker.etternavn
				),
				kontaktinformasjon = DeltakerKontaktinformasjonDto(
					telefonnummer = deltaker.telefonnummer,
					epost = deltaker.epost
				)
			),
			status = deltaker.status,
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
			oppstartsdato = deltaker.startDato,
			sluttdato = deltaker.sluttDato,
			innsoktPa = null,
			innsoktDato = deltaker.registrertDato,
			bestillingTekst = deltaker.innsokBegrunnelse,
			navKontor = deltaker.navEnhetNavn,
			navVeileder = navVeileder
		)
	}

	private fun getDeltaker(deltakerId: UUID): DeltakerDbo? {
		val sql = """
			select deltaker.id                                                                            as deltakerId,
				   deltaker.gjennomforing_id                                                              as deltakerlisteId,
				   bruker.person_ident,
				   bruker.fornavn,
				   bruker.mellomnavn,
				   bruker.etternavn,
				   bruker.telefonnummer,
				   bruker.epost,
				   (select status from deltaker_status where deltaker_id = deltaker.id and aktiv is true) as status,
				   deltaker.dager_per_uke,
				   deltaker.prosent_stilling,
				   deltaker.start_dato,
				   deltaker.slutt_dato,
				   deltaker.registrert_dato,
				   deltaker.innsok_begrunnelse,
				   nav_enhet.navn                                                                         as nav_enhet_navn,
				   nav_ansatt.id                                                                          as nav_ansatt_id,
				   nav_ansatt.navn                                                                        as nav_ansatt_navn,
				   nav_ansatt.epost                                                                       as nav_ansatt_epost
			from deltaker
					 left join bruker on deltaker.bruker_id = bruker.id
					 left join nav_enhet on bruker.nav_enhet_id = nav_enhet.id
					 left join nav_ansatt on bruker.ansvarlig_veileder_id = nav_ansatt.id
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
		val telefonnummer: String?,
		val epost: String?,
		val status: String?,
		val dagerPerUke: Int?,
		val prosentStilling: Double?,
		val startDato: LocalDate?,
		val sluttDato: LocalDate?,
		val registrertDato: LocalDate,
		val innsokBegrunnelse: String?,
		val navEnhetNavn: String?,
		val navAnsattId: UUID?,
		val navAnsattNavn: String?,
		val navAnsattEpost: String?
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
					telefonnummer = rs.getNullableString("telefonnummer"),
					epost = rs.getNullableString("epost"),
					status = rs.getString("status"),
					dagerPerUke = rs.getInt("dager_per_uke"),
					prosentStilling = rs.getDouble("prosent_stilling"),
					startDato = rs.getNullableLocalDate("start_dato"),
					sluttDato = rs.getNullableLocalDate("slutt_dato"),
					registrertDato = rs.getLocalDate("registrert_dato"),
					innsokBegrunnelse = rs.getNullableString("innsok_begrunnelse"),
					navEnhetNavn = rs.getString("nav_enhet_navn"),
					navAnsattId = rs.getNullableUUID("nav_ansatt_id"),
					navAnsattNavn = rs.getNullableString("nav_ansatt_navn"),
					navAnsattEpost = rs.getNullableString("nav_ansatt_epost")
				)
			}
		}
	}
}
