package no.nav.amt.tiltak.veileder

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.common.db_utils.getZonedDateTime
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
open class ArrangorVeilederRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		ArrangorVeilederDbo(
			id = rs.getUUID("id"),
			ansattId = rs.getUUID("ansatt_id"),
			deltakerId = rs.getUUID("deltaker_id"),
			erMedveileder = rs.getBoolean("er_medveileder"),
			gyldigFra = rs.getZonedDateTime("gyldig_fra"),
			gyldigTil = rs.getZonedDateTime("gyldig_til"),
			createdAt = rs.getZonedDateTime("created_at"),
			modifiedAt = rs.getZonedDateTime("modified_at"),
		)
	}

	internal fun inaktiverVeileder(ansattId: UUID, deltakerId: UUID, erMedveileder: Boolean) {
		val sql = """
			UPDATE arrangor_veileder
			SET gyldig_til = current_timestamp, modified_at = current_timestamp
			WHERE ansatt_id = :ansattId AND deltaker_id = :deltakerId AND er_medveileder = :erMedveileder AND gyldig_til > current_timestamp
		""".trimIndent()

		val parameters = sqlParameters(
			"ansattId" to ansattId,
			"deltakerId" to deltakerId,
			"erMedveileder" to erMedveileder
		)

		template.update(sql, parameters)
	}

	internal fun lagreVeileder(deltakerId: UUID, opprettVeilederDbo: OpprettVeilederDbo) {
		val sql = """
			INSERT INTO arrangor_veileder(
				id,
				ansatt_id,
				deltaker_id,
				gyldig_fra,
				gyldig_til,
				er_medveileder
			)
			VALUES(:id, :ansattId, :deltakerId, :gyldigFra, :gyldigTil, :erMedveileder)
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to UUID.randomUUID(),
			"ansattId" to opprettVeilederDbo.ansattId,
			"deltakerId" to deltakerId,
			"gyldigFra" to opprettVeilederDbo.gyldigFra.toOffsetDateTime(),
			"gyldigTil" to opprettVeilederDbo.gyldigTil.toOffsetDateTime(),
			"erMedveileder" to opprettVeilederDbo.erMedveileder
		)

		template.update(sql, parameters)
	}

	internal fun inaktiverAlleVeiledereForDeltaker(deltakerId: UUID) {
		val sql = """
			UPDATE arrangor_veileder
			SET gyldig_til = current_timestamp, modified_at = current_timestamp
			WHERE deltaker_id = :deltakerId AND gyldig_til > current_timestamp
		""".trimIndent()

		val parameters = sqlParameters("deltakerId" to deltakerId)

		template.update(sql, parameters)
	}

	fun inaktiverVeilederPaGjennomforinger(ansattId: UUID, gjennomforingIder: List<UUID>) {
		val sql = """
			update arrangor_veileder
			set gyldig_til = current_timestamp
			where gyldig_til > current_timestamp and id in (
				select av.id from arrangor_veileder av
					join deltaker d on av.deltaker_id = d.id
				where av.ansatt_id = :ansattId AND d.gjennomforing_id in (:gjennomforingIder)
		)
		""".trimIndent()

		val parameters = sqlParameters(
			"ansattId" to ansattId,
			"gjennomforingIder" to gjennomforingIder,
		)

		template.update(sql, parameters)
	}

	internal fun get(id: UUID): ArrangorVeilederDbo {
		val sql = """
			SELECT * FROM arrangor_veileder
			WHERE id = :id
		""".trimIndent()

		val parameters = sqlParameters("id" to id)

		return template.query(sql, parameters, rowMapper).first() ?:
			throw NoSuchElementException("Fant ingen ArrangorVeileder med id $id")
	}

	internal fun getDeltakereForVeileder(ansattId: UUID): List<ArrangorVeilederDbo> {
		val sql = """
			SELECT * FROM arrangor_veileder
			WHERE ansatt_id = :ansattId AND gyldig_fra < current_timestamp AND gyldig_til > current_timestamp
		""".trimIndent()

		val parameters = sqlParameters("ansattId" to ansattId)

		return template.query(sql, parameters, rowMapper)
	}

	internal fun getAktiveForDeltaker(deltakerId: UUID): List<ArrangorVeilederDbo> {
		val sql = """
			SELECT * FROM arrangor_veileder
			WHERE deltaker_id = :deltakerId AND gyldig_fra < current_timestamp AND gyldig_til > current_timestamp
		""".trimIndent()

		val parameters = sqlParameters("deltakerId" to deltakerId)
		return template.query(sql, parameters, rowMapper)
	}
}
