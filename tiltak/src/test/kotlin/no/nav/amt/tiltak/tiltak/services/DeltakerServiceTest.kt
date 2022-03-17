package no.nav.amt.tiltak.tiltak.services

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forOne
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.*
import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatuser
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.deltaker.service.DeltakerServiceImpl
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class DeltakerServiceTest: StringSpec ({
	val fodselsnummer = "12345678904"
	val gjennomforingId = UUID.randomUUID()
	val deltakerId = UUID.randomUUID()
	val defaultBruker = Bruker(id = UUID.randomUUID(), "GRØNN",null,"KOPP", fodselsnummer, null)

	val defaultDeltaker = Deltaker(
		id = deltakerId,
		bruker = defaultBruker,
		startDato = LocalDate.now(),
		sluttDato = LocalDate.now(),
		statuser = DeltakerStatuser.settAktivStatus(Deltaker.Status.VENTER_PA_OPPSTART),
		registrertDato = LocalDateTime.now(),
		dagerPerUke = 4,
		prosentStilling = 0.8F
	)
	val defaultDeltakerDbo = DeltakerDbo(defaultDeltaker)


	lateinit var service: DeltakerService
	val deltakerRepository = mockk<DeltakerRepository>()
	val deltakerStatusRepository = mockk<DeltakerStatusRepository>()
	val brukerService = mockk<BrukerService>()

	val deltakerStatusDboer = fun(statuser: List<DeltakerStatus>, deltakerId: UUID) =
		DeltakerStatusDbo.fromDeltakerStatuser(statuser, deltakerId)

	val deltakerStatuser = fun(aktivStatus: Deltaker.Status, dekativerteStatuser: Set<Deltaker.Status>): List<DeltakerStatus> {
		return dekativerteStatuser.map { status -> DeltakerStatus.nyInaktiv(status) } + DeltakerStatus.nyAktiv(aktivStatus)
	}

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

	"upsertDeltaker - ny deltaker - alt nødvendig lagres" {

		val deltaker = defaultDeltaker
		val deltakerDbo = defaultDeltakerDbo

		every { deltakerRepository.get(deltaker.id) } returns null
		every { brukerService.getOrCreate(fodselsnummer) } returns defaultBruker.id
		every { deltakerRepository.insert(any(), any(), any(), any(), any(), any(), any(), any()) } returns deltakerDbo
		every { deltakerStatusRepository.upsert(any<List<DeltakerStatusDbo>>()) } returns Unit

		service.upsertDeltaker(fodselsnummer, gjennomforingId, deltaker)

		verify(exactly = 1) { deltakerRepository.insert(
			id = deltaker.id,
			brukerId = defaultBruker.id,
			gjennomforingId = gjennomforingId,
			startDato = deltaker.startDato,
			sluttDato = deltaker.sluttDato,
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
			registrertDato = deltaker.registrertDato) }
		verify(exactly = 0) { deltakerRepository.update(any()) }
		verify(exactly = 1) { deltakerStatusRepository.upsert(listOf(DeltakerStatusDbo(
			id = deltaker.statuser.current.id,
			deltakerId = deltaker.id,
			status = Deltaker.Status.VENTER_PA_OPPSTART,
			endretDato = deltaker.statuser.current.endretDato,
			aktiv = true))) }
	}


	"upsertDeltaker - oppdatere deltaker med ny status - alle statuser lagres" {

		val eksisterendeStatuser = deltakerStatuser(Deltaker.Status.VENTER_PA_OPPSTART, setOf())
		val nyStatus = deltakerStatuser(Deltaker.Status.DELTAR, setOf())

		val deltaker = defaultDeltaker.copy(statuser = DeltakerStatuser(nyStatus))
		val deltakerDbo = defaultDeltakerDbo

		every { deltakerRepository.get(deltaker.id) } returns defaultDeltakerDbo
		every { deltakerStatusRepository.getStatuserForDeltaker(defaultDeltakerDbo.id) } returns
			deltakerStatusDboer(eksisterendeStatuser, deltaker.id)

		every { deltakerRepository.update(any()) } returns deltakerDbo
		every { deltakerStatusRepository.upsert(any<List<DeltakerStatusDbo>>()) } returns Unit

		service.upsertDeltaker(fodselsnummer, gjennomforingId, deltaker)

		verify(exactly = 0) { deltakerRepository.insert(any(), any(), any(), any(), any(), any(), any(), any()) }
		verify(exactly = 1) { deltakerRepository.update(any()) }
		val slot = slot<List<DeltakerStatusDbo>>()

		verify(exactly = 1) { deltakerStatusRepository.upsert(capture(slot)) }
		slot.captured shouldContain DeltakerStatusDbo(
			id = eksisterendeStatuser[0].id,
			deltakerId = deltaker.id,
			status = eksisterendeStatuser[0].status,
			endretDato = eksisterendeStatuser[0].endretDato,
			aktiv = false)
		slot.captured.forOne {
			it.deltakerId shouldBe deltaker.id
			it.status shouldBe nyStatus[0].status
			it.endretDato shouldBe nyStatus[0].endretDato
			it.aktiv shouldBe true
		}
	}

})
