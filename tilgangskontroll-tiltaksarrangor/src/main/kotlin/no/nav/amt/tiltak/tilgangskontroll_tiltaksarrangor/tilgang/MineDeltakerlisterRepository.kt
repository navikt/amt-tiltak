package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.common.db_utils.getZonedDateTime
import org.intellij.lang.annotations.Language
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*

@Component
open class MineDeltakerlisterRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		ArrangorAnsattGjennomforingTilgangDbo(
			id = rs.getUUID("id"),
			ansattId = rs.getUUID("ansatt_id"),
			gjennomforingId = rs.getUUID("gjennomforing_id"),
			gyldigFra = rs.getZonedDateTime("gyldig_fra"),
			gyldigTil = rs.getZonedDateTime("gyldig_til"),
			createdAt = rs.getZonedDateTime("created_at"),
		)
	}

	internal fun get(id: UUID): ArrangorAnsattGjennomforingTilgangDbo {
		val sql = """
			SELECT * FROM arrangor_ansatt_gjennomforing_tilgang WHERE id = :id
		""".trimIndent()

		val parameters = sqlParameters("id" to id)

		return template.query(sql, parameters, rowMapper).firstOrNull()
			?: throw NoSuchElementException("Fant ikke arrangor_ansatt_gjennomforing_tilgang med id $id")
	}

	internal fun leggTil(
		id: UUID,
		arrangorAnsattId: UUID,
		gjennomforingId: UUID,
		gyldigFra: ZonedDateTime,
		gyldigTil: ZonedDateTime
	) {
		val sql = """
			INSERT INTO arrangor_ansatt_gjennomforing_tilgang(id, ansatt_id, gjennomforing_id, gyldig_fra, gyldig_til)
				VALUES(:id, :ansattId, :gjennomforingId, :gyldigFra, :gyldigTil)
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to id,
			"ansattId" to arrangorAnsattId,
			"gjennomforingId" to gjennomforingId,
			"gyldigFra" to gyldigFra.toOffsetDateTime(),
			"gyldigTil" to gyldigTil.toOffsetDateTime(),
		)

		template.update(sql, parameters)
	}

	internal fun fjern(arrangorAnsattId: UUID, gjennomforingId: UUID) {
		val sql = """
			UPDATE arrangor_ansatt_gjennomforing_tilgang
			SET gyldig_til = current_timestamp
			WHERE ansatt_id = :ansattId AND gjennomforing_id = :gjennomforingId AND gyldig_til > current_timestamp
		""".trimIndent()

		val parameters = sqlParameters(
			"ansattId" to arrangorAnsattId,
			"gjennomforingId" to gjennomforingId,
		)

		template.update(sql, parameters)
	}

	internal fun hent(ansattId: UUID): List<ArrangorAnsattGjennomforingTilgangDbo> {
		val sql = """
			SELECT * FROM arrangor_ansatt_gjennomforing_tilgang
				WHERE ansatt_id = :ansattId AND gyldig_fra < current_timestamp AND gyldig_til > current_timestamp
		""".trimIndent()

		val parameters = sqlParameters("ansattId" to ansattId)

		return template.query(sql, parameters, rowMapper)
	}

	fun hentAntallPerAnsatt(): Map<UUID, Int> {

		@Language("PostgreSQL")
		val sql = """
			select ansatt_id, count(gjennomforing_id) as gjennomforinger
			from arrangor_ansatt_gjennomforing_tilgang
			WHERE gyldig_fra < current_timestamp
			  AND gyldig_til > current_timestamp
			group by ansatt_id
			order by gjennomforinger desc
		""".trimIndent()

		return template.query(sql) { rs, _ ->
			Pair(rs.getUUID("ansatt_id"), rs.getInt("gjennomforinger"))
		}.toMap()
	}

}
