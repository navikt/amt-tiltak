package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getLocalDateTime
import no.nav.amt.tiltak.common.db_utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
open class ArrangorAnsattRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		AnsattDbo(
			id = rs.getUUID("id"),
			personligIdent = rs.getString("personlig_ident"),
			fornavn = rs.getString("fornavn"),
			mellomnavn = rs.getString("mellomnavn"),
			etternavn = rs.getString("etternavn"),
			tilgangerSistSynkronisert = rs.getLocalDateTime("tilganger_sist_synkronisert"),
			sistVelykkedeInnlogging = rs.getLocalDateTime("sist_velykkede_innlogging"),
			createdAt = rs.getLocalDateTime("created_at"),
			modifiedAt = rs.getLocalDateTime("modified_at")
		)
	}

	fun upsertAnsatt(id: UUID, personligIdent: String, fornavn: String, mellomnavn: String?, etternavn: String) {
		val sql = """
			INSERT INTO arrangor_ansatt(id, personlig_ident, fornavn, mellomnavn, etternavn)
				VALUES(:id, :personligIdent, :fornavn, :mellomnavn, :etternavn)
			ON CONFLICT (id) DO UPDATE SET fornavn = :fornavn,
										   mellomnavn = :mellomnavn,
										   etternavn  = :etternavn
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to id,
			"personligIdent" to personligIdent,
			"fornavn" to fornavn,
			"mellomnavn" to mellomnavn,
			"etternavn" to etternavn
		)

		template.update(sql, parameters)
	}

	fun get(ansattId: UUID): AnsattDbo? {
		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"ansattId" to ansattId
			)
		)

		return template.query(
			"SELECT * FROM arrangor_ansatt WHERE id = :ansattId",
			parameters,
			rowMapper
		).firstOrNull()
	}

	fun getByPersonligIdent(personligIdent: String): AnsattDbo? {
		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"personligIdent" to personligIdent
			)
		)

		return template.query(
			"SELECT * FROM arrangor_ansatt WHERE personlig_ident = :personligIdent",
			parameters,
			rowMapper
		).firstOrNull()
	}

	fun setSistOppdatertForAnsatt(ansattId: UUID, tilgangerSistSynkronisert: LocalDateTime) {
		val sql = """
			UPDATE arrangor_ansatt SET tilganger_sist_synkronisert = :tilgangerSistSynkronisert where id = :id
		""".trimIndent()

		template.update(
			sql, sqlParameters(
				"id" to ansattId,
				"tilgangerSistSynkronisert" to tilgangerSistSynkronisert
			)
		)
	}

	fun getEldsteSistRolleSynkroniserteAnsatte(antall: Int): List<AnsattDbo> {
		val sql = """
			SELECT *
			FROM arrangor_ansatt
			ORDER BY tilganger_sist_synkronisert ASC
			LIMIT :antall
		""".trimIndent()

		return template.query(
			sql,
			sqlParameters("antall" to antall),
			rowMapper
		)
	}
}
