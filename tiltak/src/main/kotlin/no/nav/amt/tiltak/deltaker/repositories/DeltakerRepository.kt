package no.nav.amt.tiltak.deltaker.repositories

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getNullableFloat
import no.nav.amt.tiltak.common.db_utils.getNullableString
import no.nav.amt.tiltak.common.db_utils.getNullableUUID
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.core.domain.tiltak.AVSLUTTENDE_STATUSER
import no.nav.amt.tiltak.core.domain.tiltak.Adressebeskyttelse
import no.nav.amt.tiltak.core.domain.tiltak.DeltakelsesInnhold
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerHistorikk
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Kilde
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerUpsertDbo
import no.nav.amt.tiltak.nav_enhet.NavEnhetDbo
import org.postgresql.util.PGobject
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
open class DeltakerRepository(
	private val template: NamedParameterJdbcTemplate
) {
	private val rowMapper = RowMapper { rs, _ ->

		DeltakerDbo(
			id = UUID.fromString(rs.getString("id")),
			personIdent = rs.getString("person_ident"),
			fornavn = rs.getString("fornavn"),
			mellomnavn = rs.getString("mellomnavn"),
			etternavn = rs.getString("etternavn"),
			telefonnummer = rs.getString("telefonnummer"),
			epost = rs.getString("epost"),
			erSkjermet = rs.getBoolean("er_skjermet"),
			navEnhet = rs.getNullableUUID("nav_enhet_id")?.let {
				NavEnhetDbo(
					id = it,
					enhetId = rs.getString("enhet_id"),
					navn = rs.getString("navkontor")
				)
			},
			navVeilederId = rs.getNullableUUID("ansvarlig_veileder_id"),
			startDato = rs.getDate("start_dato")?.toLocalDate(),
			sluttDato = rs.getDate("slutt_dato")?.toLocalDate(),
			gjennomforingId = UUID.fromString(rs.getString("gjennomforing_id")),
			dagerPerUke = rs.getNullableFloat("dager_per_uke"),
			prosentStilling = rs.getNullableFloat("prosent_stilling"),
			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime(),
			registrertDato = rs.getTimestamp("registrert_dato").toLocalDateTime(),
			innsokBegrunnelse = rs.getNullableString("innsok_begrunnelse"),
			adressebeskyttelse = rs.getString("adressebeskyttelse")?.let { Adressebeskyttelse.valueOf(it) },
			innhold = rs.getString("innhold")?.let { fromJsonString(it) },
			kilde = Kilde.valueOf(rs.getString("kilde")),
			forsteVedtakFattet = rs.getDate("forste_vedtak_fattet")?.toLocalDate(),
			historikk = rs.getString("historikk")?.let { h -> fromJsonString<List<String>>(h).map { fromJsonString(it) } },
			sistEndretAv = rs.getNullableUUID("sist_endret_av"),
			sistEndretAvEnhet = rs.getNullableUUID("sist_endret_av_enhet")
		)
	}

	fun upsert(deltaker: DeltakerUpsertDbo) {
		val sql = """
			INSERT INTO deltaker(id, bruker_id, gjennomforing_id, start_dato, slutt_dato,
								 dager_per_uke, prosent_stilling, registrert_dato, innsok_begrunnelse, innhold, kilde,
								 forste_vedtak_fattet, historikk, sist_endret_av, sist_endret_av_enhet)
			VALUES (:id,
					:brukerId,
					:gjennomforingId,
					:startdato,
					:sluttdato,
					:dagerPerUke,
					:prosentStilling,
					:registrertDato,
					:innsokBegrunnelse,
					:innhold,
					:kilde,
					:forste_vedtak_fattet,
					:historikk,
					:sist_endret_av,
					:sist_endret_av_enhet)
			ON CONFLICT (id) DO
			UPDATE SET
				start_dato = :startdato,
				slutt_dato    = :sluttdato,
				dager_per_uke = :dagerPerUke,
				prosent_stilling = :prosentStilling,
				innsok_begrunnelse = :innsokBegrunnelse,
				innhold	= :innhold,
				kilde = :kilde,
				forste_vedtak_fattet = :forste_vedtak_fattet,
				historikk = :historikk,
				sist_endret_av = :sist_endret_av,
				sist_endret_av_enhet = :sist_endret_av_enhet,
				modified_at = CURRENT_TIMESTAMP
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"id" to deltaker.id,
				"brukerId" to deltaker.brukerId,
				"gjennomforingId" to deltaker.gjennomforingId,
				"startdato" to deltaker.startDato,
				"sluttdato" to deltaker.sluttDato,
				"dagerPerUke" to deltaker.dagerPerUke,
				"prosentStilling" to deltaker.prosentStilling,
				"registrertDato" to deltaker.registrertDato,
				"innsokBegrunnelse" to deltaker.innsokBegrunnelse,
				"innhold" to deltaker.innhold?.toPGObject(),
				"kilde" to deltaker.kilde.name,
				"forste_vedtak_fattet" to deltaker.forsteVedtakFattet,
				"historikk" to deltaker.historikk?.toPGObject(),
				"sist_endret_av" to deltaker.sistEndretAv,
				"sist_endret_av_enhet" to deltaker.sistEndretAvEnhet
			)
		)

		template.update(sql, parameters)
	}

	fun getDeltakerePaaTiltak(id: UUID): List<DeltakerDbo> {
		val sql = """
			SELECT deltaker.*, bruker.*, ne.navn as navkontor, ne.enhet_id as enhet_id
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
					 LEFT JOIN nav_enhet ne ON ne.id = bruker.nav_enhet_id
			WHERE deltaker.gjennomforing_id = :gjennomforing_id
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf("gjennomforing_id" to id)
		)

		return template.query(sql, parameters, rowMapper)
	}

	fun get(id: UUID): DeltakerDbo? {
		val sql = """
			SELECT deltaker.*, bruker.*, ne.navn as navkontor, ne.enhet_id as enhet_id
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
					 LEFT JOIN nav_enhet ne ON ne.id = bruker.nav_enhet_id
			WHERE deltaker.id = :deltakerId
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"deltakerId" to id
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}

	fun get(brukerId: UUID, gjennomforingId: UUID): DeltakerDbo? {
		val sql = """
			SELECT deltaker.*, bruker.*, ne.navn as navkontor, ne.enhet_id as enhet_id
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
					 LEFT JOIN nav_enhet ne ON ne.id = bruker.nav_enhet_id
			WHERE bruker.id = :brukerId
				AND deltaker.gjennomforing_id = :gjennomforingId
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"brukerId" to brukerId,
				"gjennomforingId" to gjennomforingId
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}

	fun getDeltakere(deltakerIder: List<UUID>): List<DeltakerDbo> {
		if (deltakerIder.isEmpty()) return emptyList()

		val sql = """
			SELECT deltaker.*, bruker.*, ne.navn as navkontor, ne.enhet_id as enhet_id
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
					 LEFT JOIN nav_enhet ne ON ne.id = bruker.nav_enhet_id
			WHERE deltaker.id in (:deltakerIder)
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"deltakerIder" to deltakerIder,
			)
		)

		return template.query(sql, parameters, rowMapper)
	}


	fun getDeltakereMedPersonIdent(personIdent: String): List<DeltakerDbo> {
		val sql = """
			SELECT deltaker.*, bruker.*, ne.navn as navkontor, ne.enhet_id as enhet_id
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
					 LEFT JOIN nav_enhet ne ON ne.id = bruker.nav_enhet_id
			WHERE bruker.person_ident = :bruker_person_ident
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"bruker_person_ident" to personIdent,
			)
		)

		return template.query(sql, parameters, rowMapper)
	}

	fun getDeltakereMedBrukerId(brukerId: UUID): List<DeltakerDbo> {
		val sql = """
			SELECT deltaker.*, bruker.*, ne.navn as navkontor, ne.enhet_id as enhet_id
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
					 LEFT JOIN nav_enhet ne ON ne.id = bruker.nav_enhet_id
			WHERE bruker.id = :brukerId
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"brukerId" to brukerId,
			)
		)

		return template.query(sql, parameters, rowMapper)
	}

	fun get(personIdent: String, gjennomforingId: UUID): DeltakerDbo? {
		val sql = """
			SELECT deltaker.*, bruker.*, ne.navn as navkontor, ne.enhet_id as enhet_id
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
					 LEFT JOIN nav_enhet ne ON ne.id = bruker.nav_enhet_id
			WHERE bruker.person_ident = :bruker_person_ident
				AND deltaker.gjennomforing_id = :gjennomforingId
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"bruker_person_ident" to personIdent,
				"gjennomforingId" to gjennomforingId
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}

	fun sluttDatoPassert(): List<DeltakerDbo> {
		val sql = """
			SELECT deltaker.*, bruker.*, ne.navn as navkontor, ne.enhet_id as enhet_id
			FROM deltaker_status
					 inner join deltaker on deltaker_status.deltaker_id = deltaker.id
					 inner join bruker on bruker.id = deltaker.bruker_id
					 LEFT JOIN nav_enhet ne ON ne.id = bruker.nav_enhet_id
			WHERE deltaker_status.aktiv = TRUE
				AND deltaker.kilde != 'KOMET'
				AND deltaker_status.status IN (:gjennomforende_statuser)
				AND deltaker.slutt_dato < CURRENT_DATE
		""".trimIndent()
		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"gjennomforende_statuser" to listOf(
					DeltakerStatus.Type.DELTAR.name,
					DeltakerStatus.Type.VENTER_PA_OPPSTART.name
				)
			)
		)
		return template.query(sql, parameters, rowMapper)
	}

	fun erPaaAvsluttetGjennomforing(): List<DeltakerDbo> {
		val sql = """
			SELECT deltaker.*, bruker.*, ne.navn as navkontor, ne.enhet_id as enhet_id
			FROM deltaker_status
         		inner join deltaker on deltaker_status.deltaker_id = deltaker.id
         		inner join bruker on bruker.id = deltaker.bruker_id
         		inner join gjennomforing on deltaker.gjennomforing_id = gjennomforing.id
		 		LEFT JOIN nav_enhet ne ON ne.id = bruker.nav_enhet_id
			WHERE deltaker_status.aktiv = TRUE
				AND deltaker.kilde != 'KOMET'
				AND deltaker_status.status not in (:avsluttende_statuser)
				AND gjennomforing.status = :gjennomforing_status
		""".trimIndent()
		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"avsluttende_statuser" to AVSLUTTENDE_STATUSER.map { it.name },
				"gjennomforing_status" to Gjennomforing.Status.AVSLUTTET.name
			)
		)
		return template.query(sql, parameters, rowMapper)
	}

	fun skalHaStatusDeltar(): List<DeltakerDbo> {
		val sql = """
			SELECT deltaker.*, bruker.*, ne.navn as navkontor, ne.enhet_id as enhet_id
			FROM deltaker_status
					 inner join deltaker on deltaker_status.deltaker_id = deltaker.id
					 inner join bruker on bruker.id = deltaker.bruker_id
					 LEFT JOIN nav_enhet ne ON ne.id = bruker.nav_enhet_id
			WHERE deltaker_status.aktiv = TRUE
				AND deltaker.kilde != 'KOMET'
				AND deltaker_status.status = '${DeltakerStatus.Type.VENTER_PA_OPPSTART.name}'
				AND deltaker.start_dato <= CURRENT_DATE
				AND (deltaker.slutt_dato IS NULL OR deltaker.slutt_dato >= CURRENT_DATE)
		""".trimIndent()
		val parameters = MapSqlParameterSource()
		return template.query(sql, parameters, rowMapper)
	}

	fun hentDeltakere(offset: Int, limit: Int): List<DeltakerDbo> {
		val sql = """
			SELECT deltaker.*, bruker.*, ne.navn as navkontor, ne.enhet_id as enhet_id
			FROM deltaker inner join bruker on bruker.id = deltaker.bruker_id
				LEFT JOIN nav_enhet ne ON ne.id = bruker.nav_enhet_id
			ORDER BY deltaker.id OFFSET :offset LIMIT :limit
		""".trimIndent()

		val parameters = sqlParameters(
			"offset" to offset,
			"limit" to limit
		)

		return template.query(sql, parameters, rowMapper)
	}

	fun slettVeilederrelasjonOgDeltaker(deltakerId: UUID) {
		template.update(
			"DELETE FROM arrangor_veileder WHERE deltaker_id = :deltakerId",
			MapSqlParameterSource().addValues(
				mapOf(
					"deltakerId" to deltakerId
				)
			)
		)
		val sql = "DELETE FROM deltaker WHERE id = :deltakerId"

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"deltakerId" to deltakerId
			)
		)

		template.update(sql, parameters)
	}

	private fun DeltakelsesInnhold.toPGObject() = PGobject().also {
		it.type = "json"
		it.value = JsonUtils.objectMapper.writeValueAsString(this)
	}

	private fun List<DeltakerHistorikk>.toPGObject() = toPGObject(this.map { JsonUtils.toJsonString(it) })

	private fun toPGObject(any: Any) = PGobject().also {
		it.type = "json"
		it.value = JsonUtils.objectMapper.writeValueAsString(any)
	}
}
