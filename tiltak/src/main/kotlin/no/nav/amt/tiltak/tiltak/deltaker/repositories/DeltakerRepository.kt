package no.nav.amt.tiltak.tiltak.deltaker.repositories

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.tiltak.deltaker.dbo.DeltakerDbo
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
class DeltakerRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		val statusString = rs.getString("deltaker_status")

		DeltakerDbo(
			internalId = rs.getInt("deltaker_internal_id"),
			externalId = UUID.fromString(rs.getString("deltaker_external_id")),
			brukerInternalId = rs.getInt("bruker_internal_id"),
			brukerFodselsnummer = rs.getString("bruker_fodselsnummer"),
			brukerFornavn = rs.getString("bruker_fornavn"),
			brukerEtternavn = rs.getString("bruker_etternavn"),
			deltakerOppstartsdato = rs.getDate("deltaker_oppstartsdato")?.toLocalDate(),
			deltakerSluttdato = rs.getDate("deltaker_sluttdato")?.toLocalDate(),
			tiltaksinstansInternalId = rs.getInt("tiltaksinstans_internal_id"),
			status = if (statusString != null) Deltaker.Status.valueOf(statusString) else null,
			createdAt = rs.getTimestamp("deltaker_created_at").toLocalDateTime(),
			modifiedAt = rs.getTimestamp("deltaker_modified_at").toLocalDateTime()
		)
	}

	fun insert(
		brukerId: Int,
		tiltaksgjennomforing: UUID,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		status: Deltaker.Status
	): DeltakerDbo {
		val sql = """
			INSERT INTO deltaker(external_id, bruker_id, tiltaksinstans_id, oppstart_dato, slutt_dato, status)
			VALUES (:externalId,
					:brukerId,
					(SELECT id from tiltaksinstans where external_id = :tiltaksinstansExternalId),
					:oppstartsdato,
					:sluttdato,
					:status)
		""".trimIndent()

		val externalId = UUID.randomUUID()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"externalId" to externalId,
				"brukerId" to brukerId,
				"tiltaksinstansExternalId" to tiltaksgjennomforing,
				"oppstartsdato" to oppstartDato,
				"sluttdato" to sluttDato,
				"status" to status.name
			)
		)

		template.update(sql, parameters)

		return get(externalId)
			?: throw NoSuchElementException("Deltaker $brukerId finnes ikke på tiltaksgjennomføring $tiltaksgjennomforing")

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
				"deltakerInternalId" to deltaker.internalId
			)
		)

		template.update(sql, parameters)

		return get(deltaker.externalId)
			?: throw NoSuchElementException("Deltaker ${deltaker.externalId} finnes ikke på tiltaksgjennomføring ${deltaker.tiltaksinstansInternalId}")
	}


	fun get(externalId: UUID): DeltakerDbo? {
		val sql = """
			SELECT deltaker.id                as deltaker_internal_id,
				   deltaker.external_id       as deltaker_external_id,
				   deltaker.bruker_id         as bruker_internal_id,
				   bruker.fodselsnummer       as bruker_fodselsnummer,
				   bruker.fornavn             as bruker_fornavn,
				   bruker.etternavn           as bruker_etternavn,
				   deltaker.oppstart_dato     as deltaker_oppstartsdato,
				   deltaker.slutt_dato        as deltaker_sluttdato,
				   deltaker.tiltaksinstans_id as tiltaksinstans_internal_id,
				   tiltaksinstans.external_id as tiltaksinstans_external_id,
				   deltaker.status            as deltaker_status,
				   deltaker.created_at        as deltaker_created_at,
				   deltaker.modified_at       as deltaker_modified_at
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
					 inner join tiltaksinstans on tiltaksinstans.id = deltaker.tiltaksinstans_id
			WHERE deltaker.external_id = :deltakerExternalId
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"deltakerExternalId" to externalId
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}

	fun get(brukerId: Int, tiltaksinstans: UUID): DeltakerDbo? {
		val sql = """
			SELECT deltaker.id                as deltaker_internal_id,
				   deltaker.external_id       as deltaker_external_id,
				   deltaker.bruker_id         as bruker_internal_id,
				   bruker.fodselsnummer       as bruker_fodselsnummer,
				   bruker.fornavn             as bruker_fornavn,
				   bruker.etternavn           as bruker_etternavn,
				   deltaker.oppstart_dato     as deltaker_oppstartsdato,
				   deltaker.slutt_dato        as deltaker_sluttdato,
				   deltaker.tiltaksinstans_id as tiltaksinstans_internal_id,
				   tiltaksinstans.external_id as tiltaksinstans_external_id,
				   deltaker.status            as deltaker_status,
				   deltaker.created_at        as deltaker_created_at,
				   deltaker.modified_at       as deltaker_modified_at
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
					 inner join tiltaksinstans on tiltaksinstans.id = deltaker.tiltaksinstans_id
			WHERE bruker.id = :brukerId
				AND tiltaksinstans.external_id = :tiltaksinstansExternalId
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"brukerId" to brukerId,
				"tiltaksinstansExternalId" to tiltaksinstans
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}

	fun get(fodselsnummer: String, tiltaksinstans: UUID): DeltakerDbo? {
		val sql = """
			SELECT deltaker.id                as deltaker_internal_id,
				   deltaker.external_id       as deltaker_external_id,
				   deltaker.bruker_id         as bruker_internal_id,
				   bruker.fodselsnummer       as bruker_fodselsnummer,
				   bruker.fornavn             as bruker_fornavn,
				   bruker.etternavn           as bruker_etternavn,
				   deltaker.oppstart_dato     as deltaker_oppstartsdato,
				   deltaker.slutt_dato        as deltaker_sluttdato,
				   deltaker.tiltaksinstans_id as tiltaksinstans_internal_id,
				   tiltaksinstans.external_id as tiltaksinstans_external_id,
				   deltaker.status            as deltaker_status,
				   deltaker.created_at        as deltaker_created_at,
				   deltaker.modified_at       as deltaker_modified_at
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
					 inner join tiltaksinstans on tiltaksinstans.id = deltaker.tiltaksinstans_id
			WHERE bruker.fodselsnummer = :bruker_fodselsnummer
				AND tiltaksinstans.external_id = :tiltaksinstansExternalId
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"bruker_fodselsnummer" to fodselsnummer,
				"tiltaksinstansExternalId" to tiltaksinstans
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}
}
