package no.nav.amt.tiltak.arrangor

import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetsregisterClient
import no.nav.amt.tiltak.clients.amt_enhetsregister.Virksomhet
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorUpdate
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.*
import java.util.function.Consumer

class ArrangorServiceImplTest: FunSpec({
	lateinit var enhetsregisterClient: EnhetsregisterClient
	lateinit var arrangorRepository: ArrangorRepository

	lateinit var arrangorService: ArrangorServiceImpl

	lateinit var transactionTemplate: TransactionTemplate

	beforeEach {
		enhetsregisterClient = mockk()
		arrangorRepository = mockk(relaxUnitFun = true)
		transactionTemplate = mockk()
		arrangorService = ArrangorServiceImpl(enhetsregisterClient, arrangorRepository, transactionTemplate)
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

	test("oppdaterArrangor - ny overordnet enhet - skal hente overordnet enhet og oppdatere arrangor") {
		val id = UUID.randomUUID()
		val arrangorUpdate = ArrangorUpdate("Foo", "1234", "6789")
		every { arrangorRepository.getByOrganisasjonsnummer(arrangorUpdate.organisasjonsnummer) } returns ArrangorDbo(
			id = id,
			navn = arrangorUpdate.navn,
			organisasjonsnummer = arrangorUpdate.organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = "9876",
			overordnetEnhetNavn = "Bar",
			createdAt = LocalDateTime.now(),
			modifiedAt = LocalDateTime.now(),
		)

		val overordnetEnhetNavn = "Ny Overordnet Enhet"

		every { enhetsregisterClient.hentVirksomhet(arrangorUpdate.overordnetEnhetOrganisasjonsnummer!!) } returns Virksomhet(
			navn = overordnetEnhetNavn,
			organisasjonsnummer = arrangorUpdate.overordnetEnhetOrganisasjonsnummer!!,
			overordnetEnhetNavn = "Baz",
			overordnetEnhetOrganisasjonsnummer = "4321",
		)

		every { transactionTemplate.executeWithoutResult(any<Consumer<TransactionStatus>>()) } answers {
			(firstArg() as Consumer<TransactionStatus>).accept(SimpleTransactionStatus())
		}

		arrangorService.oppdaterArrangor(arrangorUpdate)

		verify(exactly = 1) { enhetsregisterClient.hentVirksomhet(any()) }

		verify(exactly = 1) { arrangorRepository.update(any()) }

		verify(exactly = 1) { arrangorRepository.updateOverordnetEnhetNavn(arrangorUpdate.organisasjonsnummer, arrangorUpdate.navn) }

	}

})

