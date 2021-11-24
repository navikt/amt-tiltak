package no.nav.amt.tiltak.tiltak.deltaker.repositories

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.tiltak.deltaker.dbo.DeltakerDbo
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

@Component
open class DeltakerRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		val statusString = rs.getString("deltaker_status")

		DeltakerDbo(
			id = UUID.fromString(rs.getString("deltaker_id")),
			brukerId = UUID.fromString(rs.getString("bruker_id")),
			brukerFodselsnummer = rs.getString("bruker_fodselsnummer"),
			brukerFornavn = rs.getString("bruker_fornavn"),
			brukerEtternavn = rs.getString("bruker_etternavn"),
			deltakerOppstartsdato = rs.getDate("deltaker_oppstartsdato")?.toLocalDate(),
			deltakerSluttdato = rs.getDate("deltaker_sluttdato")?.toLocalDate(),
			tiltakInstansId = UUID.fromString(rs.getString("tiltaksinstans_id")),
			arenaStatus = rs.getString("deltaker_arena_status"),
			dagerPerUke = rs.getInt("deltaker_dager_per_uke"),
			prosentStilling = rs.getFloat("deltaker_prosent_stilling"),
			status = if (statusString != null) Deltaker.Status.valueOf(statusString) else null,
			createdAt = rs.getTimestamp("deltaker_created_at").toLocalDateTime(),
			modifiedAt = rs.getTimestamp("deltaker_modified_at").toLocalDateTime()
		)
	}

	fun insert(
		brukerId: UUID,
		tiltaksgjennomforingId: UUID,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		status: Deltaker.Status,
		arenaStatus: String?,
		dagerPerUke: Int?,
		prosentStilling: Float?
	): DeltakerDbo {
		val sql = """
			INSERT INTO deltaker(id, bruker_id, tiltaksinstans_id, oppstart_dato, slutt_dato, status, arena_status,
								 dager_per_uke, prosent_stilling)
			VALUES (:id,
					:brukerId,
					:tiltaksinstansId,
					:oppstartsdato,
					:sluttdato,
					:status,
					:arenaStatus,
					:dagerPerUke,
					:prosentStilling)
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
				"arenaStatus" to arenaStatus,
				"dagerPerUke" to dagerPerUke,
				"prosentStilling" to prosentStilling
			)
		)

		template.update(sql, parameters)

		return get(id)
			?: throw NoSuchElementException("Deltaker $brukerId finnes ikke på tiltaksgjennomføring $tiltaksgjennomforingId")

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
				"oppstartDato" to deltaker.deltakerOppstartsdato,
				"sluttDato" to deltaker.deltakerSluttdato,
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
			SELECT deltaker.id                as deltaker_id,
				   deltaker.bruker_id         as bruker_id,
				   bruker.fodselsnummer       as bruker_fodselsnummer,
				   bruker.fornavn             as bruker_fornavn,
				   bruker.etternavn           as bruker_etternavn,
				   deltaker.oppstart_dato     as deltaker_oppstartsdato,
				   deltaker.slutt_dato        as deltaker_sluttdato,
				   deltaker.tiltaksinstans_id as tiltaksinstans_id,
				   deltaker.arena_status      as deltaker_arena_status,
				   deltaker.dager_per_uke     as deltaker_dager_per_uke,
				   deltaker.prosent_stilling  as deltaker_prosent_stilling,
				   deltaker.status            as deltaker_status,
				   deltaker.created_at        as deltaker_created_at,
				   deltaker.modified_at       as deltaker_modified_at
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
					 inner join tiltaksinstans on tiltaksinstans.id = deltaker.tiltaksinstans_id
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
			SELECT deltaker.id                as deltaker_id,
				   deltaker.bruker_id         as bruker_id,
				   bruker.fodselsnummer       as bruker_fodselsnummer,
				   bruker.fornavn             as bruker_fornavn,
				   bruker.etternavn           as bruker_etternavn,
				   deltaker.oppstart_dato     as deltaker_oppstartsdato,
				   deltaker.slutt_dato        as deltaker_sluttdato,
				   deltaker.tiltaksinstans_id as tiltaksinstans_id,
				   deltaker.arena_status      as deltaker_arena_status,
				   deltaker.dager_per_uke     as deltaker_dager_per_uke,
				   deltaker.prosent_stilling  as deltaker_prosent_stilling,
				   deltaker.status            as deltaker_status,
				   deltaker.created_at        as deltaker_created_at,
				   deltaker.modified_at       as deltaker_modified_at
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
			SELECT deltaker.id                as deltaker_id,
				   deltaker.bruker_id         as bruker_id,
				   bruker.fodselsnummer       as bruker_fodselsnummer,
				   bruker.fornavn             as bruker_fornavn,
				   bruker.etternavn           as bruker_etternavn,
				   deltaker.oppstart_dato     as deltaker_oppstartsdato,
				   deltaker.slutt_dato        as deltaker_sluttdato,
				   deltaker.tiltaksinstans_id as tiltaksinstans_id,
				   deltaker.arena_status      as deltaker_arena_status,
				   deltaker.dager_per_uke     as deltaker_dager_per_uke,
				   deltaker.prosent_stilling  as deltaker_prosent_stilling,
				   deltaker.status            as deltaker_status,
				   deltaker.created_at        as deltaker_created_at,
				   deltaker.modified_at       as deltaker_modified_at
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
