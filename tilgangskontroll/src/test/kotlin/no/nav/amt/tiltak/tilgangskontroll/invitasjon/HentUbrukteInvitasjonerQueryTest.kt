package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.commands.InsertTilgangInvitasjonCommand
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

class HentUbrukteInvitasjonerQueryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var hentUbrukteInvitasjonerQuery: HentUbrukteInvitasjonerQuery

	lateinit var testDataRepository: TestDataRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		hentUbrukteInvitasjonerQuery = HentUbrukteInvitasjonerQuery(NamedParameterJdbcTemplate(dataSource))

		testDataRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("skal hente ubrukte invitasjoner") {
		val invitasjonId = UUID.randomUUID()
		val gyldigTil = ZonedDateTime.now()

		testDataRepository.insertTilgangInvitasjon(InsertTilgangInvitasjonCommand(
			id = invitasjonId,
			gjennomforingId = GJENNOMFORING_1.id,
			gyldigTil = gyldigTil,
			opprettetAvNavAnsattId = NAV_ANSATT_1.id
		))

		val invitasjon = hentUbrukteInvitasjonerQuery.query(GJENNOMFORING_1.id).first()

		invitasjon.id shouldBe invitasjonId
		invitasjon.gyldigTilDato.toLocalDate() shouldBe gyldigTil.toLocalDate()
		invitasjon.opprettetDato.toLocalDate() shouldBe LocalDate.now()
		invitasjon.opprettetAvNavIdent shouldBe NAV_ANSATT_1.nav_ident
	}


})
