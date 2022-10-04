package no.nav.amt.tiltak.tiltak.services

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerUpsert
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.deltaker.dbo.DeltakerInsertDbo
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.deltaker.service.DeltakerServiceImpl
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class DeltakerServiceTest: StringSpec ({
	val fodselsnummer = "12345678904"
	val deltakerId = UUID.randomUUID()
	val defaultBruker = Bruker(
		id = UUID.randomUUID(),
		fornavn = "GRØNN",
		mellomnavn = null,
		etternavn = "KOPP",
		fodselsnummer = fodselsnummer,
		telefonnummer = "1234",
		epost = "foo@bar.baz",
		navVeilederId = null,
		navEnhet = null,
	)

	val defaultStatus = DeltakerStatus(
		UUID.randomUUID(),
		Deltaker.Status.VENTER_PA_OPPSTART,
		null,
		LocalDateTime.now(),
		LocalDateTime.now(),
		true
	)
	val defaultDeltaker = Deltaker(
		id = deltakerId,
		bruker = defaultBruker,
		startDato = LocalDate.now(),
		sluttDato = LocalDate.now(),
		status = defaultStatus,
		registrertDato = LocalDateTime.now(),
		dagerPerUke = 4,
		prosentStilling = 0.8F,
		gjennomforingId = UUID.randomUUID()
	)


	lateinit var service: DeltakerService
	val deltakerRepository = mockk<DeltakerRepository>()
	val deltakerStatusRepository = mockk<DeltakerStatusRepository>()
	val brukerService = mockk<BrukerService>()

	beforeEach {

		service = DeltakerServiceImpl(
			deltakerRepository,
			deltakerStatusRepository,
			brukerService,
			mockk()
		)
	}

	afterEach {
		clearAllMocks()
	}
	isolationMode = IsolationMode.SingleInstance

	"upsertDeltaker - ny deltaker - kaller insert deltaker" {

		val deltaker = defaultDeltaker
		val deltakerInsertDbo = DeltakerInsertDbo(
			id = deltaker.id,
			brukerId = defaultBruker.id,
			gjennomforingId = deltaker.gjennomforingId,
			startDato = deltaker.startDato,
			sluttDato = deltaker.sluttDato,
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
			registrertDato = deltaker.registrertDato
		)
		val deltakerUpsert = DeltakerUpsert(
			id = deltaker.id,
			gjennomforingId = deltaker.gjennomforingId,
			startDato = deltaker.startDato,
			sluttDato = deltaker.sluttDato,
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
			registrertDato = deltaker.registrertDato,
			innsokBegrunnelse = deltaker.innsokBegrunnelse
		)
		every { deltakerRepository.get(deltaker.id) } returns null
		every { brukerService.getOrCreate(fodselsnummer) } returns defaultBruker.id
		every { deltakerRepository.insert(deltakerInsertDbo)} returns Unit

		service.upsertDeltaker(fodselsnummer, deltakerUpsert)

		verify(exactly = 1) { deltakerRepository.insert(deltakerInsertDbo) }
		verify(exactly = 0) { deltakerRepository.update(any()) }

	}

})
