package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getLocalDateTime
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
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
		val parameters = MapSqlParameterSource().addValues(mapOf(
			"ansattId" to ansattId
		))

		return template.query(
			"SELECT * FROM arrangor_ansatt WHERE id = :ansattId",
			parameters,
			rowMapper
		).firstOrNull()
	}

	fun getByPersonligIdent(personligIdent: String): AnsattDbo? {
		val parameters = MapSqlParameterSource().addValues(mapOf(
			"personligIdent" to personligIdent
		))

		return template.query(
			"SELECT * FROM arrangor_ansatt WHERE personlig_ident = :personligIdent",
			parameters,
			rowMapper
		).firstOrNull()
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

}
