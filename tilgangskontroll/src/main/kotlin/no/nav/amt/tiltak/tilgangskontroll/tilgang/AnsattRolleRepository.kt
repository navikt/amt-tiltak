package no.nav.amt.tiltak.tilgangskontroll.tilgang

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.common.db_utils.getZonedDateTime
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*

@Component
class AnsattRolleRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		AnsattRolleDbo(
			id = rs.getUUID("id"),
			ansattId = rs.getUUID("ansatt_id"),
			arrangorId = rs.getUUID("arrangor_id"),
			rolle = ArrangorAnsattRolle.valueOf(rs.getString("rolle")),
			createdAt = rs.getZonedDateTime("created_at"),
			gyldigFra = rs.getZonedDateTime("gyldig_fra"),
			gyldigTil = rs.getZonedDateTime("gyldig_til"),
		)
	}

	internal fun opprettRolle(id: UUID, ansattId: UUID, arrangorId: UUID, rolle: ArrangorAnsattRolle, gyldigFra: ZonedDateTime, gyldigTil: ZonedDateTime) {
		val sql = """
			INSERT INTO arrangor_ansatt_rolle(id, ansatt_id, arrangor_id, rolle, gyldig_fra, gyldig_til)
				VALUES(:id, :ansattId, :arrangorId, CAST(:rolle AS arrangor_rolle), :gyldigFra, :gyldigTil)
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to id,
			"ansattId" to ansattId,
			"arrangorId" to arrangorId,
			"rolle" to rolle.name,
			"gyldigFra" to gyldigFra.toOffsetDateTime(),
			"gyldigTil" to gyldigTil.toOffsetDateTime(),
		)

		template.update(sql, parameters)
	}

	internal fun hentAktiveRoller(ansattId: UUID): List<AnsattRolleDbo> {
		val sql = """
			SELECT * FROM arrangor_ansatt_rolle WHERE ansatt_id = :ansattId AND gyldig_fra < :currentTime AND gyldig_til > :currentTime
		""".trimIndent()

		val parameters = sqlParameters(
			"ansattId" to ansattId,
			"currentTime" to ZonedDateTime.now().toOffsetDateTime()
		)

		return template.query(sql, parameters, rowMapper)
	}

	internal fun deaktiverRolleHosArrangor(ansattId: UUID, arrangorId: UUID, arrangorAnsattRolle: ArrangorAnsattRolle) {
		val sql = """
			UPDATE arrangor_ansatt_rolle SET gyldig_til = :currentTime
			WHERE ansatt_id = :ansattId AND arrangor_id = :arrangorId AND rolle = CAST(:ansattRolle AS arrangor_rolle) AND gyldig_til > :currentTime
		""".trimIndent()

		val parameters = sqlParameters(
			"ansattId" to ansattId,
			"arrangorId" to arrangorId,
			"ansattRolle" to arrangorAnsattRolle.name,
			"currentTime" to ZonedDateTime.now().toOffsetDateTime()
		)

		template.update(sql, parameters)
	}


}
