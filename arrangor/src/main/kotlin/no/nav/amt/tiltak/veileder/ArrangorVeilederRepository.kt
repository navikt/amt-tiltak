package no.nav.amt.tiltak.veileder

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.common.db_utils.getZonedDateTime
import no.nav.amt.tiltak.core.domain.tiltak.ArrangorVeiledersDeltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
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

	private val rowMapperVeiledersDeltaker = RowMapper { rs, _ ->
		ArrangorVeiledersDeltaker(
			id = rs.getUUID("deltaker_id"),
			fornavn = rs.getString("fornavn"),
			mellomnavn = rs.getString("mellomnavn"),
			etternavn = rs.getString("etternavn"),
			fodselsnummer = rs.getString("person_ident"),
			startDato = rs.getDate("start_dato")?.toLocalDate(),
			sluttDato = rs.getDate("slutt_dato")?.toLocalDate(),
			status = DeltakerStatus.Type.valueOf(rs.getString("status")),
			statusDato = rs.getTimestamp("created_at").toLocalDateTime(),
			gjennomforingId = rs.getUUID("gjennomforing_id"),
			gjennomforingNavn = rs.getString("gjennomforing_navn"),
			gjennomforingType = rs.getString("tiltak_navn"),
			erMedveilederFor = rs.getBoolean("er_medveileder"),
			erSkjermet = rs.getBoolean("er_skjermet")
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

	internal fun getAktiveForDeltakere(deltakerIder: List<UUID>): List<ArrangorVeilederDbo> {
		if (deltakerIder.isEmpty()) return emptyList()

		val sql = """
			SELECT * FROM arrangor_veileder
			WHERE deltaker_id in (:deltakerIder) AND gyldig_fra < current_timestamp AND gyldig_til > current_timestamp
		""".trimIndent()

		val parameters = sqlParameters("deltakerIder" to deltakerIder)
		return template.query(sql, parameters, rowMapper)
	}

	internal fun getDeltakerlisteForVeileder(ansattId: UUID): List<ArrangorVeiledersDeltaker> {
		val sql = """
			SELECT arrangor_veileder.deltaker_id,
				   arrangor_veileder.er_medveileder,
				   deltaker.gjennomforing_id,
				   deltaker.start_dato,
				   deltaker.slutt_dato,
				   deltaker_status.status,
				   deltaker_status.created_at,
				   bruker.person_ident,
				   bruker.fornavn,
				   bruker.mellomnavn,
				   bruker.etternavn,
				   bruker.er_skjermet,
				   gjennomforing.navn AS gjennomforing_navn,
				   tiltak.navn        AS tiltak_navn
			FROM arrangor_veileder
					 INNER JOIN deltaker ON arrangor_veileder.deltaker_id = deltaker.id
					 INNER JOIN deltaker_status ON arrangor_veileder.deltaker_id = deltaker_status.deltaker_id
					 INNER JOIN bruker ON bruker.id = deltaker.bruker_id
					 INNER JOIN gjennomforing ON deltaker.gjennomforing_id = gjennomforing.id
					 INNER JOIN tiltak ON gjennomforing.tiltak_id = tiltak.id
			WHERE ansatt_id = :ansattId
			  AND arrangor_veileder.gyldig_fra < CURRENT_TIMESTAMP
			  AND arrangor_veileder.gyldig_til > CURRENT_TIMESTAMP
			  AND deltaker_status.aktiv = TRUE
			  AND deltaker_status.status != :pabegyntRegistreringStatus
			  AND deltaker_status.status != :pabegyntStatus
		""".trimIndent()

		val parameters = sqlParameters(
			"ansattId" to ansattId,
			"pabegyntRegistreringStatus" to DeltakerStatus.Type.PABEGYNT_REGISTRERING.name,
			"pabegyntStatus" to DeltakerStatus.Type.PABEGYNT.name
		)
		return template.query(sql, parameters, rowMapperVeiledersDeltaker)
	}
}
