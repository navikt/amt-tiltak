package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.ansatt.ArrangorAnsattRepository
import no.nav.amt.tiltak.arrangor.ArrangorRepository
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorAnsatt
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.kafka.ansatt_ingestor.model.AnsattDto
import no.nav.amt.tiltak.kafka.ansatt_ingestor.model.Arrangor
import no.nav.amt.tiltak.kafka.ansatt_ingestor.model.TilknyttetArrangor
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.utils.AsyncUtils.eventually
import no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang.AnsattRolleService
import no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang.MineDeltakerlisterServiceImpl
import no.nav.amt.tiltak.veileder.ArrangorVeilederServiceImpl
import no.nav.common.json.JsonUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class AnsattIngestorTest : IntegrationTestBase() {
	@Autowired
	private lateinit var arrangorRepository: ArrangorRepository
	@Autowired
	private lateinit var arrangorAnsattRepository: ArrangorAnsattRepository
	@Autowired
	private lateinit var ansattRolleService: AnsattRolleService
	@Autowired
	private lateinit var mineDeltakerlisterServiceImpl: MineDeltakerlisterServiceImpl
	@Autowired
	private lateinit var veilederServiceImpl: ArrangorVeilederServiceImpl

	@BeforeEach
	fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}

	@Test
	fun `ingestAnsatt - source er null - kaster meldingen`() {
		val ansattDto = ansattDto(arrangorer = listOf(tilnyttetArrangor()))

		kafkaMessageSender.sendTilAmtArrangorAnsattTopic(JsonUtils.toJson(ansattDto))
		Thread.sleep(2000) //Må vente for å vente på at kafka meldingen blir lest.

		arrangorAnsattRepository.get(ansattDto.id) shouldBe null
	}

	@Test
	fun `ingestAnsatt - ansatt finnes ikke fra for - lagrer ansatt`() {
		val gjennomforingId = TestData.GJENNOMFORING_1.id
		val tilknyttetArrangor = tilnyttetArrangor(gjennomforingId = gjennomforingId)
		val ansattDto = ansattDto(
			source = "amt-arrangor",
			arrangorer = listOf(tilknyttetArrangor)
		)
		kafkaMessageSender.sendTilAmtArrangorAnsattTopic(JsonUtils.toJson(ansattDto))

		eventually {
			arrangorRepository.getById(tilknyttetArrangor.arrangorId) shouldNotBe null

			val ansatt = arrangorAnsattRepository.get(ansattDto.id)
			ansatt?.personligIdent shouldBe ansattDto.personalia.personident

			val roller = ansattRolleService.hentAktiveRoller(ansattDto.id)
			roller.size shouldBe 1
			roller.first().arrangorId shouldBe tilknyttetArrangor.arrangorId
			roller.first().roller.size shouldBe 1
			roller.first().roller.first() shouldBe ArrangorAnsattRolle.KOORDINATOR

			val koordinatorFor = mineDeltakerlisterServiceImpl.hent(ansattDto.id)
			koordinatorFor.size shouldBe 1
			koordinatorFor.first() shouldBe tilknyttetArrangor.koordinator.first()

			val veilederFor = veilederServiceImpl.hentDeltakereForVeileder(ansattDto.id)
			veilederFor.size shouldBe 0
		}
	}

	@Test
	fun `ingestAnsatt - ansatt finnes fra for - oppdaterer ansatt`() {
		val tilknyttetArrangor = TilknyttetArrangor(
			arrangorId = TestData.ARRANGOR_1.id,
			arrangor = Arrangor(
				id = TestData.ARRANGOR_1.id,
				navn = TestData.ARRANGOR_1.navn,
				organisasjonsnummer = TestData.ARRANGOR_1.organisasjonsnummer,
				overordnetArrangorId = null
			),
			overordnetArrangor = null,
			roller = listOf(ArrangorAnsatt.AnsattRolle.KOORDINATOR, ArrangorAnsatt.AnsattRolle.VEILEDER),
			koordinator = listOf(TestData.GJENNOMFORING_1.id),
			veileder = listOf(ArrangorAnsatt.VeilederDto(TestData.DELTAKER_2.id, ArrangorAnsatt.VeilederType.MEDVEILEDER))
		)
		val ansattDto = AnsattDto(
			id = TestData.ARRANGOR_ANSATT_3.id,
			personalia = ArrangorAnsatt.PersonaliaDto(
				personident = TestData.ARRANGOR_ANSATT_3.personligIdent,
				navn = ArrangorAnsatt.Navn(TestData.ARRANGOR_ANSATT_3.fornavn, TestData.ARRANGOR_ANSATT_3.mellomnavn, TestData.ARRANGOR_ANSATT_3.etternavn),
				personId = null
			),
			source = "amt-arrangor",
			arrangorer = listOf(tilknyttetArrangor)
		)

		kafkaMessageSender.sendTilAmtArrangorAnsattTopic(JsonUtils.toJson(ansattDto))

		eventually {
			val ansatt = arrangorAnsattRepository.get(ansattDto.id)
			ansatt?.personligIdent shouldBe ansattDto.personalia.personident

			val roller = ansattRolleService.hentAktiveRoller(ansattDto.id)
			roller.size shouldBe 1
			roller.first().arrangorId shouldBe tilknyttetArrangor.arrangorId
			roller.first().roller.size shouldBe 2
			roller.first().roller.find { it == ArrangorAnsattRolle.KOORDINATOR } shouldNotBe null
			roller.first().roller.find { it == ArrangorAnsattRolle.VEILEDER } shouldNotBe null

			val koordinatorFor = mineDeltakerlisterServiceImpl.hent(ansattDto.id)
			koordinatorFor.size shouldBe 1
			koordinatorFor.first() shouldBe tilknyttetArrangor.koordinator.first()

			val veilederFor = veilederServiceImpl.hentDeltakereForVeileder(ansattDto.id)
			veilederFor.size shouldBe 1
			veilederFor.first().deltakerId shouldBe TestData.DELTAKER_2.id
			veilederFor.first().erMedveileder shouldBe true
		}
	}

	@Test
	fun `ingestAnsatt - ansatt finnes fra for, fjerner deltakerliste og deltaker - oppdaterer ansatt`() {
		val tilknyttetArrangor = TilknyttetArrangor(
			arrangorId = TestData.ARRANGOR_1.id,
			arrangor = Arrangor(
				id = TestData.ARRANGOR_1.id,
				navn = TestData.ARRANGOR_1.navn,
				organisasjonsnummer = TestData.ARRANGOR_1.organisasjonsnummer,
				overordnetArrangorId = null
			),
			overordnetArrangor = null,
			roller = listOf(ArrangorAnsatt.AnsattRolle.KOORDINATOR, ArrangorAnsatt.AnsattRolle.VEILEDER),
			koordinator = emptyList(),
			veileder = emptyList()
		)
		val ansattDto = AnsattDto(
			id = TestData.ARRANGOR_ANSATT_1.id,
			personalia = ArrangorAnsatt.PersonaliaDto(
				personident = TestData.ARRANGOR_ANSATT_1.personligIdent,
				navn = ArrangorAnsatt.Navn(TestData.ARRANGOR_ANSATT_1.fornavn, TestData.ARRANGOR_ANSATT_1.mellomnavn, TestData.ARRANGOR_ANSATT_1.etternavn),
				personId = null
			),
			source = "amt-arrangor",
			arrangorer = listOf(tilknyttetArrangor)
		)

		kafkaMessageSender.sendTilAmtArrangorAnsattTopic(JsonUtils.toJson(ansattDto))

		eventually {
			val ansatt = arrangorAnsattRepository.get(ansattDto.id)
			ansatt?.personligIdent shouldBe ansattDto.personalia.personident

			val roller = ansattRolleService.hentAktiveRoller(ansattDto.id)
			roller.size shouldBe 1
			roller.first().arrangorId shouldBe tilknyttetArrangor.arrangorId
			roller.first().roller.size shouldBe 2
			roller.first().roller.find { it == ArrangorAnsattRolle.KOORDINATOR } shouldNotBe null
			roller.first().roller.find { it == ArrangorAnsattRolle.VEILEDER } shouldNotBe null

			val koordinatorFor = mineDeltakerlisterServiceImpl.hent(ansattDto.id)
			koordinatorFor.size shouldBe 0

			val veilederFor = veilederServiceImpl.hentDeltakereForVeileder(ansattDto.id)
			veilederFor.size shouldBe 0
		}
	}

	private fun ansattDto(
		id: UUID = UUID.randomUUID(),
		source: String? = null,
		arrangorer: List<TilknyttetArrangor>
	): AnsattDto {
		return AnsattDto(
			id = id,
			source = source,
			personalia = ArrangorAnsatt.PersonaliaDto(
				personident = "12345678910",
				personId = null,
				navn = ArrangorAnsatt.Navn("Fornavn", null, "Etternavn")
			),
			arrangorer = arrangorer
		)
	}

	private fun tilnyttetArrangor(
		arrangorId: UUID = UUID.randomUUID(),
		gjennomforingId: UUID = UUID.randomUUID()
	): TilknyttetArrangor {
		return TilknyttetArrangor(
			arrangorId = arrangorId,
			arrangor = Arrangor(
				id = arrangorId,
				navn = "Navn AS",
				organisasjonsnummer = "98765",
				overordnetArrangorId = null
			),
			overordnetArrangor = null,
			roller = listOf(ArrangorAnsatt.AnsattRolle.KOORDINATOR),
			koordinator = listOf(gjennomforingId),
			veileder = emptyList()
		)
	}
}
