package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeEqualTo
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.RepositoryTestBase
import org.springframework.boot.test.context.SpringBootTest
import java.time.ZonedDateTime
import java.util.UUID

@SpringBootTest(classes = [MineDeltakerlisterRepository::class])
class MineDeltakerlisterRepositoryTest(
	sut: MineDeltakerlisterRepository
) : RepositoryTestBase({

	Given("MineDeltakerlisterRepository") {

		// kopiert fra ArrangorAnsattGjennomforingTilgangRepositoryTest#
		When("opprettTilgang skal opprette tilgang") {
			testRepository.insertNavEnhet(NAV_ENHET_1)
			testRepository.insertArrangor(ARRANGOR_1)
			testRepository.insertArrangorAnsatt(ARRANGOR_ANSATT_1)
			testRepository.insertTiltak(TILTAK_1)
			testRepository.insertGjennomforing(GJENNOMFORING_1)

			val tilgangId = UUID.randomUUID()
			val gyldigFra = ZonedDateTime.now()
			val gyldigTil = ZonedDateTime.now().plusHours(1)

			sut.leggTil(
				id = tilgangId,
				arrangorAnsattId = ARRANGOR_ANSATT_1.id,
				gjennomforingId = GJENNOMFORING_1.id,
				gyldigFra = gyldigFra,
				gyldigTil = gyldigTil
			)

			val tilgang = sut.get(tilgangId)

			Then("tilgang skal være som forventet") {
				assertSoftly(tilgang) {
					id shouldBe tilgangId
					gjennomforingId shouldBe GJENNOMFORING_1.id
					gyldigFra shouldBeEqualTo gyldigFra
					gyldigTil shouldBeEqualTo gyldigTil
					ansattId shouldBe ARRANGOR_ANSATT_1.id
				}
			}
		}
	}
})
