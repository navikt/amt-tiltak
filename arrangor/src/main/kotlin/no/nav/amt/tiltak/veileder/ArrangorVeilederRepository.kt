package no.nav.amt.tiltak.veileder

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.common.db_utils.getZonedDateTime
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

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

	internal fun opprettVeiledere(veiledere: List<OpprettVeilederDbo>, deltakerIder: List<UUID>) {
		if (veiledere.isEmpty() || deltakerIder.isEmpty()) return

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

		val parameters = deltakerIder.map { deltakerId ->
				veiledere.map { veileder ->
					sqlParameters(
						"id" to UUID.randomUUID(),
						"ansattId" to veileder.ansattId,
						"deltakerId" to deltakerId,
						"gyldigFra" to veileder.gyldigFra.toOffsetDateTime(),
						"gyldigTil" to veileder.gyldigTil.toOffsetDateTime(),
						"erMedveileder" to veileder.erMedveileder,
					)
				}
			}.flatten().toTypedArray()

		template.batchUpdate(sql, parameters)
	}

	internal fun opprettVeiledere(veiledere: List<OpprettVeilederDbo>, deltakerId: UUID) {
		opprettVeiledere(veiledere, listOf(deltakerId))
	}

	internal fun inaktiverVeiledere(veilederIder: List<UUID>) {
		if (veilederIder.isEmpty()) return

		val sql = """
			UPDATE arrangor_veileder
			SET gyldig_til = current_timestamp, modified_at = current_timestamp
			WHERE id IN (:veilederIder) AND gyldig_til > current_timestamp
		""".trimIndent()

		val parameters = sqlParameters("veilederIder" to veilederIder)

		template.update(sql, parameters)

	}

	internal fun inaktiverVeiledereForDeltakere(ansattIder: List<UUID>, deltakerIder: List<UUID>) {
		if (ansattIder.isEmpty() || deltakerIder.isEmpty()) return

		val sql = """
			UPDATE arrangor_veileder
			SET gyldig_til = current_timestamp, modified_at = current_timestamp
			WHERE ansatt_id = :ansattId AND deltaker_id in (:deltakerIder) AND gyldig_til > current_timestamp
		""".trimIndent()

		val parameters = ansattIder.map { sqlParameters(
				"ansattId" to it,
				"deltakerIder" to deltakerIder,
			)
		}.toTypedArray()

		template.batchUpdate(sql, parameters)
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

	internal fun get(id: UUID): ArrangorVeilederDbo {
		val sql = """
			SELECT * FROM arrangor_veileder
			WHERE id = :id
		""".trimIndent()

		val parameters = sqlParameters("id" to id)

		return template.query(sql, parameters, rowMapper).first() ?:
			throw NoSuchElementException("Fant ingen ArrangorVeileder med id $id")
	}

	internal fun getAktiveForDeltaker(deltakerId: UUID): List<ArrangorVeilederDbo> {
		val sql = """
			SELECT * FROM arrangor_veileder
			WHERE deltaker_id = :deltakerId AND gyldig_fra < current_timestamp AND gyldig_til > current_timestamp
		""".trimIndent()

		val parameters = sqlParameters("deltakerId" to deltakerId)
		return template.query(sql, parameters, rowMapper)
	}

	internal fun getAktiveForDeltakere(deltakerIder: List<UUID>): List<ArrangorVeilederDbo> {
		if (deltakerIder.isEmpty()) return emptyList()

		val sql = """
			SELECT * FROM arrangor_veileder
			WHERE deltaker_id in (:deltakerIder) AND gyldig_fra < current_timestamp AND gyldig_til > current_timestamp
		""".trimIndent()

		val parameters = sqlParameters("deltakerIder" to deltakerIder)
		return template.query(sql, parameters, rowMapper)
	}
}
