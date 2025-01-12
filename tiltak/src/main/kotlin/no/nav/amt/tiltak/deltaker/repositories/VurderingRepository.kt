package no.nav.amt.tiltak.deltaker.repositories

import no.nav.amt.tiltak.common.db_utils.DbUtils
import no.nav.amt.tiltak.core.domain.tiltak.Vurderingstype
import no.nav.amt.tiltak.utils.getLocalDateTime
import no.nav.amt.tiltak.utils.getNullableLocalDateTime
import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID
import no.nav.amt.tiltak.core.domain.tiltak.VurderingDbo

@Component
open class VurderingRepository(
	private val template: NamedParameterJdbcTemplate
) {
	private val rowMapper = RowMapper { rs, _ ->
		VurderingDbo(
			id = rs.getUUID("id"),
			deltakerId = rs.getUUID("deltaker_id"),
			vurderingstype = Vurderingstype.valueOf(rs.getString("vurderingstype")),
			begrunnelse = rs.getString("begrunnelse"),
			opprettetAvArrangorAnsattId = rs.getUUID("opprettet_av_arrangor_ansatt_id"),
			gyldigFra = rs.getLocalDateTime("gyldig_fra"),
			gyldigTil = rs.getNullableLocalDateTime("gyldig_til")
		)
	}

	fun getVurderingerForDeltaker(deltakerId: UUID): List<VurderingDbo> {
		val sql = """
			SELECT *
			FROM vurdering
			WHERE deltaker_id = :deltakerId;
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf("deltakerId" to deltakerId)
		)

		return template.query(sql, parameters, rowMapper)
	}

	fun getAktiveByGjennomforing(gjennomforingId: UUID): List<VurderingDbo> {
		val sql = """
			SELECT *
			FROM vurdering
			JOIN deltaker on vurdering.deltaker_id = deltaker.id
			WHERE deltaker.gjennomforing_id = :gjennomforing_id AND gyldig_til is null
		""".trimIndent()

		val param = DbUtils.sqlParameters("gjennomforing_id" to gjennomforingId)

		return template.query(sql, param, rowMapper)
	}

	fun insert(vurdering: VurderingDbo) {
		val sql = """
			INSERT INTO vurdering (id, deltaker_id, opprettet_av_arrangor_ansatt_id, vurderingstype, begrunnelse, gyldig_fra, gyldig_til)
			VALUES (
				:id, :deltaker_id, :opprettet_av_arrangor_ansatt_id, :vurderingstype, :begrunnelse, :gyldig_fra, :gyldig_til
			)
		""".trimIndent()
		val params = DbUtils.sqlParameters(
			"id" to vurdering.id,
			"deltaker_id" to vurdering.deltakerId,
			"opprettet_av_arrangor_ansatt_id" to vurdering.opprettetAvArrangorAnsattId,
			"vurderingstype" to vurdering.vurderingstype.name,
			"begrunnelse" to vurdering.begrunnelse,
			"gyldig_fra" to vurdering.gyldigFra,
			"gyldig_til" to vurdering.gyldigTil
		)

		template.update(sql, params)
	}

	fun deaktiver(id: UUID) {
		val sql = """
			UPDATE vurdering SET gyldig_til = :gyldig_til WHERE id = :id
		""".trimIndent()
		val params = DbUtils.sqlParameters(
			"gyldig_til" to LocalDateTime.now(),
			"id" to id
		)
		template.update(sql, params)
	}

	fun slett(deltakerId: UUID) {
		val sql = """
			DELETE from vurdering WHERE deltaker_id = :deltakerId
		""".trimIndent()
		val params = DbUtils.sqlParameters(
			"deltakerId" to deltakerId,
		)
		template.update(sql, params)
	}
}
