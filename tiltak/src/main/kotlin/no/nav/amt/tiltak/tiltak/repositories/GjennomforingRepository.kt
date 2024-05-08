package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.GjennomforingUpsert
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.tiltak.dbo.GjennomforingDbo
import no.nav.amt.tiltak.utils.getNullableUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.UUID

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
			opprettetAar = rs.getInt("opprettet_aar"),
			lopenr = rs.getInt("lopenr"),
			erKurs = rs.getBoolean("er_kurs"),
			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime(),
		)
	}

	private val rowMapperGjennomforing = RowMapper { rs, _ ->
		Gjennomforing(
			id = UUID.fromString(rs.getString("gjennomforing_id")),
			arrangor = Arrangor(
				id = rs.getUUID("arrangor_id"),
				navn = rs.getString("arrangor_navn"),
				organisasjonsnummer = rs.getString("organisasjonsnummer"),
				overordnetEnhetNavn = rs.getString("overordnet_enhet_navn"),
				overordnetEnhetOrganisasjonsnummer = rs.getString("overordnet_enhet_organisasjonsnummer")
			),
			tiltak = Tiltak(
				id = UUID.fromString(rs.getString("tiltak_id")),
				navn = rs.getString("tiltak_navn"),
				kode = rs.getString("type"),
			),
			navn = rs.getString("gjennomforing_navn"),
			status = Gjennomforing.Status.valueOf(rs.getString("status")),
			startDato = rs.getDate("start_dato")?.toLocalDate(),
			sluttDato = rs.getDate("slutt_dato")?.toLocalDate(),
			opprettetAar = rs.getInt("opprettet_aar"),
			erKurs = rs.getBoolean("er_kurs"),
			lopenr = rs.getInt("lopenr")
		)
	}

	fun insert(gjennomforing: GjennomforingUpsert): GjennomforingDbo {

		//language=PostgreSQL
		val sql = """
		INSERT INTO gjennomforing(id, tiltak_id, arrangor_id, navn, status, start_dato,
                           slutt_dato, opprettet_aar, lopenr, er_kurs)
		VALUES (:id,
				:tiltakId,
				:arrangorId,
				:navn,
				:status,
				:startDato,
				:sluttDato,
				:opprettetAar,
				:lopenr,
				:erKurs
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
				"opprettetAar" to gjennomforing.opprettetAar,
				"lopenr" to gjennomforing.lopenr,
				"erKurs" to gjennomforing.erKurs
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
				arrangor_id		= :arrangorId,
				start_dato      = :startDato,
				slutt_dato      = :sluttDato,
				opprettet_aar 	= :opprettetAar,
				lopenr 			= :lopenr,
				er_kurs			= :erKurs,
				modified_at     = :modifiedAt
			WHERE id = :id
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"navn" to gjennomforing.navn,
				"status" to gjennomforing.status.name,
				"arrangorId" to gjennomforing.arrangorId,
				"startDato" to gjennomforing.startDato,
				"sluttDato" to gjennomforing.sluttDato,
				"opprettetAar" to gjennomforing.opprettetAar,
				"lopenr" to gjennomforing.lopenr,
				"erKurs" to gjennomforing.erKurs,
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

	fun getGjennomforingWithArrangorAndTiltak(gjennomforingIder: List<UUID>): List<Gjennomforing> {
		if (gjennomforingIder.isEmpty()) return emptyList()

		val sql = """
			SELECT gjennomforing.id as gjennomforing_id,
			gjennomforing.navn as gjennomforing_navn,
			gjennomforing.status,
			gjennomforing.start_dato,
			gjennomforing.slutt_dato,
			gjennomforing.opprettet_aar,
			gjennomforing.lopenr,
			gjennomforing.arrangor_id,
			gjennomforing.tiltak_id,
			gjennomforing.er_kurs,
			arrangor.navn as arrangor_navn,
			arrangor.organisasjonsnummer,
			arrangor.overordnet_enhet_navn,
			arrangor.overordnet_enhet_organisasjonsnummer,
			tiltak.navn as tiltak_navn,
			tiltak.type
			FROM gjennomforing
			         INNER JOIN arrangor ON arrangor.id = gjennomforing.arrangor_id
			         INNER JOIN tiltak ON tiltak.id = gjennomforing.tiltak_id
			WHERE gjennomforing.id IN (:ids);
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(mapOf("ids" to gjennomforingIder))

		return template.query(sql, parameters, rowMapperGjennomforing)
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
		deleteTiltaksansvarligGjennomforingTilgang(gjennomforingId)
		deleteTiltaksarrangorGjennomforingTilgang(gjennomforingId)

		val sql = "DELETE FROM gjennomforing WHERE id = :gjennomforingId"

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"gjennomforingId" to gjennomforingId
			)
		)

		template.update(sql, parameters)
	}

	private fun deleteTiltaksansvarligGjennomforingTilgang(gjennomforingId: UUID) {
		val sql = "DELETE FROM tiltaksansvarlig_gjennomforing_tilgang WHERE gjennomforing_id = :gjennomforingId"

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"gjennomforingId" to gjennomforingId
			)
		)

		template.update(sql, parameters)
	}

	private fun deleteTiltaksarrangorGjennomforingTilgang(gjennomforingId: UUID) {
		val sql = "DELETE FROM arrangor_ansatt_gjennomforing_tilgang WHERE gjennomforing_id = :gjennomforingId"

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"gjennomforingId" to gjennomforingId
			)
		)

		template.update(sql, parameters)
	}
}
