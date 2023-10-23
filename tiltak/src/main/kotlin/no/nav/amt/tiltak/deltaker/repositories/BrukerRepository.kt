package no.nav.amt.tiltak.deltaker.repositories

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.common.json.JsonUtils.objectMapper
import no.nav.amt.tiltak.core.domain.tiltak.Adresse
import no.nav.amt.tiltak.core.domain.tiltak.Adressebeskyttelse
import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.deltaker.dbo.BrukerDbo
import no.nav.amt.tiltak.utils.getNullableUUID
import no.nav.amt.tiltak.utils.getUUID
import org.postgresql.util.PGobject
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
open class BrukerRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		BrukerDbo(
			id = rs.getUUID("id"),
			personIdent = rs.getString("person_ident"),
			fornavn = rs.getString("fornavn"),
			mellomnavn = rs.getString("mellomnavn"),
			etternavn = rs.getString("etternavn"),
			telefonnummer = rs.getString("telefonnummer"),
			epost = rs.getString("epost"),
			ansvarligVeilederId = rs.getNullableUUID("ansvarlig_veileder_id"),
			navEnhetId = rs.getNullableUUID("nav_enhet_id"),
			erSkjermet = rs.getBoolean("er_skjermet"),
			adresse = rs.getString("adresse")?.let { fromJsonString<Adresse>(it) },
			adressebeskyttelse = rs.getString("adressebeskyttelse")?.let { Adressebeskyttelse.valueOf(it) },
			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime()
		)
	}

	fun upsert(bruker: Bruker) {
		val sql = """
			INSERT INTO bruker(id, person_ident, fornavn, mellomnavn, etternavn, telefonnummer, epost, ansvarlig_veileder_id, nav_enhet_id, er_skjermet, adresse, adressebeskyttelse)
			VALUES (:id,
					:personIdent,
					:fornavn,
					:mellomnavn,
					:etternavn,
					:telefonnummer,
					:epost,
					:veileder_id,
					:nav_enhet_id,
					:er_skjermet,
					:adresse,
					:adressebeskyttelse)
			ON CONFLICT(id) DO UPDATE SET
			    person_ident = :personIdent,
			 	fornavn = :fornavn,
				mellomnavn = :mellomnavn,
				etternavn = :etternavn,
				telefonnummer = :telefonnummer,
				epost = :epost,
				ansvarlig_veileder_id = :veileder_id,
				nav_enhet_id = :nav_enhet_id,
				modified_at = CURRENT_TIMESTAMP,
				er_skjermet = :er_skjermet,
				adresse = :adresse,
				adressebeskyttelse = :adressebeskyttelse
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to bruker.id,
			"personIdent" to bruker.personIdent,
			"fornavn" to bruker.fornavn,
			"mellomnavn" to bruker.mellomnavn,
			"etternavn" to bruker.etternavn,
			"telefonnummer" to bruker.telefonnummer,
			"epost" to bruker.epost,
			"veileder_id" to bruker.ansvarligVeilederId,
			"nav_enhet_id" to bruker.navEnhetId,
			"er_skjermet" to bruker.erSkjermet,
			"adresse" to bruker.adresse?.toPGObject(),
			"adressebeskyttelse" to bruker.adressebeskyttelse?.name
		)

		template.update(sql, parameters)
	}

	fun get(personIdent: String): BrukerDbo? {
		val sql = """
			SELECT *
			FROM bruker
			WHERE person_ident = :personIdent
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"personIdent" to personIdent
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}

	fun get(id: UUID): BrukerDbo? {
		val sql = """
			SELECT *
			FROM bruker
			WHERE id = :id
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"id" to id
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}

	fun slettBruker(id: UUID) {
		val sql = "DELETE FROM bruker WHERE id = :id"

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"id" to id
			)
		)

		template.update(sql, parameters)
	}


	fun slettBruker(personIdent: String) {
		val sql = "DELETE FROM bruker WHERE person_ident = :personIdent"

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"personIdent" to personIdent
			)
		)

		template.update(sql, parameters)
	}

	private fun Adresse.toPGObject() = PGobject().also {
		it.type = "json"
		it.value = objectMapper.writeValueAsString(this)
	}
}
