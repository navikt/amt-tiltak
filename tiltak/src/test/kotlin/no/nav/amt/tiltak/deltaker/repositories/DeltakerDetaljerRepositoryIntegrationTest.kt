package no.nav.amt.tiltak.deltaker.repositories

import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class DeltakerDetaljerRepositoryIntegrationTest : IntegrationTestBase() {

	lateinit var getDeltakerDetaljerQuery: GetDeltakerDetaljerQuery

	@BeforeEach
	internal fun setUp() {
		getDeltakerDetaljerQuery = GetDeltakerDetaljerQuery(NamedParameterJdbcTemplate(dataSource))
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	fun `Should get deltaker detaljer`() {
		val deltakerDetaljer = getDeltakerDetaljerQuery.query(TestData.DELTAKER_1.id)
			?: fail("deltakerDetaljer should not be null")

		deltakerDetaljer.deltakerId shouldBe TestData.DELTAKER_1.id
		deltakerDetaljer.fornavn shouldBe TestData.BRUKER_1.fornavn
		deltakerDetaljer.mellomnavn shouldBe null
		deltakerDetaljer.etternavn shouldBe TestData.BRUKER_1.etternavn
		deltakerDetaljer.fodselsnummer shouldBe TestData.BRUKER_1.fodselsnummer
		deltakerDetaljer.telefonnummer shouldBe TestData.BRUKER_1.telefonnummer
		deltakerDetaljer.epost shouldBe TestData.BRUKER_1.epost
		deltakerDetaljer.navEnhetNavn shouldBe TestData.NAV_ENHET_1.navn
		deltakerDetaljer.veilederNavn shouldBe TestData.NAV_ANSATT_1.navn
		deltakerDetaljer.veilederTelefonnummer shouldBe TestData.NAV_ANSATT_1.telefonnummer
		deltakerDetaljer.veilederEpost shouldBe TestData.NAV_ANSATT_1.epost
		deltakerDetaljer.startDato shouldBe TestData.DELTAKER_1.startDato
		deltakerDetaljer.sluttDato shouldBe TestData.DELTAKER_1.sluttDato
		deltakerDetaljer.status shouldBe Deltaker.Status.DELTAR
		deltakerDetaljer.gjennomforingId shouldBe TestData.GJENNOMFORING_1.id
		deltakerDetaljer.gjennomforingStartDato shouldBe TestData.GJENNOMFORING_1.startDato
		deltakerDetaljer.gjennomforingSluttDato shouldBe TestData.GJENNOMFORING_1.sluttDato
		deltakerDetaljer.tiltakNavn shouldBe TestData.TILTAK_1.navn
		deltakerDetaljer.tiltakKode shouldBe TestData.TILTAK_1.type
		deltakerDetaljer.innsokBegrunnelse shouldBe TestData.DELTAKER_1.innsokBegrunnelse
	}

	@Test
	fun `Should get deltaker detaljer if nav ansatt is null`() {
		val deltakerDetaljer = getDeltakerDetaljerQuery.query(TestData.DELTAKER_2.id)
		deltakerDetaljer shouldNotBe null
	}
}
