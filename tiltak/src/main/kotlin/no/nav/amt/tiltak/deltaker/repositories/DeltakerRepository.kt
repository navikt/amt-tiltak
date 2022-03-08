package no.nav.amt.tiltak.deltaker.repositories

import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Component
open class DeltakerRepository(
	private val template: NamedParameterJdbcTemplate
) {
	private val rowMapper = RowMapper { rs, _ ->

		DeltakerDbo(
			id = UUID.fromString(rs.getString("id")),
			brukerId = UUID.fromString(rs.getString("bruker_id")),
			brukerFodselsnummer = rs.getString("fodselsnummer"),
			brukerFornavn = rs.getString("fornavn"),
			brukerMellomnavn = rs.getString("mellomnavn"),
			brukerEtternavn = rs.getString("etternavn"),
			startDato = rs.getDate("start_dato")?.toLocalDate(),
			sluttDato = rs.getDate("slutt_dato")?.toLocalDate(),
			gjennomforingId = UUID.fromString(rs.getString("gjennomforing_id")),
			dagerPerUke = rs.getInt("dager_per_uke"),
			prosentStilling = rs.getFloat("prosent_stilling"),
			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime(),
			registrertDato = rs.getTimestamp("registrert_dato").toLocalDateTime()
		)
	}

	fun insert(
		id: UUID,
		brukerId: UUID,
		gjennomforingId: UUID,
		startDato: LocalDate?,
		sluttDato: LocalDate?,
		dagerPerUke: Int?,
		prosentStilling: Float?,
		registrertDato: LocalDateTime
	): DeltakerDbo {
		val sql = """
			INSERT INTO deltaker(id, bruker_id, gjennomforing_id, start_dato, slutt_dato,
								 dager_per_uke, prosent_stilling, registrert_dato)
			VALUES (:id,
					:brukerId,
					:gjennomforingId,
					:startdato,
					:sluttdato,
					:dagerPerUke,
					:prosentStilling,
					:registrertDato)
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"id" to id,
				"brukerId" to brukerId,
				"gjennomforingId" to gjennomforingId,
				"startdato" to startDato,
				"sluttdato" to sluttDato,
				"dagerPerUke" to dagerPerUke,
				"prosentStilling" to prosentStilling,
				"registrertDato" to registrertDato
			)
		)

		template.update(sql, parameters)

		return get(id)
			?: throw NoSuchElementException("Deltaker $brukerId finnes ikke på tiltaksgjennomføring $gjennomforingId")

	}

	fun getDeltakerePaaTiltak(id: UUID): List<DeltakerDbo> {
		val sql = """
			SELECT deltaker.*,
				bruker.fodselsnummer,
				bruker.fornavn,
				bruker.mellomnavn,
				bruker.etternavn
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
			WHERE deltaker.gjennomforing_id = :gjennomforing_id
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf("gjennomforing_id" to id)
		)

		return template.query(sql, parameters, rowMapper)
	}

	fun update(deltaker: DeltakerDbo): DeltakerDbo {
		val sql = """
			UPDATE deltaker
			SET start_dato = :startDato,
				slutt_dato    = :sluttDato,
				dager_per_uke = :dagerPerUke,
				prosent_stilling = :prosentStilling,
				modified_at   = :modifiedAt
			WHERE id = :deltakerInternalId
	""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"startDato" to deltaker.startDato,
				"sluttDato" to deltaker.sluttDato,
				"dagerPerUke" to deltaker.dagerPerUke,
				"prosentStilling" to deltaker.prosentStilling,
				"modifiedAt" to deltaker.modifiedAt,
				"deltakerInternalId" to deltaker.id
			)
		)

		template.update(sql, parameters)

		return get(deltaker.id)
			?: throw NoSuchElementException("Deltaker ${deltaker.id} finnes ikke på tiltaksgjennomføring ${deltaker.gjennomforingId}")
	}

	fun get(id: UUID): DeltakerDbo? {
		val sql = """
			SELECT deltaker.*,
				   bruker.fodselsnummer,
				   bruker.fornavn,
				   bruker.mellomnavn,
				   bruker.etternavn
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
			WHERE deltaker.id = :deltakerId
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"deltakerId" to id
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}

	fun get(brukerId: UUID, gjennomforingId: UUID): DeltakerDbo? {
		val sql = """
			SELECT deltaker.*,
				   bruker.fodselsnummer,
				   bruker.fornavn,
				   bruker.mellomnavn,
				   bruker.etternavn
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
			WHERE bruker.id = :brukerId
				AND deltaker.gjennomforing_id = :gjennomforingId
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"brukerId" to brukerId,
				"gjennomforingId" to gjennomforingId
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}

	fun get(fodselsnummer: String, gjennomforingId: UUID): DeltakerDbo? {
		val sql = """
			SELECT deltaker.*,
				   bruker.fodselsnummer,
				   bruker.fornavn,
				   bruker.mellomnavn,
				   bruker.etternavn
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
			WHERE bruker.fodselsnummer = :bruker_fodselsnummer
				AND deltaker.gjennomforing_id = :gjennomforingId
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"bruker_fodselsnummer" to fodselsnummer,
				"gjennomforingId" to gjennomforingId
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}

	fun potensieltHarSlutta(): List<DeltakerDbo> {
		val sql = """
			SELECT deltaker.*,
				   bruker.fodselsnummer,
				   bruker.fornavn,
				   bruker.mellomnavn,
				   bruker.etternavn
			FROM deltaker_status
					 inner join deltaker on deltaker_status.deltaker_id = deltaker.id
					 inner join bruker on bruker.id = deltaker.bruker_id
			WHERE deltaker_status.aktiv = TRUE
				AND deltaker_status.status IN ('DELTAR', 'VENTER_PA_OPPSTART')
				AND deltaker.slutt_dato < CURRENT_DATE
		""".trimIndent()
		val parameters = MapSqlParameterSource()
		return template.query(sql, parameters, rowMapper)
	}

	fun potensieltDeltar(): List<DeltakerDbo> {
		val sql = """
			SELECT deltaker.*,
				   bruker.fodselsnummer,
				   bruker.fornavn,
				   bruker.mellomnavn,
				   bruker.etternavn
			FROM deltaker_status
					 inner join deltaker on deltaker_status.deltaker_id = deltaker.id
					 inner join bruker on bruker.id = deltaker.bruker_id
			WHERE deltaker_status.aktiv = TRUE
				AND deltaker_status.status = 'VENTER_PA_OPPSTART'
				AND deltaker.start_dato <= CURRENT_DATE
				AND deltaker.slutt_dato >= CURRENT_DATE
		""".trimIndent()
		val parameters = MapSqlParameterSource()
		return template.query(sql, parameters, rowMapper)
	}

	fun slettDeltaker(deltakerId: UUID) {
		val sql = "DELETE FROM deltaker WHERE id = :deltakerId"

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"deltakerId" to deltakerId
			)
		)

		template.update(sql, parameters)
	}
}
