package no.nav.amt.tiltak.data_publisher.publish

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getLocalDateTime
import no.nav.amt.tiltak.common.db_utils.getNullableLocalDateTime
import no.nav.amt.tiltak.common.db_utils.getNullableString
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.data_publisher.model.*
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class ArrangorAnsattPublishQuery(
	private val template: NamedParameterJdbcTemplate
) {

	fun get(id: UUID): ArrangorAnsattPublishDto {
		val ansatt = getAnsattDetaljer(id)
		val roller = getAnsattRoller(id)
		val veileder = getAnsattVeileder(id)
		val koordinator = getAnsattKoordinator(id)

		val arrangorIds = roller.map { it.arrangorId }.toSet()

		return ArrangorAnsattPublishDto(
			id = ansatt.id,
			personalia = PersonPublishDto(
				personident = ansatt.personligIdent,
				navn = Navn(
					fornavn = ansatt.fornavn,
					mellomnavn = ansatt.mellomnavn,
					etternavn = ansatt.etternavn
				)
			),
			arrangorer = arrangorIds.map { arrangorId ->
				TilknyttetArrangor(
					arrangorId = arrangorId,
					roller = roller.filter { it.arrangorId == arrangorId }.map { it.rolle },
					veileder = veileder.filter { it.arrangorId == arrangorId }.map {
						Veileder(
							deltakerId = it.deltakerId,
							type = if (it.erMedveileder) VeilederType.MEDVEILEDER else VeilederType.VEILEDER
						)
					},
					koordinator = koordinator.filter { it.arrangorId == arrangorId }.map { it.gjennomforingId }
				)
			}
		)
	}

	private fun getAnsattDetaljer(id: UUID): AnsattDetaljer {
		return template.query(
			"SELECT * FROM arrangor_ansatt WHERE id = :id",
			sqlParameters("id" to id),
			AnsattDetaljer.rowMapper
		).first()
	}

	private fun getAnsattRoller(ansattId: UUID): List<AnsattRoller> {
		return template.query(
			"SELECT * FROM arrangor_ansatt_rolle where ansatt_id = :ansattId",
			sqlParameters("ansattId" to ansattId),
			AnsattRoller.rowMapper
		).filter { isGyldig(it.gyldigTil) }
	}

	private fun getAnsattVeileder(ansattId: UUID): List<AnsattVeileder> {
		val sql = """
			select arrangor_veileder.ansatt_id,
				   arrangor_veileder.deltaker_id,
				   arrangor_veileder.er_medveileder,
				   gjennomforing.arrangor_id,
				   arrangor_veileder.gyldig_fra,
				   arrangor_veileder.gyldig_til
			from arrangor_veileder
			left join deltaker on arrangor_veileder.deltaker_id = deltaker.id
			left join gjennomforing on deltaker.gjennomforing_id = gjennomforing.id
			where arrangor_veileder.ansatt_id = :ansattId
		""".trimIndent()

		return template.query(
			sql,
			sqlParameters("ansattId" to ansattId),
			AnsattVeileder.rowMapper
		).filter { isGyldig(it.gyldigTil) }
	}

	private fun getAnsattKoordinator(ansattId: UUID): List<AnsattKoordinator> {
		val sql = """
			select arrangor_ansatt_gjennomforing_tilgang.ansatt_id,
				   arrangor_ansatt_gjennomforing_tilgang.gjennomforing_id,
				   gjennomforing.arrangor_id,
				   arrangor_ansatt_gjennomforing_tilgang.gyldig_fra,
				   arrangor_ansatt_gjennomforing_tilgang.gyldig_til
			from arrangor_ansatt_gjennomforing_tilgang
					 left join gjennomforing on arrangor_ansatt_gjennomforing_tilgang.gjennomforing_id = gjennomforing.id
			where arrangor_ansatt_gjennomforing_tilgang.ansatt_id = :ansattId
		""".trimIndent()

		return template.query(
			sql,
			sqlParameters("ansattId" to ansattId),
			AnsattKoordinator.rowMapper
		).filter { isGyldig(it.gyldigTil) }
	}

	fun isGyldig(gyldigTil: LocalDateTime?): Boolean {
		return gyldigTil == null || gyldigTil.isAfter(LocalDateTime.now())
	}

	private data class AnsattKoordinator(
		val ansattId: UUID,
		val gjennomforingId: UUID,
		val arrangorId: UUID,
		val gyldigFra: LocalDateTime,
		val gyldigTil: LocalDateTime?
	) {
		companion object {
			val rowMapper = RowMapper { rs, _ ->
				AnsattKoordinator(
					ansattId = rs.getUUID("ansatt_id"),
					gjennomforingId = rs.getUUID("gjennomforing_id"),
					arrangorId = rs.getUUID("arrangor_id"),
					gyldigFra = rs.getLocalDateTime("gyldig_fra"),
					gyldigTil = rs.getNullableLocalDateTime("gyldig_til")
				)
			}
		}
	}

	private data class AnsattVeileder(
		val ansattId: UUID,
		val deltakerId: UUID,
		val arrangorId: UUID,
		val erMedveileder: Boolean,
		val gyldigFra: LocalDateTime,
		val gyldigTil: LocalDateTime?
	) {
		companion object {
			val rowMapper = RowMapper { rs, _ ->
				AnsattVeileder(
					ansattId = rs.getUUID("ansatt_id"),
					deltakerId = rs.getUUID("deltaker_id"),
					arrangorId = rs.getUUID("arrangor_id"),
					erMedveileder = rs.getBoolean("er_medveileder"),
					gyldigFra = rs.getLocalDateTime("gyldig_fra"),
					gyldigTil = rs.getNullableLocalDateTime("gyldig_til")
				)
			}
		}
	}

	private data class AnsattRoller(
		val arrangorId: UUID,
		val rolle: AnsattRolle,
		val gyldigFra: LocalDateTime,
		val gyldigTil: LocalDateTime?
	) {
		companion object {
			val rowMapper = RowMapper { rs, _ ->
				AnsattRoller(
					arrangorId = rs.getUUID("arrangor_id"),
					rolle = AnsattRolle.valueOf(rs.getString("rolle")),
					gyldigFra = rs.getLocalDateTime("gyldig_fra"),
					gyldigTil = rs.getNullableLocalDateTime("gyldig_til")
				)
			}
		}
	}

	private data class AnsattDetaljer(
		val id: UUID,
		val personligIdent: String,
		val fornavn: String,
		val mellomnavn: String?,
		val etternavn: String
	) {

		companion object {
			val rowMapper = RowMapper { rs, _ ->
				AnsattDetaljer(
					id = rs.getUUID("id"),
					personligIdent = rs.getString("personlig_ident"),
					fornavn = rs.getString("fornavn"),
					mellomnavn = rs.getNullableString("mellomnavn"),
					etternavn = rs.getString("etternavn")
				)
			}
		}

	}
}
