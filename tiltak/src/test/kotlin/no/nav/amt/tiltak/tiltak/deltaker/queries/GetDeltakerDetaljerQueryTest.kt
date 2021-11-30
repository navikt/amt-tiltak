package no.nav.amt.tiltak.tiltak.deltaker.queries

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.util.*

class GetDeltakerDetaljerQueryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var getDeltakerDetaljerQuery: GetDeltakerDetaljerQuery

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		getDeltakerDetaljerQuery = GetDeltakerDetaljerQuery(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanAndInitDatabase(dataSource, "/get-deltaker-detaljer-query-data.sql")
	}

	test("Should get deltaker detaljer") {
		val deltakerDetaljer = getDeltakerDetaljerQuery.query(UUID.fromString("dc600c70-124f-4fe7-a687-b58439beb214"))
			?: fail("deltakerDetaljer should not be null")

		deltakerDetaljer.deltakerId shouldBe UUID.fromString("dc600c70-124f-4fe7-a687-b58439beb214")
		deltakerDetaljer.fornavn shouldBe "Bruker Fornavn"
		deltakerDetaljer.mellomnavn shouldBe null
		deltakerDetaljer.etternavn shouldBe "Bruker Etternavn"
		deltakerDetaljer.fodselsnummer shouldBe "12345678910"
		deltakerDetaljer.telefonnummer shouldBe "384"
		deltakerDetaljer.epost shouldBe "m@2.c"
		deltakerDetaljer.veilederFornavn shouldBe "Vashnir"
		deltakerDetaljer.veilederEtternavn shouldBe "Veiledersen"
		deltakerDetaljer.veilederTelefonnummer shouldBe "84756"
		deltakerDetaljer.veilederEpost shouldBe "vashnir.veiledersen@nav.no"
		deltakerDetaljer.oppstartDato shouldBe LocalDate.now().minusDays(1)
		deltakerDetaljer.sluttDato shouldBe LocalDate.now().plusDays(1)
		deltakerDetaljer.status shouldBe Deltaker.Status.GJENNOMFORES
		deltakerDetaljer.tiltakInstansId shouldBe UUID.fromString("b3420940-5479-48c8-b2fa-3751c7a33aa2")
		deltakerDetaljer.tiltakInstansOppstartDato shouldBe LocalDate.now()
		deltakerDetaljer.tiltakInstansSluttDato shouldBe LocalDate.now()
		deltakerDetaljer.tiltakNavn shouldBe "Tiltak1"
		deltakerDetaljer.tiltakKode shouldBe "AMO"
	}

})

