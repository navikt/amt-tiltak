package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.tiltak.deltaker.DeltakerService
import no.nav.amt.tiltak.tiltak.repositories.TiltakInstansRepository
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import no.nav.amt.tiltak.tiltak.services.TiltakServiceImpl
import no.nav.amt.tiltak.tiltak.testutils.DatabaseTestUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.web.server.ResponseStatusException
import java.util.*

class TiltakInstansControllerIntegrationTest {
	private lateinit var namedJdbcTemplate: NamedParameterJdbcTemplate
	private lateinit var tiltakRepository: TiltakRepository
	private lateinit var tiltakInstansRepository: TiltakInstansRepository
	private lateinit var tiltakInstansService: TiltakService
	private lateinit var deltakerService: DeltakerService
	private lateinit var controller: TiltakInstansController

	@BeforeEach
	fun before () {
		namedJdbcTemplate = DatabaseTestUtils.getDatabase()
		tiltakInstansRepository = TiltakInstansRepository(namedJdbcTemplate)
		tiltakRepository = TiltakRepository(namedJdbcTemplate)
		deltakerService = Mockito.mock(DeltakerService::class.java)
		tiltakInstansService = TiltakServiceImpl(tiltakRepository, tiltakInstansRepository, deltakerService)
		controller = TiltakInstansController(tiltakInstansService)
	}

	@Test
	fun `hentTiltakInstans - tiltaksgjennomføring finnes ikke - skal returnere NOT FOUND`() {
		val id = UUID.randomUUID().toString()
		val exception = assertThrows(ResponseStatusException::class.java) {
			controller.hentTiltakInstans(id)
		}
		assertEquals("404 NOT_FOUND", exception.status.toString())
	}

	@Test
	fun `hentTiltakInstans - tiltak finnes ikke - skal returnere INTERNAL SERVER ERROR`() {
		val tiltakNavn ="Gruppe amo"
		val tlId = insertTiltaksleverandor()
		val tiltak = tiltakRepository.insert("22", tiltakNavn, "kode")
		val instans = tlId.let {
			tiltakInstansRepository.insert(
				arenaId = 2,
				tiltakId = tiltak.externalId,
				tiltaksleverandorId = it,
				navn = "Test",
				status = null,
				oppstartDato = null,
				sluttDato = null,
				registrertDato = null,
				fremmoteDato = null
			)
		}

		val resultat = controller.hentTiltakInstans(instans.externalId.toString())

		assertEquals(instans.externalId, resultat.id)
		assertEquals(tiltakNavn, resultat.tiltak.tiltaksnavn)
	}

	@Test
	fun `hentTiltakInstans - tiltak finnes - skal returnere tiltak`() {
		val leverandorId = insertTiltaksleverandor()
		val tiltak = tiltakRepository.insert("4", "Gruppe AMO", "GRUPPEAMO")
		val tiltakInstans = tiltakInstansRepository.insert(
			4,
			tiltakId = tiltak.externalId,
			tiltaksleverandorId = leverandorId,
			navn = "Kaffekurs",
			status = TiltakInstans.Status.GJENNOMFORES,
			oppstartDato = null,
			sluttDato = null,
			registrertDato = null,
			fremmoteDato = null
		)
		val resultat = controller.hentTiltakInstans(tiltakInstans.externalId.toString())
		assertEquals(tiltakInstans.externalId, resultat.id)
		assertEquals(tiltakInstans.navn, resultat.navn)


	}

	private fun insertTiltaksleverandor() : UUID {
		val id = "0dc9ccec-fd1e-4c4e-b91a-c23e6d89c18e"
		val insert = """
			INSERT INTO tiltaksleverandor(id, external_id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer,navn)
			VALUES (1, '$id', '12345678', 'Orgnavn1', '87654321', 'Virksomhetsnavn1');
		""".trimIndent()
		namedJdbcTemplate.jdbcTemplate.update(insert)

		return UUID.fromString(id)

	}

}
