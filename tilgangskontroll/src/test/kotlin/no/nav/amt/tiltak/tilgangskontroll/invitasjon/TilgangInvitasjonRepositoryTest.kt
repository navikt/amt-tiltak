package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.commands.InsertTilgangForesporselCommand
import no.nav.amt.tiltak.test.database.data.commands.InsertTilgangInvitasjonCommand
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.ZonedDateTime
import java.util.*
import kotlin.NoSuchElementException

class TilgangInvitasjonRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: TilgangInvitasjonRepository

	lateinit var testDataRepository: TestDataRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = TilgangInvitasjonRepository(NamedParameterJdbcTemplate(dataSource))

		testDataRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("get - skal hente invitasjon") {
		val foresporselId = UUID.randomUUID()

		testDataRepository.insertTilgangForesporsel(
			InsertTilgangForesporselCommand(
				id = foresporselId,
				personligIdent = "",
				fornavn = "",
				etternavn = "",
				gjennomforingId = GJENNOMFORING_1.id,
			)
		)

		val invitasjonId = UUID.randomUUID()
		val now = ZonedDateTime.now()

		testDataRepository.insertTilgangInvitasjon(
			InsertTilgangInvitasjonCommand(
				id = invitasjonId,
				gjennomforingId = GJENNOMFORING_1.id,
				gyldigTil = now,
				opprettetAvNavAnsattId = NAV_ANSATT_1.id,
				erBrukt = true,
				tidspunktBrukt = now,
				tilgangForesporselId = foresporselId,
		))

		val invitasjon = repository.get(invitasjonId)

		invitasjon.id shouldBe invitasjonId
		invitasjon.gjennomforingId shouldBe GJENNOMFORING_1.id
		invitasjon.gyldigTil.toLocalDate() shouldBe now.toLocalDate()
		invitasjon.opprettetAvNavAnsattId shouldBe NAV_ANSATT_1.id
		invitasjon.erBrukt shouldBe true
		invitasjon.tidspunktBrukt?.toLocalDate() shouldBe now.toLocalDate()
		invitasjon.tilgangForesporselId shouldBe foresporselId
	}

	test("get - skal kaste exception hvis ikke finnes") {
		shouldThrow<NoSuchElementException> {
			repository.get(UUID.randomUUID())
		}
	}

	test("opprettInvitasjon - skal opprette invitasjon") {
		val invitasjonId = UUID.randomUUID()
		val now = ZonedDateTime.now()

		repository.opprettInvitasjon(
			id = invitasjonId,
			gjennomforingId = GJENNOMFORING_1.id,
			opprettetAvNavAnsattId = NAV_ANSATT_1.id,
			gydligTil = now
		)

		val invitasjon = repository.get(invitasjonId)

		invitasjon.id shouldBe invitasjonId
		invitasjon.gjennomforingId shouldBe GJENNOMFORING_1.id
		invitasjon.opprettetAvNavAnsattId shouldBe NAV_ANSATT_1.id
		invitasjon.gyldigTil.toLocalDate() shouldBe now.toLocalDate()
	}

	test("settTilBrukt - skal marker invitasjon som brukt") {

		val foresporselId = UUID.randomUUID()
		val invitasjonId = UUID.randomUUID()
		val now = ZonedDateTime.now()

		testDataRepository.insertTilgangForesporsel(
			InsertTilgangForesporselCommand(
				id = foresporselId,
				personligIdent = "",
				fornavn = "",
				etternavn = "",
				gjennomforingId = GJENNOMFORING_1.id,
			)
		)

		testDataRepository.insertTilgangInvitasjon(
			InsertTilgangInvitasjonCommand(
				id = invitasjonId,
				gjennomforingId = GJENNOMFORING_1.id,
				gyldigTil = now,
				opprettetAvNavAnsattId = NAV_ANSATT_1.id,
			)
		)

		repository.settTilBrukt(invitasjonId, foresporselId)

		val invitasjon = repository.get(invitasjonId)

		invitasjon.erBrukt shouldBe true
		invitasjon.tidspunktBrukt?.toLocalDate() shouldBe now.toLocalDate()
		invitasjon.tilgangForesporselId shouldBe foresporselId
	}

	test("slettInvitasjon - skal slette invitasjon") {
		val invitasjonId = UUID.randomUUID()

		testDataRepository.insertTilgangInvitasjon(
			InsertTilgangInvitasjonCommand(
				id = invitasjonId,
				gjennomforingId = GJENNOMFORING_1.id,
				gyldigTil = ZonedDateTime.now(),
				opprettetAvNavAnsattId = NAV_ANSATT_1.id,
			)
		)

		repository.slettInvitasjon(invitasjonId)

		shouldThrow<NoSuchElementException> {
			repository.get(invitasjonId)
		}
	}

})
