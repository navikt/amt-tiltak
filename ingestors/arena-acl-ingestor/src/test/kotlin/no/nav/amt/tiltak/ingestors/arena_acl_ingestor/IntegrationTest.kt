package no.nav.amt.tiltak.ingestors.arena_acl_ingestor

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.arrangor.ArrangorRepository
import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetsregisterClient
import no.nav.amt.tiltak.clients.amt_enhetsregister.Virksomhet
import no.nav.amt.tiltak.core.domain.tiltak.*
import no.nav.amt.tiltak.core.port.*
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.deltaker.repositories.NavKontorRepository
import no.nav.amt.tiltak.deltaker.service.DeltakerServiceImpl
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor.DeltakerProcessor
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor.GjennomforingProcessor
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.tiltak.dbo.GjennomforingDbo
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import no.nav.amt.tiltak.tiltak.services.BrukerServiceImpl
import no.nav.amt.tiltak.tiltak.services.GjennomforingServiceImpl
import no.nav.amt.tiltak.tiltak.services.TiltakServiceImpl
import org.junit.Ignore
import org.junit.jupiter.api.*
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Ignore
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

	private lateinit var brukerRepository: BrukerRepository
	private lateinit var navKontorRepository: NavKontorRepository
	private lateinit var navKontorService: NavKontorService

	private lateinit var gjennomforingProcessor: GjennomforingProcessor;
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
	val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)

	@BeforeAll
	fun beforeAll() {
		jdbcTemplate = NamedParameterJdbcTemplate(datasource)
		tiltakRepository = TiltakRepository(jdbcTemplate)
		tiltakService = TiltakServiceImpl(tiltakRepository)
		gjennomforingRepository = GjennomforingRepository(jdbcTemplate)
		gjennomforingService = GjennomforingServiceImpl(gjennomforingRepository, tiltakService)
		deltakerRepository = DeltakerRepository(jdbcTemplate)
		deltakerStatusRepository = DeltakerStatusRepository(jdbcTemplate)
		navKontorRepository = NavKontorRepository(jdbcTemplate)
		navKontorService = mockk()
		personService = mockk()
		brukerRepository = BrukerRepository(jdbcTemplate)
		brukerService = BrukerServiceImpl(brukerRepository, navKontorRepository, navKontorService, personService, mockk())
		deltakerService = DeltakerServiceImpl(deltakerRepository, deltakerStatusRepository, brukerService, TransactionTemplate(DataSourceTransactionManager(datasource)))
		deltakerProcessor = DeltakerProcessor(gjennomforingService, deltakerService, personService)
		enhetsregisterClient = mockk()
		arrangorRepository = ArrangorRepository(jdbcTemplate)
		arrangorService = no.nav.amt.tiltak.arrangor.ArrangorService(mockk(), enhetsregisterClient, arrangorRepository)

		gjennomforingProcessor = GjennomforingProcessor(arrangorService, gjennomforingService, tiltakService)
		ingestor = ArenaAclIngestorImpl(deltakerProcessor, gjennomforingProcessor)

		every { enhetsregisterClient.hentVirksomhet(virksomhetsnr) } returns virksomhet

		every { personService.hentPersonKontaktinformasjon(personIdent) } returns Kontaktinformasjon("epost", "telefon" )
		every { navKontorService.hentNavKontorForBruker(personIdent) } returns null
		every { personService.hentTildeltVeileder(personIdent) } returns null
		every { personService.hentPerson(personIdent) } returns person
	}

	@Test
	fun `ingestKafkaMessageValue() - Skal ingeste gyldig gjennomføring`() {

		ingestor.ingestKafkaMessageValue(gjennomforingJson)

		val inserted = gjennomforingRepository.get(toInsertGjennomforing.id)

		inserted shouldNotBe null
		val expected = toInsertGjennomforing.copy(arrangorId = inserted!!.arrangorId, createdAt = inserted.createdAt, modifiedAt = inserted.modifiedAt)
		inserted shouldBe expected

	}

	@Test
	fun `ingestKafkaMessageValue() - Skal ingeste gyldig deltaker`() {

		ingestor.ingestKafkaMessageValue(gjennomforingJson)
		ingestor.ingestKafkaMessageValue(deltakerJson)

		val inserted = deltakerService.hentDeltaker(deltakerToInsert.id)

		inserted shouldNotBe null
		inserted.bruker shouldNotBe null

		val uuid = UUID.randomUUID()
		val expected = deltakerToInsert.copy(
			bruker = deltakerToInsert.bruker?.copy(id= inserted.bruker!!.id),
			statuser = DeltakerStatuser(deltakerToInsert.statuser.statuser.map { it.copy(id = uuid) })
		)
		val actual = inserted.copy(statuser = DeltakerStatuser(inserted.statuser.statuser.map { it.copy(id = uuid) }))
		actual shouldBe expected

	}

	@Test
	fun `ingestKafkaMessageValue() - Skal ingeste gyldig deltaker oppdatering`() {
		ingestor.ingestKafkaMessageValue(gjennomforingJson)
		ingestor.ingestKafkaMessageValue(deltakerJson)
		ingestor.ingestKafkaMessageValue(deltakerOppdatertJson)

		val inserted = deltakerService.hentDeltaker(deltakerOppdatert.id)

		inserted shouldNotBe null
		inserted.bruker shouldNotBe null

		val uuid = UUID.randomUUID()
		val expected = deltakerOppdatert.copy(
			bruker = deltakerOppdatert.bruker?.copy(id= inserted.bruker!!.id),
			statuser = DeltakerStatuser(deltakerOppdatert.statuser.statuser.map { it.copy(id = uuid) })
		)
		val actual = inserted.copy(statuser = DeltakerStatuser(inserted.statuser.statuser.map { it.copy(id = uuid) }))
		actual.statuser shouldBe expected.statuser
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
		createdAt =  now,
		modifiedAt =  now
	)

	val gjennomforingJson = """
			{
			  "transactionId": "b3b46fc2-ad90-4dbb-abfb-2b767318258b",
			  "type": "GJENNOMFORING",
			  "timestamp": "${now}",
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
				"registrertDato": "${toInsertGjennomforing.registrertDato}"
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
		mellomnavn = "",
		etternavn = "Etternavn",
		telefonnummer = "12345678",
		diskresjonskode = null
	)

	val deltakerToInsert = Deltaker(
		id = UUID.randomUUID(),
		bruker = Bruker(
			id = UUID.randomUUID(),
			fornavn = person.fornavn,
			etternavn = person.etternavn,
			fodselsnummer = personIdent
		),
		startDato = null,
		sluttDato = null,
		statuser = DeltakerStatuser(listOf(
			DeltakerStatus(
				id = UUID.randomUUID(),
				status =  Deltaker.Status.VENTER_PA_OPPSTART,
				endretDato =  now.minusHours(1),
				aktiv = true
			))),
		registrertDato = now,
		dagerPerUke = 5,
		prosentStilling = 100F,
	)

	val deltakerOppdatert = Deltaker(
		id = deltakerToInsert.id,
		bruker = deltakerToInsert.bruker,
		startDato = LocalDate.now().minusDays(1),
		sluttDato = LocalDate.now().plusDays(1),
		statuser = deltakerToInsert.statuser.medNy(Deltaker.Status.DELTAR, now),
		registrertDato = now,
		dagerPerUke = 3,
		prosentStilling = 50F,
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
			    "status": "${deltakerOppdatert.status}",
				"startDato": "${deltakerOppdatert.startDato}",
				"sluttDato": "${deltakerOppdatert.sluttDato}",
			    "dagerPerUke": "${deltakerOppdatert.dagerPerUke}",
			    "prosentDeltid": ${deltakerOppdatert.prosentStilling},
			    "registrertDato": "${deltakerOppdatert.registrertDato}",
				"statusEndretDato": "${deltakerOppdatert.statuser.statuser.last().endretDato}"
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
			    "status": "VENTER_PA_OPPSTART",
			    "dagerPerUke": ${deltakerToInsert.dagerPerUke},
			    "prosentDeltid": ${deltakerToInsert.prosentStilling},
			    "registrertDato": "${deltakerToInsert.registrertDato}",
				"statusEndretDato": "${deltakerToInsert.statuser.statuser.last().endretDato}"
			  }
			}
		""".trimIndent()

}
