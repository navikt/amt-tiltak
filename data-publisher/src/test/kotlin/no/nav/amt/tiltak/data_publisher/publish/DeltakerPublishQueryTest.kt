package no.nav.amt.tiltak.data_publisher.publish

import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.lib.models.arrangor.melding.Vurderingstype
import no.nav.amt.lib.models.deltaker.DeltakerHistorikk
import no.nav.amt.tiltak.core.port.UnleashService
import no.nav.amt.tiltak.data_publisher.DatabaseTestDataHandler
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate

class DeltakerPublishQueryTest : FunSpec({
	val dataSource = SingletonPostgresContainer.getDataSource()
	val template = NamedParameterJdbcTemplate(dataSource)
	val unleashService = mockk<UnleashService>()

	val query = DeltakerPublishQuery(template, unleashService)
	val db = DatabaseTestDataHandler(template)

	beforeEach {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	afterEach {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	test("get") {
		every { unleashService.erKometMasterForTiltakstype(any()) } returns false
		val input = db.createDeltaker()

		when (val data = query.get(input.id, null)) {
			is DeltakerPublishQuery.Result.OK -> data.result.id shouldBe input.id
			else -> fail("Should be ok, was $data")
		}
	}

	test("get - deltaker som komet er master for, uten vurdering, skal ikke publisere") {
		every { unleashService.erKometMasterForTiltakstype(any()) } returns true
		val input = db.createDeltaker()

		when (val data = query.get(input.id, null)) {
			is DeltakerPublishQuery.Result.DontPublish -> {}
			else -> fail("Should be ok, was $data")
		}
	}

	test("get - deltaker som arena er master for skal opprettes med importertFraArena data") {
		every { unleashService.erKometMasterForTiltakstype(any()) } returns false
		val deltakerInput = db.createDeltaker()

		when (val data = query.get(deltakerInput.id, null)) {
			is DeltakerPublishQuery.Result.OK -> {
				data.result.historikk!!.size shouldBe 1
				val importertFraArenaElement = (data.result.historikk[0] as DeltakerHistorikk.ImportertFraArena).importertFraArena
				importertFraArenaElement.deltakerId shouldBe deltakerInput.id
				importertFraArenaElement.importertDato.toLocalDate() shouldBe LocalDate.now()
				importertFraArenaElement.deltakerVedImport.deltakerId shouldBe deltakerInput.id
				importertFraArenaElement.deltakerVedImport.startdato shouldBe deltakerInput.startDato
				importertFraArenaElement.deltakerVedImport.sluttdato shouldBe deltakerInput.sluttDato

			}
			else -> fail("Should be ok, was $data")
		}
	}
	test("get - deltaker som arena er master for skal opprettes med vurderinger data") {
		every { unleashService.erKometMasterForTiltakstype(any()) } returns false
		val deltakerInput = db.createDeltaker()
		val arrangorAnsatt = db.createArrangorAnsatt()
		val vurdering = db.createVurdering(deltakerId = deltakerInput.id, opprettetAvArrangorAnsattId = arrangorAnsatt.id)


		when (val data = query.get(deltakerInput.id, null)) {
			is DeltakerPublishQuery.Result.OK -> {
				data.result.historikk!!.size shouldBe 2
				val importertFraArenaElement = (data.result.historikk.first { it is DeltakerHistorikk.ImportertFraArena } as DeltakerHistorikk.ImportertFraArena).importertFraArena
				importertFraArenaElement.deltakerId shouldBe deltakerInput.id
				importertFraArenaElement.importertDato.toLocalDate() shouldBe LocalDate.now()
				importertFraArenaElement.deltakerVedImport.deltakerId shouldBe deltakerInput.id
				importertFraArenaElement.deltakerVedImport.startdato shouldBe deltakerInput.startDato
				importertFraArenaElement.deltakerVedImport.sluttdato shouldBe deltakerInput.sluttDato

				val vurderingElement = (data.result.historikk.first { it is DeltakerHistorikk.VurderingFraArrangor } as DeltakerHistorikk.VurderingFraArrangor).data
				vurderingElement.deltakerId shouldBe deltakerInput.id
				vurderingElement.begrunnelse shouldBe vurdering.begrunnelse
				vurderingElement.vurderingstype shouldBe Vurderingstype.valueOf(vurdering.vurderingstype.name)

			}
			else -> fail("Should be ok, was $data")
		}
	}
})
