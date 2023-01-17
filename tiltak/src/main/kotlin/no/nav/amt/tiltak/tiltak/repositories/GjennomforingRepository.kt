package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.GjennomforingUpsert
import no.nav.amt.tiltak.tiltak.dbo.GjennomforingDbo
import no.nav.amt.tiltak.utils.getNullableUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class GjennomforingRepository(private val template: NamedParameterJdbcTemplate) {

	private val rowMapper = RowMapper { rs, _ ->
		GjennomforingDbo(
			id = UUID.fromString(rs.getString("id")),
			arrangorId = UUID.fromString(rs.getString("arrangor_id")),
			tiltakId = UUID.fromString(rs.getString("tiltak_id")),
			navn = rs.getString("navn"),
			status = Gjennomforing.Status.valueOf(rs.getString("status")),
			startDato = rs.getDate("start_dato")?.toLocalDate(),
			sluttDato = rs.getDate("slutt_dato")?.toLocalDate(),
			navEnhetId = rs.getNullableUUID("nav_enhet_id"),
			opprettetAar = rs.getInt("opprettet_aar"),
			lopenr = rs.getInt("lopenr"),
			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime(),
		)
	}

	fun insert(gjennomforing: GjennomforingUpsert): GjennomforingDbo {

		//language=PostgreSQL
		val sql = """
		INSERT INTO gjennomforing(id, tiltak_id, arrangor_id, navn, status, start_dato,
                           slutt_dato, nav_enhet_id, opprettet_aar, lopenr)
		VALUES (:id,
				:tiltakId,
				:arrangorId,
				:navn,
				:status,
				:startDato,
				:sluttDato,
				:navEnhetId,
				:opprettetAar,
				:lopenr
				)
	""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"id" to gjennomforing.id,
				"tiltakId" to gjennomforing.tiltakId,
				"arrangorId" to gjennomforing.arrangorId,
				"navn" to gjennomforing.navn,
				"status" to gjennomforing.status.name,
				"startDato" to gjennomforing.startDato,
				"sluttDato" to gjennomforing.sluttDato,
				"navEnhetId" to gjennomforing.navEnhetId,
				"opprettetAar" to gjennomforing.opprettetAar,
				"lopenr" to gjennomforing.lopenr,
			)
		)

		template.update(sql, parameters)

		return get(gjennomforing.id)
			?: throw NoSuchElementException("Gjennomføring med id ${gjennomforing.id} finnes ikke")
	}

	fun update(gjennomforing: GjennomforingDbo): GjennomforingDbo {
		//language=PostgreSQL
		val sql = """
			UPDATE gjennomforing
			SET navn            = :navn,
				status          = :status,
				start_dato      = :startDato,
				slutt_dato      = :sluttDato,
				nav_enhet_id 	= :navEnhetId,
				opprettet_aar 	= :opprettetAar,
				lopenr 			= :lopenr,
				modified_at     = :modifiedAt
			WHERE id = :id
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"navn" to gjennomforing.navn,
				"status" to gjennomforing.status.name,
				"startDato" to gjennomforing.startDato,
				"sluttDato" to gjennomforing.sluttDato,
				"opprettetAar" to gjennomforing.opprettetAar,
				"navEnhetId" to gjennomforing.navEnhetId,
				"lopenr" to gjennomforing.lopenr,
				"modifiedAt" to gjennomforing.modifiedAt,
				"id" to gjennomforing.id
			)
		)

		template.update(sql, parameters)

		return get(gjennomforing.id)
			?: throw NoSuchElementException("Tiltak med id ${gjennomforing.id} finnes ikke")
	}


	fun get(id: UUID): GjennomforingDbo? {
		val sql = "SELECT * FROM gjennomforing WHERE id = :id"

		val parameters = MapSqlParameterSource().addValues(mapOf("id" to id))

		return template.query(sql, parameters, rowMapper).firstOrNull()
	}

	fun get(gjennomforingIder: List<UUID>): List<GjennomforingDbo> {
		if (gjennomforingIder.isEmpty()) return emptyList()

		val sql = "SELECT * FROM gjennomforing WHERE id in(:ids)"

		val parameters = MapSqlParameterSource().addValues(mapOf("ids" to gjennomforingIder))

		return template.query(sql, parameters, rowMapper)
	}


	fun getByArrangorId(arrangorId: UUID): List<GjennomforingDbo> {
		val sql = "SELECT * FROM gjennomforing WHERE arrangor_id = :arrangorId"

		val parameters = MapSqlParameterSource().addValues(mapOf("arrangorId" to arrangorId))

		return template.query(sql, parameters, rowMapper)
	}

	fun getByLopenr(lopenr: Int): List<GjennomforingDbo> {
		val sql = "SELECT * FROM gjennomforing WHERE lopenr = :lopenr"

		val parameters = MapSqlParameterSource().addValues(mapOf("lopenr" to lopenr))

		return template.query(sql, parameters, rowMapper)
	}


	fun delete(gjennomforingId: UUID) {
		val sql = "DELETE FROM gjennomforing WHERE id = :gjennomforingId"

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"gjennomforingId" to gjennomforingId
			)
		)

		template.update(sql, parameters)
	}


}
