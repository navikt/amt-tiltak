package no.nav.amt.tiltak.deltaker.repositories

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
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
		val statusString = rs.getString("status")

		DeltakerDbo(
			id = UUID.fromString(rs.getString("id")),
			brukerId = UUID.fromString(rs.getString("bruker_id")),
			brukerFodselsnummer = rs.getString("fodselsnummer"),
			brukerFornavn = rs.getString("fornavn"),
			brukerEtternavn = rs.getString("etternavn"),
			startDato = rs.getDate("oppstart_dato")?.toLocalDate(),
			sluttDato = rs.getDate("slutt_dato")?.toLocalDate(),
			tiltakInstansId = UUID.fromString(rs.getString("tiltaksinstans_id")),
			dagerPerUke = rs.getInt("dager_per_uke"),
			prosentStilling = rs.getFloat("prosent_stilling"),
			status = Deltaker.Status.valueOf(statusString),
			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime(),
			registrertDato = rs.getTimestamp("registrert_dato").toLocalDateTime()
		)
	}

	fun insert(
		brukerId: UUID,
		tiltaksgjennomforingId: UUID,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		status: Deltaker.Status,
		dagerPerUke: Int?,
		prosentStilling: Float?,
		registrertDato: LocalDateTime
	): DeltakerDbo {
		val sql = """
			INSERT INTO deltaker(id, bruker_id, tiltaksinstans_id, oppstart_dato, slutt_dato, status,
								 dager_per_uke, prosent_stilling, registrert_dato)
			VALUES (:id,
					:brukerId,
					:tiltaksinstansId,
					:oppstartsdato,
					:sluttdato,
					:status,
					:dagerPerUke,
					:prosentStilling,
					:registrertDato)
		""".trimIndent()

		val id = UUID.randomUUID()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"id" to id,
				"brukerId" to brukerId,
				"tiltaksinstansId" to tiltaksgjennomforingId,
				"oppstartsdato" to oppstartDato,
				"sluttdato" to sluttDato,
				"status" to status.name,
				"dagerPerUke" to dagerPerUke,
				"prosentStilling" to prosentStilling,
				"registrertDato" to registrertDato
			)
		)

		template.update(sql, parameters)

		return get(id)
			?: throw NoSuchElementException("Deltaker $brukerId finnes ikke på tiltaksgjennomføring $tiltaksgjennomforingId")

	}

	fun getDeltakerePaaTiltakInstans(id: UUID): List<DeltakerDbo> {
		val sql = """
			SELECT deltaker.*,
				bruker.fodselsnummer,
				bruker.fornavn,
				bruker.etternavn
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
			WHERE deltaker.tiltaksinstans_id = :tiltaksinstans_id
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf("tiltaksinstans_id" to id)
		)

		return template.query(sql, parameters, rowMapper)
	}

	fun update(deltaker: DeltakerDbo): DeltakerDbo {
		val sql = """
			UPDATE deltaker
			SET status        = :deltakerStatus,
				oppstart_dato = :oppstartDato,
				slutt_dato    = :sluttDato,
				modified_at   = :modifiedAt
			WHERE id = :deltakerInternalId
	""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"deltakerStatus" to deltaker.status?.name,
				"oppstartDato" to deltaker.startDato,
				"sluttDato" to deltaker.sluttDato,
				"modifiedAt" to deltaker.modifiedAt,
				"deltakerInternalId" to deltaker.id
			)
		)

		template.update(sql, parameters)

		return get(deltaker.id)
			?: throw NoSuchElementException("Deltaker ${deltaker.id} finnes ikke på tiltaksgjennomføring ${deltaker.tiltakInstansId}")
	}

	fun get(id: UUID): DeltakerDbo? {
		val sql = """
			SELECT deltaker.*,
				   bruker.fodselsnummer,
				   bruker.fornavn,
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

	fun get(brukerId: UUID, tiltaksinstansId: UUID): DeltakerDbo? {
		val sql = """
			SELECT deltaker.*,
				   bruker.fodselsnummer,
				   bruker.fornavn,
				   bruker.etternavn
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
			WHERE bruker.id = :brukerId
				AND deltaker.tiltaksinstans_id = :tiltaksinstansId
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"brukerId" to brukerId,
				"tiltaksinstansId" to tiltaksinstansId
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}

	fun get(fodselsnummer: String, tiltaksinstansId: UUID): DeltakerDbo? {
		val sql = """
			SELECT deltaker.id,
				   deltaker.bruker_id,
				   bruker.fodselsnummer,
				   bruker.fornavn,
				   bruker.etternavn,
				   deltaker.oppstart_dato,
				   deltaker.slutt_dato,
				   deltaker.tiltaksinstans_id,
				   deltaker.dager_per_uke,
				   deltaker.prosent_stilling,
				   deltaker.status,
				   deltaker.created_at,
				   deltaker.modified_at,
				   deltaker.registrert_dato
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
			WHERE bruker.fodselsnummer = :bruker_fodselsnummer
				AND deltaker.tiltaksinstans_id = :tiltaksinstansId
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"bruker_fodselsnummer" to fodselsnummer,
				"tiltaksinstansId" to tiltaksinstansId
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}
}
