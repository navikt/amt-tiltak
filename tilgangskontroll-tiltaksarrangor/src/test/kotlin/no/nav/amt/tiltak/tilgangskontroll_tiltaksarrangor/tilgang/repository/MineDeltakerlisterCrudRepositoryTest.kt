package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang.repository

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeCloseTo
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_TILGANG_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_2
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.RepositoryTestBase
import org.springframework.boot.test.context.SpringBootTest
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.UUID

@SpringBootTest(classes = [MineDeltakerlisterCrudRepository::class])
class MineDeltakerlisterCrudRepositoryTest(
	sut: MineDeltakerlisterCrudRepository
) : RepositoryTestBase({

	Given("MineDeltakerlisterRepository") {

		// kopiert fra ArrangorAnsattGjennomforingTilgangRepositoryTest#
		When("opprettTilgang skal opprette tilgang") {
			val tilgangId = UUID.randomUUID()
			val gyldigFra = OffsetDateTime.now()
			val gyldigTil = OffsetDateTime.now().plusHours(1)

			with(testRepository) {
				insertNavEnhet(NAV_ENHET_1)
				insertArrangor(ARRANGOR_1)
				insertArrangorAnsatt(ARRANGOR_ANSATT_1)
				insertTiltak(TILTAK_1)
				insertGjennomforing(GJENNOMFORING_1)
			}

			sut.insert(
				ArrangorAnsattGjennomforingTilgangEntity(
					id = tilgangId,
					ansattId = ARRANGOR_ANSATT_1.id,
					gjennomforingId = GJENNOMFORING_1.id,
					gyldigFra = gyldigFra,
					gyldigTil = gyldigTil
				)
			)

			val optionalTilgang = sut.findById(tilgangId)

			Then("tilgang skal være som forventet") {
				optionalTilgang.isPresent shouldBe true

				assertSoftly(optionalTilgang.get()) {
					id shouldBe tilgangId
					gjennomforingId shouldBe GJENNOMFORING_1.id
					gyldigFra shouldBe gyldigFra
					gyldigTil shouldBe gyldigTil
					ansattId shouldBe ARRANGOR_ANSATT_1.id
				}
			}
		}

		When("hentAktiveGjennomforingTilgangerForAnsatt - skal returnere tilganger som ikke er stoppet") {
			val ansattId = ARRANGOR_ANSATT_1.id
			val gjennomforing1Id = GJENNOMFORING_1.id
			val gjennomforing2Id = GJENNOMFORING_2.id

			val tilgangId1 = UUID.randomUUID()
			val tilgangId2 = UUID.randomUUID()

			with(testRepository) {
				insertNavEnhet(NAV_ENHET_1)
				insertNavEnhet(NAV_ENHET_2)
				insertArrangor(ARRANGOR_1)
				insertArrangor(ARRANGOR_2)
				insertArrangorAnsatt(ARRANGOR_ANSATT_1)
				insertTiltak(TILTAK_1)
				insertGjennomforing(GJENNOMFORING_1)
				insertGjennomforing(GJENNOMFORING_2)

				insertMineDeltakerlister(
					GJENNOMFORING_TILGANG_1.copy(
						id = tilgangId1,
						ansattId = ansattId,
						gjennomforingId = gjennomforing1Id
					)
				)

				insertMineDeltakerlister(
					GJENNOMFORING_TILGANG_1.copy(
						id = tilgangId2,
						ansattId = ansattId,
						gjennomforingId = gjennomforing2Id,
						gyldigTil = ZonedDateTime.now().minusMinutes(10),
						gyldigFra = ZonedDateTime.now().minusMinutes(100)
					)
				)
			}

			val tilganger: List<ArrangorAnsattGjennomforingTilgangEntity> = sut.hent(ansattId)

			tilganger.size shouldBe 1
			tilganger.any { it.gjennomforingId == gjennomforing1Id } shouldBe true
			tilganger.any { it.gjennomforingId == gjennomforing2Id } shouldBe false
		}

		When("fjernTilgang - skal sette gyldig_til") {
			val ansattId = ARRANGOR_ANSATT_1.id
			val gjennomforingId = GJENNOMFORING_1.id
			val tilgangId = UUID.randomUUID()

			with(testRepository) {
				insertNavEnhet(NAV_ENHET_1)
				insertNavEnhet(NAV_ENHET_2)
				insertArrangor(ARRANGOR_1)
				insertArrangor(ARRANGOR_2)
				insertArrangorAnsatt(ARRANGOR_ANSATT_1)
				insertArrangorAnsatt(ARRANGOR_ANSATT_2)
				insertTiltak(TILTAK_1)
				insertGjennomforing(GJENNOMFORING_1)
				insertGjennomforing(GJENNOMFORING_2)

				insertMineDeltakerlister(
					GJENNOMFORING_TILGANG_1.copy(
						id = tilgangId,
						ansattId = ansattId,
						gjennomforingId = gjennomforingId
					)
				)
			}

			sut.fjern(ansattId, gjennomforingId)

			val optionalTilgang = sut.findById(tilgangId)

			Then("gyldigTil should be as expected") {
				optionalTilgang.isPresent shouldBe true
				val tilgang = optionalTilgang.get()
				tilgang.gyldigTil.toZonedDateTime() shouldBeCloseTo ZonedDateTime.now()
			}
		}

		When("getAntallGjennomforingerPerAnsatt") {
			val tilgangId = UUID.randomUUID()

			with(testRepository) {
				insertNavEnhet(NAV_ENHET_1)
				insertArrangor(ARRANGOR_1)
				insertArrangorAnsatt(ARRANGOR_ANSATT_1)
				insertTiltak(TILTAK_1)
				insertGjennomforing(GJENNOMFORING_1)
			}

			sut.insert(
				ArrangorAnsattGjennomforingTilgangEntity(
					id = tilgangId,
					ansattId = ARRANGOR_ANSATT_1.id,
					gjennomforingId = GJENNOMFORING_1.id,
					gyldigFra = OffsetDateTime.now().minusDays(1),
					gyldigTil = OffsetDateTime.now().plusDays(1)
				)
			)

			val gjennomforingCounts = sut.hentAntallPerAnsatt()

			Then("gjennomforingCounts should be as expected") {
				assertSoftly(gjennomforingCounts) {
					size shouldBe 1

					assertSoftly(first()) {
						ansattId shouldBe ARRANGOR_ANSATT_1.id
						gjennomforinger shouldBe 1
					}
				}
			}
		}
	}
})
