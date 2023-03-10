package no.nav.amt.tiltak.veileder

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeilederInput
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_2
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import java.time.ZonedDateTime
import java.util.*

class ArrangorVeilederServiceImplTest {

	lateinit var arrangorVeilederRepository: ArrangorVeilederRepository

	lateinit var arrangorAnsattService: ArrangorAnsattService

	lateinit var arrangorVeilederServiceImpl: ArrangorVeilederServiceImpl

	lateinit var deltakerService: DeltakerService

	lateinit var gjennomforingService: GjennomforingService

	lateinit var transactionTemplate: TransactionTemplate

	@BeforeEach
	fun setup() {
		arrangorVeilederRepository = mockk(relaxUnitFun = true)
		arrangorAnsattService = mockk()
		deltakerService = mockk()
		gjennomforingService = mockk()
		transactionTemplate = mockk()

		arrangorVeilederServiceImpl = ArrangorVeilederServiceImpl(
			arrangorAnsattService = arrangorAnsattService,
			arrangorVeilederRepository = arrangorVeilederRepository,
			deltakerService = deltakerService,
			gjennomforingService = gjennomforingService,
			transactionTemplate = transactionTemplate,
		)
	}

	@Test
	fun `opprettVeiledere - to medveiledere finnes fra før - eldste inaktiveres`() {

		val deltakerIder = listOf(deltaker1.id)
		val input = listOf(
			ArrangorVeilederInput(UUID.randomUUID(), true),
			ArrangorVeilederInput(UUID.randomUUID(), true),
		)

		every { transactionTemplate.executeWithoutResult(any<java.util.function.Consumer<TransactionStatus>>()) } answers {
			(firstArg() as java.util.function.Consumer<TransactionStatus>).accept(SimpleTransactionStatus())
		}

		every {
			deltakerService.hentDeltakere(deltakerIder)
		} returns listOf(deltaker1)

		every {
			gjennomforingService.getGjennomforing(deltaker1.gjennomforingId)
		} returns gjennomforing


		val medveilederSomSkalErstattes = veilederDbo(deltakerIder.first(), ZonedDateTime.now().minusWeeks(20))
		val medveilederSomIkkeSkalErstattes = veilederDbo(deltakerIder.first(), ZonedDateTime.now().minusWeeks(1))

		every { arrangorVeilederRepository.getAktiveForDeltakere(deltakerIder) } returns listOf(
			medveilederSomSkalErstattes,
			medveilederSomIkkeSkalErstattes,
		)

		arrangorVeilederServiceImpl.opprettVeiledere(input, deltakerIder)

		verify(exactly = 1) {
			arrangorVeilederRepository.inaktiverVeiledereForDeltakere(
				input.map { it.ansattId },
				deltakerIder
			)
		}

		verify(exactly = 1) {
			arrangorVeilederRepository.inaktiverVeiledere(listOf(medveilederSomSkalErstattes.id))
		}

		verify(exactly = 1) {
			arrangorVeilederRepository.opprettVeiledere(
				match { it.first().ansattId == input.first().ansattId && it.last().ansattId == input.last().ansattId },
				deltakerIder,
			)
		}
	}

	@Test
	fun `opprettVeiledere - flere deltakere med flere medveiledere finnes fra før - eldste medveiledere inaktiveres`() {

		val deltakerIder = listOf(deltaker1.id, deltaker2.id)
		val input = listOf(
			ArrangorVeilederInput(UUID.randomUUID(), true),
			ArrangorVeilederInput(UUID.randomUUID(), true),
		)

		every {
			deltakerService.hentDeltakere(deltakerIder)
		} returns listOf(deltaker1, deltaker2)

		every {
			gjennomforingService.getGjennomforing(deltaker1.gjennomforingId)
		} returns gjennomforing


		every { transactionTemplate.executeWithoutResult(any<java.util.function.Consumer<TransactionStatus>>()) } answers {
			(firstArg() as java.util.function.Consumer<TransactionStatus>).accept(SimpleTransactionStatus())
		}

		val medveiledereSomSkalErstattes = listOf(
			veilederDbo(deltakerIder.first(), ZonedDateTime.now().minusWeeks(20)),
			veilederDbo(deltakerIder.last(), ZonedDateTime.now().minusWeeks(30)),
			veilederDbo(deltakerIder.last(), ZonedDateTime.now().minusWeeks(10)),
		)

		val medveiledereSomIkkeSkalErstattes = listOf(
			veilederDbo(deltakerIder.first(), ZonedDateTime.now().minusWeeks(1)),
			veilederDbo(deltakerIder.last(), ZonedDateTime.now().minusWeeks(2))
		)

		every { arrangorVeilederRepository.getAktiveForDeltakere(deltakerIder) } returns
			medveiledereSomSkalErstattes.plus(medveiledereSomIkkeSkalErstattes)

		arrangorVeilederServiceImpl.opprettVeiledere(input, deltakerIder)

		verify(exactly = 1) {
			arrangorVeilederRepository.inaktiverVeiledereForDeltakere(
				input.map { it.ansattId },
				deltakerIder
			)
		}

		verify(exactly = 1) {
			arrangorVeilederRepository.inaktiverVeiledere(match {
				it.containsAll(medveiledereSomSkalErstattes.map { v -> v.id })
					&& it.size == medveiledereSomSkalErstattes.size
			})
		}

		verify(exactly = 1) {
			arrangorVeilederRepository.opprettVeiledere(
				match { it.first().ansattId == input.first().ansattId && it.last().ansattId == input.last().ansattId },
				deltakerIder,
			)
		}
	}

	private fun veilederDbo(deltakerId: UUID, opprettet: ZonedDateTime) = ArrangorVeilederDbo(
			id = UUID.randomUUID(),
			ansattId = UUID.randomUUID(),
			deltakerId = deltakerId,
			erMedveileder = true,
			gyldigFra  = opprettet,
			gyldigTil  = ZonedDateTime.now().plusYears(1),
			createdAt  = opprettet,
			modifiedAt = opprettet,
	)

	private val deltaker1 = DELTAKER_1.toDeltaker(BRUKER_1, DELTAKER_1_STATUS_1)
	private val deltaker2 = DELTAKER_2.toDeltaker(BRUKER_2, DELTAKER_2_STATUS_1)
	private val gjennomforing = GJENNOMFORING_1.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_1.toArrangor())

}
