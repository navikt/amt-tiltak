package no.nav.amt.tiltak.tilgangskontroll.tilgang

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.common.db_utils.getZonedDateTime
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*

@Component
open class ArrangorAnsattGjennomforingTilgangRepository(
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

	internal fun opprettTilgang(id: UUID, arrangorAnsattId: UUID, gjennomforingId: UUID, gyldigFra: ZonedDateTime, gyldigTil: ZonedDateTime) {
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

	internal fun oppdaterGyldigTil(id: UUID, gyldigTil: ZonedDateTime) {
		val sql = """
			update arrangor_ansatt_gjennomforing_tilgang set gyldig_til = :gyldigTil where id = :id
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to id,
			"gyldigTil" to gyldigTil.toOffsetDateTime(),
		)

		template.update(sql, parameters)
	}

	internal fun hentAktiveGjennomforingTilgangerForAnsatt(ansattId: UUID): List<ArrangorAnsattGjennomforingTilgangDbo> {
		val sql = """
			SELECT * FROM arrangor_ansatt_gjennomforing_tilgang
				WHERE ansatt_id = :ansattId AND gyldig_fra < current_timestamp AND gyldig_til > current_timestamp
		""".trimIndent()

		val parameters = sqlParameters("ansattId" to ansattId)

		return template.query(sql, parameters, rowMapper)
	}

}
