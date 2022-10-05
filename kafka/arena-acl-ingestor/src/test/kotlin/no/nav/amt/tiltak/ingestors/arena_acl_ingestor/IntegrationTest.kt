package no.nav.amt.tiltak.ingestors.arena_acl_ingestor

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.arrangor.ArrangorRepository
import no.nav.amt.tiltak.arrangor.ArrangorServiceImpl
import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetsregisterClient
import no.nav.amt.tiltak.clients.amt_enhetsregister.Virksomhet
import no.nav.amt.tiltak.clients.norg.NorgClient
import no.nav.amt.tiltak.clients.veilarbarena.VeilarbarenaClient
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.kafka.ArenaAclIngestor
import no.nav.amt.tiltak.core.port.*
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.deltaker.service.DeltakerServiceImpl
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor.DeltakerProcessor
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor.GjennomforingProcessor
import no.nav.amt.tiltak.nav_enhet.NavEnhetRepository
import no.nav.amt.tiltak.nav_enhet.NavEnhetServiceImpl
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.tiltak.dbo.GjennomforingDbo
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import no.nav.amt.tiltak.tiltak.repositories.HentKoordinatorerForGjennomforingQuery
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import no.nav.amt.tiltak.tiltak.services.BrukerService
import no.nav.amt.tiltak.tiltak.services.GjennomforingServiceImpl
import no.nav.amt.tiltak.tiltak.services.TiltakServiceImpl
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntegrationTest {
	private lateinit var tiltakRepository: TiltakRepository
	private lateinit var tiltakService: TiltakService
	private lateinit var gjennomforingRepository: GjennomforingRepository
	private lateinit var gjennomforingService: GjennomforingService
	private lateinit var deltakerRepository: DeltakerRepository
	private lateinit var deltakerStatusRepository: DeltakerStatusRepository
	private lateinit var brukerService: BrukerService
	private lateinit var deltakerService: DeltakerService
	private lateinit var personService: PersonService
	private lateinit var norgClient: NorgClient

	private lateinit var brukerRepository: BrukerRepository
	private lateinit var navEnhetRepository: NavEnhetRepository
	private lateinit var veilarbarenaClient: VeilarbarenaClient
	private lateinit var navEnhetService: NavEnhetService

	private lateinit var gjennomforingProcessor: GjennomforingProcessor
	private lateinit var deltakerProcessor: DeltakerProcessor

	private lateinit var enhetsregisterClient: EnhetsregisterClient
	private lateinit var arrangorRepository: ArrangorRepository
	private lateinit var arrangorService: ArrangorService

	private lateinit var ingestor: ArenaAclIngestor


	private lateinit var jdbcTemplate: NamedParameterJdbcTemplate
	private val datasource = SingletonPostgresContainer.getDataSource()
	private val gjennomforingId = UUID.randomUUID()
	val personIdent = "32948329048"
	val virksomhetsnr = "1235543432"
	val brukerEpost = "epost"
	val brukerTelefon = "telefon"
	val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)

	@BeforeAll
	fun beforeAll() {
		val transactionTemplate = TransactionTemplate(DataSourceTransactionManager(datasource))

		jdbcTemplate = NamedParameterJdbcTemplate(datasource)
		tiltakRepository = TiltakRepository(jdbcTemplate)
		gjennomforingRepository = GjennomforingRepository(jdbcTemplate)
		deltakerRepository = DeltakerRepository(jdbcTemplate)
		navEnhetRepository = NavEnhetRepository(jdbcTemplate)
		deltakerStatusRepository = DeltakerStatusRepository(jdbcTemplate)
		arrangorRepository = ArrangorRepository(jdbcTemplate)
		brukerRepository = BrukerRepository(jdbcTemplate)

		norgClient = mockk()
		veilarbarenaClient = mockk()
		personService = mockk()
		enhetsregisterClient = mockk()

		navEnhetService = NavEnhetServiceImpl(norgClient, navEnhetRepository, veilarbarenaClient)
		tiltakService = TiltakServiceImpl(tiltakRepository)
		brukerService = BrukerService(brukerRepository, personService, mockk(), navEnhetService)
		deltakerService = DeltakerServiceImpl(deltakerRepository, deltakerStatusRepository, brukerService, transactionTemplate)
		arrangorService = ArrangorServiceImpl(enhetsregisterClient, arrangorRepository)
		gjennomforingService = GjennomforingServiceImpl(gjennomforingRepository, tiltakService, deltakerService, arrangorService, transactionTemplate, HentKoordinatorerForGjennomforingQuery(jdbcTemplate))

		deltakerProcessor = DeltakerProcessor(gjennomforingService, deltakerService, personService, transactionTemplate)
		gjennomforingProcessor = GjennomforingProcessor(arrangorService, gjennomforingService, tiltakService, navEnhetService)

		ingestor = ArenaAclIngestorImpl(deltakerProcessor, gjennomforingProcessor)

		every { enhetsregisterClient.hentVirksomhet(virksomhetsnr) } returns virksomhet

		every { personService.hentPersonKontaktinformasjon(personIdent) } returns Kontaktinformasjon(brukerEpost, brukerTelefon)
		every { veilarbarenaClient.hentBrukerOppfolgingsenhetId(personIdent) } returns null
		every { personService.hentTildeltVeilederNavIdent(personIdent) } returns null
		every { personService.hentPerson(personIdent) } returns person

		DbTestDataUtils.cleanDatabase(datasource)

		val testDataRepository = TestDataRepository(jdbcTemplate)

		testDataRepository.insertNavEnhet(NAV_ENHET_1)
	}

	@Test
	fun `ingestKafkaMessageValue() - Skal ingeste gyldig gjennomføring`() {

		ingestor.ingestKafkaRecord(gjennomforingJson)

		val inserted = gjennomforingRepository.get(toInsertGjennomforing.id)

		inserted shouldNotBe null
		val expected = toInsertGjennomforing.copy(arrangorId = inserted!!.arrangorId, createdAt = inserted.createdAt, modifiedAt = inserted.modifiedAt)
		inserted shouldBe expected

	}

	@Test
	fun `ingestKafkaMessageValue() - Skal ingeste gyldig deltaker`() {

		ingestor.ingestKafkaRecord(gjennomforingJson)
		ingestor.ingestKafkaRecord(deltakerJson)

		val inserted = deltakerService.hentDeltaker(deltakerToInsert.id)

		inserted shouldNotBe null

		val uuid = UUID.randomUUID()
		val expected = deltakerToInsert.copy(
			status = deltakerToInsert.status.copy(id = uuid, opprettetDato = now)
		)
		val actual = inserted!!.copy(status = inserted.status.copy(id = uuid, opprettetDato = now))
		actual shouldBe expected

	}

	@Test
	fun `ingestKafkaMessageValue() - Skal ingeste gyldig deltaker oppdatering`() {
		ingestor.ingestKafkaRecord(gjennomforingJson)
		ingestor.ingestKafkaRecord(deltakerJson)
		ingestor.ingestKafkaRecord(deltakerOppdatertJson)

		val inserted = deltakerService.hentDeltaker(deltakerOppdatert.id)

		inserted shouldNotBe null
		//inserted!!.bruker shouldNotBe null

		val uuid = UUID.randomUUID()
		val expected = deltakerOppdatert.copy(
			status = deltakerOppdatert.status.copy(id = uuid, opprettetDato = now)
		)
		val actual = inserted!!.copy(status = inserted.status.copy(id = uuid, opprettetDato = now))
		actual.status shouldBe expected.status
		actual shouldBe expected

	}

	val toInsertGjennomforing = GjennomforingDbo(
		id =  gjennomforingId,
		arrangorId =  UUID.randomUUID(),
		tiltakId =  UUID.randomUUID(),
		navn =  "Tiltak hos muligheter AS",
		status =  Gjennomforing.Status.GJENNOMFORES,
		startDato =  LocalDate.now().minusDays(3),
		sluttDato =  LocalDate.now().minusDays(1),
		registrertDato =  now.minusDays(5),
		fremmoteDato =  null,
		navEnhetId = NAV_ENHET_1.id,
		createdAt =  now,
		modifiedAt =  now,
		lopenr = 123,
		opprettetAar = 2020
	)

	val gjennomforingJson = """
			{
			  "transactionId": "b3b46fc2-ad90-4dbb-abfb-2b767318258b",
			  "type": "GJENNOMFORING",
			  "timestamp": "$now",
			  "operation": "CREATED",
			  "payload": {
			    "id": "${toInsertGjennomforing.id}",
				"tiltak": {
					"id": "${toInsertGjennomforing.tiltakId}",
					"kode": "INDOPPFAG",
					"navn": "Oppfølging"
				},
				"virksomhetsnummer": "$virksomhetsnr",
				"navn": "${toInsertGjennomforing.navn}",
				"status": "${toInsertGjennomforing.status.name}",
				"startDato": "${toInsertGjennomforing.startDato}",
				"sluttDato": "${toInsertGjennomforing.sluttDato}",
				"registrertDato": "${toInsertGjennomforing.registrertDato}",
				"opprettetAar": ${toInsertGjennomforing.opprettetAar},
				"lopenr": ${toInsertGjennomforing.lopenr},
				"ansvarligNavEnhetId": "${NAV_ENHET_1.enhetId}"
			  }
			}
		""".trimIndent()

	val virksomhet = Virksomhet(
		navn = "En virksomhet",
		organisasjonsnummer = "123",
		overordnetEnhetOrganisasjonsnummer = null,
		overordnetEnhetNavn = null
	)

	val person = Person(
		fornavn = "Fornavn",
		mellomnavn = null,
		etternavn = "Etternavn",
		telefonnummer = brukerTelefon,
		diskresjonskode = null
	)

	val deltakerToInsert = Deltaker(
		id = UUID.randomUUID(),
		fornavn = person.fornavn,
		etternavn = person.etternavn,
		fodselsnummer = personIdent,
		navEnhetId = null,
		navVeilederId = null,
		telefonnummer = brukerTelefon,
		epost = brukerEpost,
		startDato = null,
		sluttDato = null,
		status = DeltakerStatus(
				id = UUID.randomUUID(),
				type =  Deltaker.Status.VENTER_PA_OPPSTART,
				gyldigFra =  now.minusHours(1),
				opprettetDato = now,
				aktiv = true
		),
		registrertDato = now,
		dagerPerUke = 5,
		prosentStilling = 100F,
		gjennomforingId = gjennomforingId
	)

	val deltakerOppdatert = Deltaker(
		id = deltakerToInsert.id,
		fornavn = person.fornavn,
		etternavn = person.etternavn,
		fodselsnummer = personIdent,
		navEnhetId = null,
		navVeilederId = null,
		telefonnummer = brukerTelefon,
		epost = brukerEpost,
		startDato = LocalDate.now().minusDays(1),
		sluttDato = LocalDate.now().plusDays(1),
		status = DeltakerStatus(id= UUID.randomUUID(), Deltaker.Status.DELTAR, now, now, true),
		registrertDato = now,
		dagerPerUke = 3,
		prosentStilling = 50F,
		gjennomforingId = gjennomforingId
	)

	val deltakerOppdatertJson = """
			{
			  "transactionId": "0a99b548-c831-47c4-87f6-760e9800b29d",
			  "type": "DELTAKER",
			  "timestamp": "2022-01-10T11:46:44.799Z",
			  "operation": "CREATED",
			  "payload": {
			    "id": "${deltakerOppdatert.id}",
			    "gjennomforingId": "${gjennomforingId}",
			    "personIdent": "$personIdent",
			    "status": "${deltakerOppdatert.status.type.name}",
				"startDato": "${deltakerOppdatert.startDato}",
				"sluttDato": "${deltakerOppdatert.sluttDato}",
			    "dagerPerUke": "${deltakerOppdatert.dagerPerUke}",
			    "prosentDeltid": ${deltakerOppdatert.prosentStilling},
			    "registrertDato": "${deltakerOppdatert.registrertDato}",
				"statusEndretDato": "${deltakerOppdatert.status.gyldigFra}"
			  }
			}
		""".trimIndent()

	val deltakerJson = """
			{
			  "transactionId": "0a99b548-c831-47c4-87f6-760e9800b29c",
			  "type": "DELTAKER",
			  "timestamp": "2022-01-10T11:46:44.799Z",
			  "operation": "CREATED",
			  "payload": {
			    "id": "${deltakerToInsert.id}",
			    "gjennomforingId": "${gjennomforingId}",
			    "personIdent": "$personIdent",
			    "status": "${deltakerToInsert.status.type.name}",
			    "dagerPerUke": ${deltakerToInsert.dagerPerUke},
			    "prosentDeltid": ${deltakerToInsert.prosentStilling},
			    "registrertDato": "${deltakerToInsert.registrertDato}",
				"statusEndretDato": "${deltakerToInsert.status.gyldigFra}"
			  }
			}
		""".trimIndent()

}
