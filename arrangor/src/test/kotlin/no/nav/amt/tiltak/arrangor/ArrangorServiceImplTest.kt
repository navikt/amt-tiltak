package no.nav.amt.tiltak.arrangor

import io.kotest.core.spec.style.FunSpec
import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetsregisterClient
import no.nav.amt.tiltak.clients.amt_enhetsregister.Virksomhet
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.UUID

class ArrangorServiceImplTest: FunSpec({
	lateinit var enhetsregisterClient: EnhetsregisterClient
	lateinit var arrangorRepository: ArrangorRepository

	lateinit var arrangorService: ArrangorServiceImpl

	beforeEach {
		enhetsregisterClient = mockk()
		arrangorRepository = mockk(relaxUnitFun = true)
		arrangorService = ArrangorServiceImpl(enhetsregisterClient, arrangorRepository)
	}

	test("getOrCreateArrangor - skal opprette arrangor hvis ikke finnes") {
		val navn = "Test"
		val organisasjonsnummer = "1234"
		val overordnetEnhetNavn = "Test2"
		val overordnetEnhetOrganisasjonsnummer = "5678"
		every { arrangorRepository.getByOrganisasjonsnummer(organisasjonsnummer) } returns null
		every { enhetsregisterClient.hentVirksomhet(organisasjonsnummer) } returns Virksomhet(
			navn = navn,
			organisasjonsnummer = organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = overordnetEnhetOrganisasjonsnummer,
			overordnetEnhetNavn = overordnetEnhetNavn,
		)
		every { arrangorRepository.getById(any()) } returns ArrangorDbo(
			id = UUID.randomUUID(),
			navn = navn,
			organisasjonsnummer = organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = overordnetEnhetOrganisasjonsnummer,
			overordnetEnhetNavn = overordnetEnhetNavn,
			createdAt = LocalDateTime.now(),
			modifiedAt = LocalDateTime.now(),
		)

		arrangorService.getOrCreateArrangor(organisasjonsnummer)

		verify(exactly = 1) { arrangorRepository.insert(any(), navn, organisasjonsnummer, overordnetEnhetNavn, overordnetEnhetOrganisasjonsnummer) }

	}

	test("getOrCreateArrangor - skal ikke opprette arrangor hvis finnes") {
		val navn = "Test"
		val organisasjonsnummer = "1234"
		val overordnetEnhetNavn = "Test2"
		val overordnetEnhetOrganisasjonsnummer = "5678"
		every { arrangorRepository.getByOrganisasjonsnummer(organisasjonsnummer) } returns ArrangorDbo(
			id = UUID.randomUUID(),
			navn = navn,
			organisasjonsnummer = organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = overordnetEnhetOrganisasjonsnummer,
			overordnetEnhetNavn = overordnetEnhetNavn,
			createdAt = LocalDateTime.now(),
			modifiedAt = LocalDateTime.now(),
		)

		arrangorService.getOrCreateArrangor(organisasjonsnummer)

		verify(exactly = 0) { arrangorRepository.insert(any(), any(), any(), any(), any()) }
	}

})

