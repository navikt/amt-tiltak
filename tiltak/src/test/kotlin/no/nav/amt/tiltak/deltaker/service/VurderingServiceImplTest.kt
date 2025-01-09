package no.nav.amt.tiltak.deltaker.service

import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import java.util.UUID
import no.nav.amt.tiltak.core.domain.tiltak.VurderingDbo
import no.nav.amt.tiltak.core.domain.tiltak.Vurderingstype
import no.nav.amt.tiltak.deltaker.repositories.VurderingRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class VurderingServiceImplTest {
	val dataSource = SingletonPostgresContainer.getDataSource()
	private val template = NamedParameterJdbcTemplate(dataSource)
	private val vurderingRepository = VurderingRepository(template)
	private val vurderingService = VurderingServiceImpl(vurderingRepository)

	@BeforeEach
	fun before() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	fun `hentAktiveVurderingerForGjennomforing - henter aktive vurderinger pa gjennomforing`() {
		vurderingRepository.insert(VurderingDbo(
			UUID.randomUUID(),
			TestData.DELTAKER_1.id,
			Vurderingstype.OPPFYLLER_IKKE_KRAVENE,
			"Ikke nok mattefag",
			TestData.ARRANGOR_ANSATT_1.id,
			gyldigFra = LocalDateTime.now(),
			gyldigTil = null
		))
		vurderingRepository.insert(VurderingDbo(
			UUID.randomUUID(),
			TestData.DELTAKER_1.id,
			Vurderingstype.OPPFYLLER_KRAVENE,
			null,
			TestData.ARRANGOR_ANSATT_1.id,
			gyldigFra = LocalDateTime.now(),
			gyldigTil = LocalDateTime.now()
		))
		vurderingRepository.insert(VurderingDbo(
			UUID.randomUUID(),
			TestData.DELTAKER_2.id,
			Vurderingstype.OPPFYLLER_KRAVENE,
			null,
			TestData.ARRANGOR_ANSATT_1.id,
			gyldigFra = LocalDateTime.now(),
			gyldigTil = null
		))

		val vurderinger = vurderingService.hentAktiveVurderingerForGjennomforing(TestData.GJENNOMFORING_1.id)

		vurderinger.size shouldBe 2
		vurderinger.find { it.deltakerId == TestData.DELTAKER_1.id }?.vurderingstype shouldBe Vurderingstype.OPPFYLLER_IKKE_KRAVENE
		vurderinger.find { it.deltakerId == TestData.DELTAKER_2.id }?.vurderingstype shouldBe Vurderingstype.OPPFYLLER_KRAVENE
	}
}
