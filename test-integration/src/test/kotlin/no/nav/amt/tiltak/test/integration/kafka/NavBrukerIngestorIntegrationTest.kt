package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.domain.tiltak.Oppholdsadresse
import no.nav.amt.tiltak.core.domain.tiltak.Vegadresse
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.utils.NavBrukerMsg
import no.nav.amt.tiltak.test.utils.AsyncUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID


class NavBrukerIngestorIntegrationTest : IntegrationTestBase() {

	@Autowired
	lateinit var navEnhetService: NavEnhetService

	@Autowired
	lateinit var brukerRepository: BrukerRepository

	@Autowired
	lateinit var deltakerService: DeltakerService

	@Autowired
	lateinit var navAnsattService: NavAnsattService

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}

	@Test
	internal fun `ingest - bruker finnes - skal oppdatere bruker`() {
		val msg = NavBrukerMsg(
			personId = BRUKER_1.id,
			personident = "ny ident",
			fornavn = "nytt",
			mellomnavn = null,
			etternavn = "navn",
			navVeilederId = BRUKER_1.ansvarligVeilederId,
			navEnhet = NavBrukerMsg.NavEnhetMsg(NAV_ENHET_1.id, NAV_ENHET_1.enhetId, NAV_ENHET_1.navn),
			telefon = "42",
			epost = "ny@epost",
			erSkjermet = true,
			adresse = TestData.lagAdresse().copy(oppholdsadresse = Oppholdsadresse(
				coAdressenavn = null,
				matrikkeladresse = null,
				vegadresse = Vegadresse(
					husnummer = "1",
					husbokstav = null,
					adressenavn = "Gateveien",
					tilleggsnavn = null,
					postnummer = "1234",
					poststed = "ANDEBY"
				),
			))
		)

		kafkaMessageSender.sendTilNavBrukerTopic(msg.personId, JsonUtils.toJsonString(msg))

		AsyncUtils.eventually {
			val bruker = brukerRepository.get(BRUKER_1.id)!!

			bruker.personIdent shouldBe msg.personident
			bruker.fornavn shouldBe msg.fornavn
			bruker.mellomnavn shouldBe msg.mellomnavn
			bruker.etternavn shouldBe msg.etternavn
			bruker.ansvarligVeilederId shouldBe msg.navVeilederId
			bruker.navEnhetId shouldBe msg.navEnhet?.id
			bruker.telefonnummer shouldBe msg.telefon
			bruker.epost shouldBe msg.epost
			bruker.erSkjermet shouldBe msg.erSkjermet
			bruker.adresse?.oppholdsadresse?.vegadresse?.poststed shouldBe "ANDEBY"
		}
	 }

	@Test
	internal fun `ingest - nav enhet endret - skal oppdatere nav enhet`() {
		val nyttNavEnhetNavn = "Nytt navn"
		val msg = NavBrukerMsg(
			personId = BRUKER_1.id,
			personident = BRUKER_1.personIdent,
			fornavn = BRUKER_1.fornavn,
			mellomnavn = BRUKER_1.mellomnavn,
			etternavn = BRUKER_1.etternavn,
			navVeilederId = BRUKER_1.ansvarligVeilederId,
			navEnhet = NavBrukerMsg.NavEnhetMsg(NAV_ENHET_1.id, NAV_ENHET_1.enhetId, nyttNavEnhetNavn),
			telefon = BRUKER_1.telefonnummer,
			epost = BRUKER_1.epost,
			erSkjermet = BRUKER_1.erSkjermet,
			adresse = BRUKER_1.adresse
		)

		kafkaMessageSender.sendTilNavBrukerTopic(msg.personId, JsonUtils.toJsonString(msg))

		AsyncUtils.eventually {
			navEnhetService.getNavEnhet(NAV_ENHET_1.id).navn shouldBe nyttNavEnhetNavn
		}
	}

	@Test
	internal fun `ingest - ny nav-veileder - skal hente ny nav-veileder og oppdatere bruker`() {
		val nyNavVeileder = NAV_ANSATT_1.copy(id = UUID.randomUUID(), navIdent = "X42")

		mockAmtPersonHttpServer.addAnsattResponse(nyNavVeileder.toModel(), "GET")

		val msg = NavBrukerMsg(
			personId = BRUKER_1.id,
			personident = BRUKER_1.personIdent,
			fornavn = BRUKER_1.fornavn,
			mellomnavn = BRUKER_1.mellomnavn,
			etternavn = BRUKER_1.etternavn,
			navVeilederId = nyNavVeileder.id,
			navEnhet = NavBrukerMsg.NavEnhetMsg(NAV_ENHET_1.id, NAV_ENHET_1.enhetId, NAV_ENHET_1.navn),
			telefon = BRUKER_1.telefonnummer,
			epost = BRUKER_1.epost,
			erSkjermet = BRUKER_1.erSkjermet,
			adresse = BRUKER_1.adresse
		)

		kafkaMessageSender.sendTilNavBrukerTopic(msg.personId, JsonUtils.toJsonString(msg))

		AsyncUtils.eventually {
			val ansatt = navAnsattService.getNavAnsatt(nyNavVeileder.id)
			ansatt.navIdent shouldBe nyNavVeileder.navIdent

			val bruker = brukerRepository.get(BRUKER_1.id)!!
			bruker.ansvarligVeilederId shouldBe nyNavVeileder.id
		}
	}

	@Test
	internal fun `ingest - mottar tombstone - skal slette bruker og deltakere`() {
		kafkaMessageSender.sendTilNavBrukerTopic(BRUKER_1.id, null)

		AsyncUtils.eventually {
			brukerRepository.get(BRUKER_1.id) shouldBe null

			deltakerService.hentDeltakereMedPersonId(BRUKER_1.id) shouldBe emptyList()
		}
	}
}
