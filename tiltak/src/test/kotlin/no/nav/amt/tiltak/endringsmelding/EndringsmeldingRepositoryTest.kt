package no.nav.amt.tiltak.endringsmelding

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.domain.tiltak.EndringsmeldingStatusAarsak
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeCloseTo
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData.ENDRINGSMELDING_1_DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.ENDRINGSMELDING_1_DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

class EndringsmeldingRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: EndringsmeldingRepository

	lateinit var testRepository: TestDataRepository

	lateinit var objectMapper: ObjectMapper

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		val template = NamedParameterJdbcTemplate(dataSource)
		rootLogger.level = Level.WARN

		testRepository = TestDataRepository(template)

		objectMapper = JsonUtils.objectMapper

		repository = EndringsmeldingRepository(
			template,
			objectMapper,
		)
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		testRepository.deleteAllEndringsmeldinger()
	}

	test("getByGjennomforing - en endringsmelding - henter endringsmelding") {
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)

		val meldinger = repository.getByGjennomforing(DELTAKER_1.gjennomforingId)

		meldinger.size shouldBe 1
		meldinger[0].status shouldBe Endringsmelding.Status.AKTIV
	}

	test("getByGjennomforing - inaktiv endringsmelding - returnerer alle") {
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1.copy(
			id = UUID.randomUUID(),
			status = Endringsmelding.Status.UTDATERT)
		)

		val meldinger = repository.getByGjennomforing(DELTAKER_1.gjennomforingId)

		meldinger.size shouldBe 2
		meldinger.any {it.status == Endringsmelding.Status.UTDATERT } shouldBe true
		meldinger.any {it.status == Endringsmelding.Status.AKTIV } shouldBe true
	}

	test("getByDeltaker - henter endringsmelding") {
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_2)

		val meldinger = repository.getByDeltaker(DELTAKER_1.id)

		meldinger.size shouldBe 1
		meldinger[0].opprettetAvArrangorAnsattId shouldBe ARRANGOR_ANSATT_1.id
	}

	test("markerSomTilbakekalt - skal sette status til TILBAKEKALT") {
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)

		repository.markerSomTilbakekalt(ENDRINGSMELDING_1_DELTAKER_1.id)

		val oppdatertMelding = repository.get(ENDRINGSMELDING_1_DELTAKER_1.id)

		oppdatertMelding.status shouldBe Endringsmelding.Status.TILBAKEKALT
		oppdatertMelding.modifiedAt shouldBeAfter ENDRINGSMELDING_1_DELTAKER_1.modifiedAt

	}

	test("markerSomUtfort - skal sette status til UTFORT og nav ansatt") {
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)

		repository.markerSomUtfort(ENDRINGSMELDING_1_DELTAKER_1.id, NAV_ANSATT_1.id)

		val oppdatertMelding = repository.get(ENDRINGSMELDING_1_DELTAKER_1.id)

		oppdatertMelding.status shouldBe Endringsmelding.Status.UTFORT
		oppdatertMelding.utfortAvNavAnsattId shouldBe NAV_ANSATT_1.id
		oppdatertMelding.utfortTidspunkt!! shouldBeCloseTo ZonedDateTime.now()
		oppdatertMelding.modifiedAt shouldBeAfter ENDRINGSMELDING_1_DELTAKER_1.modifiedAt
	}

	test("markerAktiveSomUtdatert - skal sette status til UTDATERT") {
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)

		repository.markerAktiveSomUtdatert(
			DELTAKER_1.id,
			EndringsmeldingDbo.Type.valueOf(ENDRINGSMELDING_1_DELTAKER_1.type)
		)

		val oppdatertMelding = repository.get(ENDRINGSMELDING_1_DELTAKER_1.id)

		oppdatertMelding.status shouldBe Endringsmelding.Status.UTDATERT
		oppdatertMelding.modifiedAt shouldBeAfter ENDRINGSMELDING_1_DELTAKER_1.modifiedAt
	}

	test("getAktive - skal returnere tom liste hvis ingen deltakerIder er sendt inn") {
		repository.getAktive(emptyList()) shouldBe emptyList()
	}

	test("getAktive - skal hente aktive endringsmeldinger for deltakere") {
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)

		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_2)

		val aktiveMeldinger = repository.getAktive(listOf(DELTAKER_1.id, DELTAKER_2.id))

		aktiveMeldinger shouldHaveSize 2
		aktiveMeldinger.any { it.deltakerId == DELTAKER_1.id && it.status == Endringsmelding.Status.AKTIV }
		aktiveMeldinger.any { it.deltakerId == DELTAKER_2.id && it.status == Endringsmelding.Status.AKTIV }
	}

	test("insert - skal inserte aktiv leggTilOppstartsdatoEndringsmelding") {
		val id = UUID.randomUUID()
		val innhold = EndringsmeldingDbo.Innhold.LeggTilOppstartsdatoInnhold(LocalDate.now())

		repository.insert(
			id = id,
			deltakerId = DELTAKER_1.id,
			opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
			innhold = innhold,
			type = EndringsmeldingDbo.Type.LEGG_TIL_OPPSTARTSDATO
		)

		val endringsmelding = repository.get(id)

		endringsmelding.type shouldBe EndringsmeldingDbo.Type.LEGG_TIL_OPPSTARTSDATO
		endringsmelding.innhold shouldBe innhold
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
	}

	test("insert - skal inserte aktiv endreOppstartsdatoEndringsmelding") {
		val id = UUID.randomUUID()
		val innhold = EndringsmeldingDbo.Innhold.EndreOppstartsdatoInnhold(LocalDate.now())

		repository.insert(
			id = id,
			deltakerId = DELTAKER_1.id,
			opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
			innhold = innhold,
			type = EndringsmeldingDbo.Type.ENDRE_OPPSTARTSDATO

		)

		val endringsmelding = repository.get(id)

		endringsmelding.type shouldBe EndringsmeldingDbo.Type.ENDRE_OPPSTARTSDATO
		endringsmelding.innhold shouldBe innhold
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
	}

	test("insert - skal inserte aktiv forlengDeltakelseEndringsmelding") {
		val id = UUID.randomUUID()
		val innhold = EndringsmeldingDbo.Innhold.ForlengDeltakelseInnhold(LocalDate.now())

		repository.insert(
			id = id,
			deltakerId = DELTAKER_1.id,
			opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
			innhold = innhold,
			type = EndringsmeldingDbo.Type.FORLENG_DELTAKELSE

		)

		val endringsmelding = repository.get(id)

		endringsmelding.type shouldBe EndringsmeldingDbo.Type.FORLENG_DELTAKELSE
		endringsmelding.innhold shouldBe innhold
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
	}

	test("insert - skal inserte aktiv avsluttDeltakelseEndringsmelding") {
		val id = UUID.randomUUID()
		val innhold = EndringsmeldingDbo.Innhold.AvsluttDeltakelseInnhold(
			LocalDate.now(),
			EndringsmeldingStatusAarsak(EndringsmeldingStatusAarsak.Type.UTDANNING)
		)

		repository.insert(
			id = id,
			deltakerId = DELTAKER_1.id,
			opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
			innhold = innhold,
			type = EndringsmeldingDbo.Type.AVSLUTT_DELTAKELSE

		)

		val endringsmelding = repository.get(id)

		endringsmelding.type shouldBe EndringsmeldingDbo.Type.AVSLUTT_DELTAKELSE
		endringsmelding.innhold shouldBe innhold
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
	}

	test("insert - skal inserte aktiv deltakerIkkeAktuellEndringsmelding") {
		val id = UUID.randomUUID()
		val innhold = EndringsmeldingDbo.Innhold.DeltakerIkkeAktuellInnhold(
			EndringsmeldingStatusAarsak(EndringsmeldingStatusAarsak.Type.UTDANNING)
		)

		repository.insert(
			id = id,
			deltakerId = DELTAKER_1.id,
			opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
			innhold = innhold,
			type = EndringsmeldingDbo.Type.DELTAKER_IKKE_AKTUELL

		)

		val endringsmelding = repository.get(id)

		endringsmelding.type shouldBe EndringsmeldingDbo.Type.DELTAKER_IKKE_AKTUELL
		endringsmelding.innhold shouldBe innhold
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
	}

	test("deleteByDeltaker - skal slette alle endringsmeldinger på deltakeren") {
		val utfortMelding = ENDRINGSMELDING_1_DELTAKER_1.copy(
				id = UUID.randomUUID(),
				status = Endringsmelding.Status.UTFORT,
				utfortTidspunkt = ZonedDateTime.now().minusWeeks(1),
				utfortAvNavAnsattId = NAV_ANSATT_1.id,
			)

		testRepository.insertEndringsmelding(utfortMelding)
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)

		repository.deleteByDeltaker(DELTAKER_1.id)

		repository.getByDeltaker(DELTAKER_1.id) shouldHaveSize 0
	}
})
