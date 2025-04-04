package no.nav.amt.tiltak.test.database.data

import no.nav.amt.tiltak.common.db_utils.DbUtils
import no.nav.amt.tiltak.core.domain.tiltak.VurderingDbo
import no.nav.amt.tiltak.test.database.DbTestDataUtils.parameters
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorAnsattGjennomforingTilgangInput
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorAnsattInput
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorAnsattRolleInput
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorInput
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorVeilederDboInput
import no.nav.amt.tiltak.test.database.data.inputs.BrukerInput
import no.nav.amt.tiltak.test.database.data.inputs.DeltakerInput
import no.nav.amt.tiltak.test.database.data.inputs.DeltakerStatusInput
import no.nav.amt.tiltak.test.database.data.inputs.EndringsmeldingInput
import no.nav.amt.tiltak.test.database.data.inputs.GjennomforingInput
import no.nav.amt.tiltak.test.database.data.inputs.NavAnsattInput
import no.nav.amt.tiltak.test.database.data.inputs.NavEnhetInput
import no.nav.amt.tiltak.test.database.data.inputs.TiltakInput
import no.nav.amt.tiltak.test.database.data.inputs.TiltaksansvarligGjennomforingTilgangInput
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDateTime

class TestDataRepository(
	private val template: NamedParameterJdbcTemplate
) {

	fun insertArrangorAnsatt(cmd: ArrangorAnsattInput) {
		val sql = """
			INSERT INTO arrangor_ansatt(id, personlig_ident, fornavn, mellomnavn, etternavn)
			VALUES(:id, :personlig_ident, :fornavn, :mellomnavn, :etternavn)
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"personlig_ident" to cmd.personligIdent,
				"fornavn" to cmd.fornavn,
				"mellomnavn" to cmd.mellomnavn,
				"etternavn" to cmd.etternavn
			)
		)
	}

	fun insertMineDeltakerlister(cmd: ArrangorAnsattGjennomforingTilgangInput) {
		val sql = """
			INSERT INTO arrangor_ansatt_gjennomforing_tilgang(id, ansatt_id, gjennomforing_id, gyldig_fra, gyldig_til)
			VALUES(:id, :ansatt_id, :gjennomforing_id, :gyldig_fra, :gyldig_til)
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"ansatt_id" to cmd.ansattId,
				"gjennomforing_id" to cmd.gjennomforingId,
				"gyldig_fra" to cmd.gyldigFra.toOffsetDateTime(),
				"gyldig_til" to cmd.gyldigTil.toOffsetDateTime()
			)
		)
	}

	fun insertArrangorAnsattRolle(cmd: ArrangorAnsattRolleInput) {
		val sql = """
			INSERT INTO arrangor_ansatt_rolle(id, arrangor_id, ansatt_id, rolle)
			VALUES(?, ?, ?, ?::arrangor_rolle)
		""".trimIndent()

		template.jdbcTemplate.update(
			sql,
			cmd.id,
			cmd.arrangorId,
			cmd.ansattId,
			cmd.rolle
		)
	}

	fun insertArrangor(cmd: ArrangorInput) {
		val sql = """
			INSERT INTO arrangor(id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer, navn)
			VALUES (:id, :overordnet_enhet_organisasjonsnummer, :overordnet_enhet_navn, :organisasjonsnummer, :navn)
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"overordnet_enhet_organisasjonsnummer" to cmd.overordnetEnhetOrganisasjonsnummer,
				"overordnet_enhet_navn" to cmd.overordnetEnhetNavn,
				"organisasjonsnummer" to cmd.organisasjonsnummer,
				"navn" to cmd.navn,
			)
		)
	}

	fun insertBruker(cmd: BrukerInput) {
		val sql = """
			INSERT INTO bruker (id, person_ident, fornavn, etternavn, telefonnummer, epost, ansvarlig_veileder_id, nav_enhet_id, er_skjermet, adressebeskyttelse)
			VALUES (:id, :person_ident, :fornavn, :etternavn, :telefonnummer, :epost, :ansvarlig_veileder_id, :nav_enhet_id, :er_skjermet, :adressebeskyttelse);
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"person_ident" to cmd.personIdent,
				"fornavn" to cmd.fornavn,
				"etternavn" to cmd.etternavn,
				"telefonnummer" to cmd.telefonnummer,
				"epost" to cmd.epost,
				"ansvarlig_veileder_id" to cmd.ansvarligVeilederId,
				"nav_enhet_id" to cmd.navEnhet?.id,
				"er_skjermet" to cmd.erSkjermet,
				"adressebeskyttelse" to cmd.adressebeskyttelse?.name
			)
		)
	}

	fun insertDeltaker(cmd: DeltakerInput) {
		val sql = """
			INSERT INTO deltaker (
				id, bruker_id, gjennomforing_id, start_dato,
				slutt_dato, dager_per_uke, prosent_stilling, registrert_dato, innsok_begrunnelse, modified_at, kilde
			 )
			VALUES (
				:id, :bruker_id, :gjennomforing_id, :start_dato,
				:slutt_dato, :dager_per_uke, :prosent_stilling, :registrert_dato, :innsok_begrunnelse, :modified_at, :kilde
			);
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"bruker_id" to cmd.brukerId,
				"gjennomforing_id" to cmd.gjennomforingId,
				"start_dato" to cmd.startDato,
				"slutt_dato" to cmd.sluttDato,
				"dager_per_uke" to cmd.dagerPerUke,
				"prosent_stilling" to cmd.prosentStilling,
				"registrert_dato" to cmd.registrertDato,
				"innsok_begrunnelse" to cmd.innsokBegrunnelse,
				"modified_at" to cmd.endretDato,
				"kilde" to cmd.kilde.name
			)
		)
	}

	fun insertDeltakerStatus(cmd: DeltakerStatusInput) {
		val sql = """
			INSERT INTO deltaker_status (id, deltaker_id, gyldig_fra, status, aarsak, aktiv, created_at)
			VALUES (:id, :deltaker_id, :gyldigFra, :status, :aarsak, :aktiv, :createdAt)
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"deltaker_id" to cmd.deltakerId,
				"gyldigFra" to cmd.gyldigFra,
				"status" to cmd.status,
				"aarsak" to cmd.aarsak,
				"aktiv" to cmd.aktiv,
				"createdAt" to cmd.createdAt.toLocalDateTime()
			)
		)
	}

	fun insertGjennomforing(cmd: GjennomforingInput) {
		val sql = """
			INSERT INTO gjennomforing (id, tiltak_id, arrangor_id, navn, status, start_dato, slutt_dato, created_at, modified_at, opprettet_aar, lopenr, er_kurs)
			VALUES (:id, :tiltak_id, :arrangor_id, :navn, :status, :start_dato, :slutt_dato, :created_at, :modified_at, :opprettet_aar, :lopenr, :er_kurs)
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"tiltak_id" to cmd.tiltakId,
				"arrangor_id" to cmd.arrangorId,
				"navn" to cmd.navn,
				"status" to cmd.status,
				"start_dato" to cmd.startDato,
				"slutt_dato" to cmd.sluttDato,
				"created_at" to cmd.createdAt,
				"modified_at" to cmd.modifiedAt,
				"opprettet_aar" to cmd.opprettetAar,
				"lopenr" to cmd.lopenr,
				"er_kurs" to cmd.erKurs
			)
		)
	}

	fun deleteAllGjennomforinger() {
		val sql = """
			TRUNCATE gjennomforing CASCADE
		""".trimIndent()

		template.jdbcTemplate.update(sql)
	}

	fun insertNavAnsatt(cmd: NavAnsattInput) {
		val sql = """
			INSERT INTO nav_ansatt (id, nav_ident, navn, telefonnummer, epost)
			VALUES (:id, :nav_ident, :navn, :telefonnummer, :epost)
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"nav_ident" to cmd.navIdent,
				"navn" to cmd.navn,
				"telefonnummer" to cmd.telefonnummer,
				"epost" to cmd.epost
			)
		)
	}

	fun insertNavEnhet(cmd: NavEnhetInput) {
		val sql = """
			INSERT INTO nav_enhet(id, enhet_id, navn)
			VALUES (:id, :enhet_id, :navn)
		""".trimIndent()

		template.update(
			sql, parameters(
				"id" to cmd.id,
				"enhet_id" to cmd.enhetId,
				"navn" to cmd.navn
			)
		)
	}


	fun insertTiltak(cmd: TiltakInput) {
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

	fun insertEndringsmelding(cmd: EndringsmeldingInput) {
		val sql = """
			INSERT INTO endringsmelding(
				id,
				deltaker_id,
				utfort_av_nav_ansatt_id,
				opprettet_av_arrangor_ansatt_id,
				utfort_tidspunkt,
				status,
				type,
				innhold,
				created_at,
				modified_at
			)
			VALUES(
				:id,
				:deltakerId,
				:utfortAvNavAnsattId,
				:opprettetAvArrangorAnsattId,
				:utfortTidspunkt,
				:status,
				:type,
				CAST(:innhold as jsonb),
				:createdAt,
				:modifiedAt
			)
		""".trimIndent()

		val sqlParameters = parameters(
			"id" to cmd.id,
			"deltakerId" to cmd.deltakerId,
			"utfortAvNavAnsattId" to cmd.utfortAvNavAnsattId,
			"opprettetAvArrangorAnsattId" to cmd.opprettetAvArrangorAnsattId,
			"utfortTidspunkt" to cmd.utfortTidspunkt?.toOffsetDateTime(),
			"status" to cmd.status.name,
			"type" to cmd.type,
			"innhold" to cmd.innhold,
			"createdAt" to cmd.createdAt.toOffsetDateTime(),
			"modifiedAt" to cmd.modifiedAt.toOffsetDateTime(),
		)

		template.update(sql, sqlParameters)
	}

	fun deleteAllEndringsmeldinger() {
		val sql = """
			TRUNCATE endringsmelding CASCADE
		""".trimIndent()

		template.jdbcTemplate.update(sql)
	}

	fun deleteAllVurderinger() {
		val sql = """
			TRUNCATE vurdering CASCADE
		""".trimIndent()

		template.jdbcTemplate.update(sql)
	}

	fun insertTiltaksansvarligGjennomforingTilgang(cmd: TiltaksansvarligGjennomforingTilgangInput) {
		val sql = """
			INSERT INTO tiltaksansvarlig_gjennomforing_tilgang(id, nav_ansatt_id, gjennomforing_id, gyldig_til, created_at)
			 VALUES(:id, :navAnsattId, :gjennomforingId, :gyldigTil, :createdAt)
		""".trimIndent()

		val sqlParameters = parameters(
			"id" to cmd.id,
			"navAnsattId" to cmd.navAnsattId,
			"gjennomforingId" to cmd.gjennomforingId,
			"gyldigTil" to cmd.gyldigTil.toOffsetDateTime(),
			"createdAt" to cmd.createdAt.toOffsetDateTime()
		)

		template.update(sql, sqlParameters)
	}

	fun deleteAllTiltaksansvarligGjennomforingTilgang() {
		val sql = """
			TRUNCATE tiltaksansvarlig_gjennomforing_tilgang CASCADE
		""".trimIndent()

		template.jdbcTemplate.update(sql)
	}

	fun insertArrangorVeileder(input: ArrangorVeilederDboInput) {
		val sql = """
			INSERT INTO arrangor_veileder(
				id,
				ansatt_id,
				deltaker_id,
				gyldig_fra,
				gyldig_til,
				er_medveileder,
				modified_at,
				created_at
			)
			VALUES(:id, :ansattId, :deltakerId, :gyldigFra, :gyldigTil, :erMedveileder, :modifiedAt, :createdAt)
		""".trimIndent()

		val parameters = DbUtils.sqlParameters(
			"id" to input.id,
			"ansattId" to input.ansattId,
			"deltakerId" to input.deltakerId,
			"gyldigFra" to input.gyldigFra.toOffsetDateTime(),
			"gyldigTil" to input.gyldigTil.toOffsetDateTime(),
			"erMedveileder" to input.erMedveileder,
			"modifiedAt" to input.modifiedAt.toOffsetDateTime(),
			"createdAt" to input.createdAt.toOffsetDateTime(),
		)

		template.update(sql, parameters)
	}

	fun insertVurdering(vurdering: VurderingDbo) {
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
			"gyldig_fra" to LocalDateTime.now(),
			"gyldig_til" to null
		)

		template.update(sql, params)
	}
}
