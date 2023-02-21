package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getLocalDateTime
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

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

	fun opprettAnsatt(id: UUID, personligIdent: String, fornavn: String, mellomnavn: String?, etternavn: String) {
		val sql = """
			INSERT INTO arrangor_ansatt(id, personlig_ident, fornavn, mellomnavn, etternavn)
				VALUES(:id, :personligIdent, :fornavn, :mellomnavn, :etternavn)
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

	fun getAnsatte(ansattIder: List<UUID>): List<AnsattDbo> {
		if (ansattIder.isEmpty()) return emptyList()

		val sql = "SELECT * FROM arrangor_ansatt WHERE id in (:ansattIder)"
		val parameters = sqlParameters("ansattIder" to ansattIder)

		return template.query(sql, parameters, rowMapper)
	}

	fun getAnsatteForGjennomforing(gjennomforingId: UUID, rolle: ArrangorAnsattRolle): List<AnsattDbo> {
		val sql = """
			SELECT distinct a.*
			FROM arrangor_ansatt a
					 INNER JOIN arrangor_ansatt_rolle aar on a.id = aar.ansatt_id
					 INNER JOIN arrangor_ansatt_gjennomforing_tilgang aagt on aar.ansatt_id = aagt.ansatt_id
			WHERE aagt.gjennomforing_id = :gjennomforingId
			  AND aar.rolle = CAST(:rolle AS arrangor_rolle)
			  AND aagt.gyldig_fra < CURRENT_TIMESTAMP
			  AND aagt.gyldig_til > CURRENT_TIMESTAMP
		""".trimIndent()
		return template.query(
			sql,
			sqlParameters(
				"gjennomforingId" to gjennomforingId,
				"rolle" to rolle.name,
			),
			rowMapper
		)
	}

	fun getAnsatteMedRolleForArrangor(arrangorId: UUID, rolle: ArrangorAnsattRolle): List<AnsattDbo> {
		val sql = """
			SELECT distinct a.*
			FROM arrangor_ansatt a
				INNER JOIN arrangor_ansatt_rolle aar on a.id = aar.ansatt_id
			WHERE aar.arrangor_id = :arrangorId
			  AND aar.rolle = CAST(:rolle AS arrangor_rolle)
			  AND aar.gyldig_fra < CURRENT_TIMESTAMP
			  AND aar.gyldig_til > CURRENT_TIMESTAMP
		""".trimIndent()

		return template.query(
			sql,
			sqlParameters(
				"arrangorId" to arrangorId,
				"rolle" to rolle.name,
			),
			rowMapper
		)

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

	fun setVelykketInnlogging(ansattId: UUID) {
		val sql = """
			UPDATE arrangor_ansatt SET sist_velykkede_innlogging = CURRENT_TIMESTAMP WHERE id = :ansattId
		""".trimIndent()

		template.update(sql, sqlParameters("ansattId" to ansattId))
	}

	fun getAnsattMetrics(): AnsattMetrics {
		val sql = """
			select
    		(select count(*) as antall_ansatte from arrangor_ansatt as antall_ansatte),
    		(select count(*) as logged_in_last_hour from arrangor_ansatt where sist_velykkede_innlogging BETWEEN NOW() - INTERVAL '1 HOUR' AND NOW()),
    		(select count(*) as logged_in_last_day from arrangor_ansatt where sist_velykkede_innlogging BETWEEN NOW() - INTERVAL '24 HOURS' AND NOW()),
    		(select count(*) as logged_in_last_week from arrangor_ansatt where sist_velykkede_innlogging BETWEEN NOW() - INTERVAL '7 DAYS' AND NOW())
		""".trimIndent()

		return template.query(sql) { rs, _ ->
			AnsattMetrics(
				rs.getInt("antall_ansatte"),
				rs.getInt("logged_in_last_hour"),
				rs.getInt("logged_in_last_day"),
				rs.getInt("logged_in_last_week"),
			)
		}.first()
	}

	data class AnsattMetrics(
		val antallAnsatte: Int,
		val antallAnsatteInnloggetSisteTime: Int,
		val antallAnsatteInnloggetSisteDag: Int,
		val antallAnsatteInnloggetSisteUke: Int
	)

}
