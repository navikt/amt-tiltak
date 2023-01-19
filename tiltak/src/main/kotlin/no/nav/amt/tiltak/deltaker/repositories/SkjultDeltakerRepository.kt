package no.nav.amt.tiltak.deltaker.repositories

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class SkjultDeltakerRepository(
	private val template: NamedParameterJdbcTemplate
) {

	fun skjulDeltaker(id: UUID, deltakerId: UUID, skjultAvArrangorAnsattId: UUID) {
		val sql = """
			INSERT INTO skjult_deltaker(id, deltaker_id, skjult_av_arrangor_ansatt_id)
			VALUES (:id, :deltakerId, :skjultAvArrangorAnsattId)
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to id,
			"deltakerId" to deltakerId,
			"skjultAvArrangorAnsattId" to skjultAvArrangorAnsattId
		)

		template.update(sql, parameters)
	}

	fun opphevSkjulDeltaker(deltakerId: UUID) {
		val sql = """
			UPDATE skjult_deltaker SET skjult_til = current_timestamp WHERE deltaker_id = :deltakerId AND skjult_til > current_timestamp
		""".trimIndent()

		val parameters = sqlParameters(
			"deltakerId" to deltakerId,
		)

		template.update(sql, parameters)
	}

	fun erSkjultForTiltaksarrangor(deltakerIder: List<UUID>): Map<UUID, Boolean> {
		if (deltakerIder.isEmpty())
			return emptyMap()

		val sql = """
			SELECT deltaker_id FROM skjult_deltaker WHERE deltaker_id in(:deltakerIder) and current_timestamp < skjult_til
		""".trimIndent()

		val parameters = sqlParameters(
			"deltakerIder" to deltakerIder,
		)

		val skjulteDeltakere = template.query(sql, parameters) { rs, _ -> rs.getUUID("deltaker_id") }

		val erSkjultMap = deltakerIder.associateWith { false }.toMutableMap()

		skjulteDeltakere.forEach { erSkjultMap[it] = true }

		return erSkjultMap
	}

}
