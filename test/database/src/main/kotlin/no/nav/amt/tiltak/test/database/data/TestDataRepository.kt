package no.nav.amt.tiltak.test.database.data

import no.nav.amt.tiltak.test.database.DatabaseTestUtils.parameters
import no.nav.amt.tiltak.test.database.data.commands.*
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class TestDataRepository(
	private val template: NamedParameterJdbcTemplate
) {

	fun insertArrangorAnsatt(cmd: InsertArrangorAnsattCommand) {
		val sql = """
			INSERT INTO arrangor_ansatt(id, personlig_ident, fornavn, etternavn)
			VALUES(:id, :personlig_ident, :fornavn, :etternavn)
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"personlig_ident" to cmd.personlig_ident,
				"fornavn" to cmd.fornavn,
				"etternavn" to cmd.etternavn
			)
		)
	}

	fun insertArrangorAnsattGjennomforingTilgang(cmd: InsertArrangorAnsattGjennomforingTilgang) {
		val sql = """
			INSERT INTO arrangor_ansatt_gjennomforing_tilgang(id, ansatt_id, gjennomforing_id)
			VALUES(:id, :ansatt_id, :gjennomforing_id)
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"ansatt_id" to cmd.ansatt_id,
				"gjennomforing_id" to cmd.gjennomforing_id,
			)
		)
	}

	fun insertArrangorAnsattRolle(cmd: InsertArrangorAnsattRolleCommand) {
		val sql = """
			INSERT INTO arrangor_ansatt_rolle(id, arrangor_id, ansatt_id, rolle)
			VALUES(?, ?, ?, ?::arrangor_rolle)
		""".trimIndent()

		template.jdbcTemplate.update(
			sql,
			cmd.id,
			cmd.arrangor_id,
			cmd.ansatt_id,
			cmd.rolle
		)
	}

	fun insertArrangor(cmd: InsertArrangorCommand) {
		val sql = """
			INSERT INTO arrangor(id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer, navn)
			VALUES (:id, :overordnet_enhet_organisasjonsnummer, :overordnet_enhet_navn, :organisasjonsnummer, :navn)
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"overordnet_enhet_organisasjonsnummer" to cmd.overordnet_enhet_organisasjonsnummer,
				"overordnet_enhet_navn" to cmd.overordnet_enhet_navn,
				"organisasjonsnummer" to cmd.organisasjonsnummer,
				"navn" to cmd.navn,
			)
		)
	}

	fun insertBruker(cmd: InsertBrukerCommand) {
		val sql = """
			INSERT INTO bruker (id, fodselsnummer, fornavn, etternavn, telefonnummer, epost, ansvarlig_veileder_id, nav_kontor_id)
			VALUES (:id, :fodselsnummer, :fornavn, :etternavn, :telefonnummer, :epost, :ansvarlig_veileder_id, :nav_kontor_id);
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"fodselsnummer" to cmd.fodselsnummer,
				"fornavn" to cmd.fornavn,
				"etternavn" to cmd.etternavn,
				"telefonnummer" to cmd.telefonnummer,
				"epost" to cmd.epost,
				"ansvarlig_veileder_id" to cmd.ansvarlig_veileder_id,
				"nav_kontor_id" to cmd.nav_kontor_id
			)
		)
	}

	fun insertDeltaker(cmd: InsertDeltakerCommand) {
		val sql = """
			INSERT INTO deltaker (id, bruker_id, gjennomforing_id, start_dato, slutt_dato, dager_per_uke, prosent_stilling, registrert_dato)
			VALUES (:id, :bruker_id, :gjennomforing_id, :start_dato, :slutt_dato, :dager_per_uke, :prosent_stilling, :registrert_dato);
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"bruker_id" to cmd.bruker_id,
				"gjennomforing_id" to cmd.gjennomforing_id,
				"start_dato" to cmd.start_dato,
				"slutt_dato" to cmd.slutt_dato,
				"dager_per_uke" to cmd.dager_per_uke,
				"prosent_stilling" to cmd.prosent_stilling,
				"registrert_dato" to cmd.registrert_dato
			)
		)
	}

	fun insertDeltakerStatus(cmd: InsertDeltakerStatusCommand) {
		val sql = """
			INSERT INTO deltaker_status (id, deltaker_id, endret_dato, status, aktiv)
			VALUES (:id, :deltaker_id, :endret_dato, :status, :aktiv)
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"deltaker_id" to cmd.deltaker_id,
				"endret_dato" to cmd.endret_dato.toOffsetDateTime(),
				"status" to cmd.status,
				"aktiv" to cmd.aktiv
			)
		)
	}

	fun insertGjennomforing(cmd: InsertGjennomforingCommand) {
		val sql = """
			INSERT INTO gjennomforing (id, tiltak_id, arrangor_id, navn, status, start_dato, slutt_dato, registrert_dato, fremmote_dato)
			VALUES (:id, :tiltak_id, :arrangor_id, :navn, :status, :start_dato, :slutt_dato, :registrert_dato, :fremmote_dato)
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"tiltak_id" to cmd.tiltak_id,
				"arrangor_id" to cmd.arrangor_id,
				"navn" to cmd.navn,
				"status" to cmd.status,
				"start_dato" to cmd.start_dato,
				"slutt_dato" to cmd.slutt_dato,
				"registrert_dato" to cmd.registrert_dato,
				"fremmote_dato" to cmd.fremmote_dato
			)
		)
	}

	fun insertNavAnsatt(cmd: InsertNavAnsattCommand) {
		val sql = """
			INSERT INTO nav_ansatt (id, nav_ident, navn, telefonnummer, epost)
			VALUES (:id, :nav_ident, :navn, :telefonnummer, :epost)
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"nav_ident" to cmd.nav_ident,
				"navn" to cmd.navn,
				"telefonnummer" to cmd.telefonnummer,
				"epost" to cmd.epost
			)
		)
	}

	fun insertNavKontor(cmd: InsertNavKontorCommand) {
		val sql = """
			INSERT INTO nav_kontor (id, enhet_id, navn)
			VALUES (:id, :enhet_id, :navn)
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"enhet_id" to cmd.enhet_id,
				"navn" to cmd.navn
			)
		)
	}


	fun insertTiltak(cmd: InsertTiltakCommand) {
		val sql = """
			INSERT INTO tiltak(id, navn, type)
			VALUES (:id, :navn, :type)

		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"navn" to cmd.navn,
				"type" to cmd.type
			)
		)
	}

}
