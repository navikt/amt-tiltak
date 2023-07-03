package no.nav.amt.tiltak.arrangor

import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.arrangor.AmtArrangorService
import java.time.LocalDateTime
import java.util.UUID

class ArrangorServiceImplTest: FunSpec({
	lateinit var amtArrangorService: AmtArrangorService
	lateinit var arrangorRepository: ArrangorRepository

	lateinit var arrangorService: ArrangorServiceImpl

	beforeEach {
		amtArrangorService = mockk()
		arrangorRepository = mockk(relaxUnitFun = true)
		arrangorService = ArrangorServiceImpl(amtArrangorService, arrangorRepository)
	}

	test("getOrCreateArrangor - skal opprette arrangor hvis ikke finnes") {
		val navn = "Test"
		val organisasjonsnummer = "1234"
		val overordnetEnhetNavn = "Test2"
		val overordnetEnhetOrganisasjonsnummer = "5678"
		val arrangorId = UUID.randomUUID()
		val arrangor = Arrangor(
			id = arrangorId,
			navn = navn,
			organisasjonsnummer = organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = overordnetEnhetOrganisasjonsnummer,
			overordnetEnhetNavn = overordnetEnhetNavn
		)
		every { arrangorRepository.getByOrganisasjonsnummer(organisasjonsnummer) } returns null
		every { arrangorRepository.upsert(any(), any(), organisasjonsnummer, any(), any()) } returns ArrangorDbo(
			id = arrangorId,
			navn = navn,
			organisasjonsnummer = organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = overordnetEnhetOrganisasjonsnummer,
			overordnetEnhetNavn = overordnetEnhetNavn,
			createdAt = LocalDateTime.now(),
			modifiedAt = LocalDateTime.now(),
		)

		arrangorService.getOrCreateArrangor(arrangor)

		verify(exactly = 1) { arrangorRepository.upsert(any(), navn, organisasjonsnummer, overordnetEnhetNavn, overordnetEnhetOrganisasjonsnummer) }

	}

	test("getOrCreateArrangor - skal ikke opprette arrangor hvis finnes") {
		val navn = "Test"
		val organisasjonsnummer = "1234"
		val overordnetEnhetNavn = "Test2"
		val overordnetEnhetOrganisasjonsnummer = "5678"
		val arrangorId = UUID.randomUUID()
		val arrangor = Arrangor(
			id = arrangorId,
			navn = navn,
			organisasjonsnummer = organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = overordnetEnhetOrganisasjonsnummer,
			overordnetEnhetNavn = overordnetEnhetNavn
		)
		every { arrangorRepository.getByOrganisasjonsnummer(organisasjonsnummer) } returns ArrangorDbo(
			id = arrangorId,
			navn = navn,
			organisasjonsnummer = organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = overordnetEnhetOrganisasjonsnummer,
			overordnetEnhetNavn = overordnetEnhetNavn,
			createdAt = LocalDateTime.now(),
			modifiedAt = LocalDateTime.now(),
		)

		arrangorService.getOrCreateArrangor(arrangor)

		verify(exactly = 0) { arrangorRepository.upsert(any(), any(), any(), any(), any()) }
	}
})

