package no.nav.amt.tiltak.ansatt

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.data_publisher.DataPublisherService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class ArrangorAnsattServiceImplTest {

	lateinit var arrangorAnsattRepository: ArrangorAnsattRepository
	lateinit var personService: PersonService
	lateinit var arrangorAnsattTilgangService: ArrangorAnsattTilgangService
	lateinit var arrangorService: ArrangorService
	lateinit var publisherService: DataPublisherService

	lateinit var arrangorAnsattService: ArrangorAnsattServiceImpl

	@BeforeEach
	internal fun setUp() {
		arrangorAnsattRepository = mockk()
		personService = mockk()
		arrangorService = mockk()
		arrangorAnsattTilgangService = mockk()
		publisherService = mockk()


		arrangorAnsattService = ArrangorAnsattServiceImpl(
			arrangorAnsattRepository = arrangorAnsattRepository,
			personService = personService,
			arrangorService = arrangorService,
			dataPublisherService = publisherService,
		)

		every { publisherService.publish(any(), any()) } returns Unit

		arrangorAnsattService.arrangorAnsattTilgangService = arrangorAnsattTilgangService
	}

	@Test
	internal fun `getAnsatteSistSynkronisertEldreEnn gir kun de som er eldre enn dato`() {
		val ansatt1 = ansatt("1", LocalDateTime.now().minusDays(10))
		val ansatt2 = ansatt("2", LocalDateTime.now().minusDays(5))

		every { arrangorAnsattTilgangService.hentAnsattTilganger(any()) } returns listOf()
		every { arrangorService.getArrangorerById(any()) } returns listOf()
		every { arrangorAnsattRepository.getEldsteSistRolleSynkroniserteAnsatte(any()) } returns listOf(
			ansatt1,
			ansatt2
		)

		val ansatteToUpdate =
			arrangorAnsattService.getAnsatteSistSynkronisertEldreEnn(LocalDateTime.now().minusWeeks(1), 2)

		ansatteToUpdate.size shouldBe 1
		ansatteToUpdate.map { it.id } shouldBe listOf(ansatt1.id)
	}

	private fun ansatt(
		nr: String,
		sistSynkronisert: LocalDateTime
	): AnsattDbo {
		return AnsattDbo(
			id = UUID.randomUUID(),
			personligIdent = nr,
			fornavn = "Fornavn_$nr",
			mellomnavn = null,
			etternavn = "Etternavn_$nr",
			tilgangerSistSynkronisert = sistSynkronisert,
			createdAt = LocalDateTime.now(),
			modifiedAt = LocalDateTime.now(),
			sistVelykkedeInnlogging = LocalDateTime.MIN
		)
	}
}
