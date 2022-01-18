package no.nav.amt.tiltak.core.domain.tiltak

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker.Status.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class DeltakerStatuserTest {

	val now: LocalDate = LocalDate.now()

	@Test
	fun `current - inneholder gjeldende status`() {
		val statuser = DeltakerStatuser(
			listOf(
				DeltakerStatus.nyInaktiv(VENTER_PA_OPPSTART, now.minusWeeks(1)),
				DeltakerStatus.nyAktiv(DELTAR, now)
			)
		)

		assertEquals(statuser.current.status, DELTAR)
		assertEquals(statuser.current.endretDato, now)
	}

	@Test
	fun `kun en status kan vaere aktiv`() {
		assertThrows<IllegalArgumentException> {
			DeltakerStatuser(
				listOf(
					DeltakerStatus.nyAktiv(VENTER_PA_OPPSTART, now.minusWeeks(1)),
					DeltakerStatus.nyAktiv(DELTAR, now)
				)
			)
		}

	}

	@Test
	fun `medNy - legg til status - returnerer ny DeltagerStatuser med ny aktiv status`() {
		val venterPaOppstartInaktiv = DeltakerStatus.nyInaktiv(VENTER_PA_OPPSTART, now.minusWeeks(2))
		val deltarAktiv = DeltakerStatus.nyAktiv(DELTAR, now.minusWeeks(1))
		val deltarInktiv = deltarAktiv.deaktiver()
		val statuser = DeltakerStatuser(listOf(venterPaOppstartInaktiv, deltarAktiv))

		val oppdaterteStatuser = statuser.medNy(status = HAR_SLUTTET, now)


		assertTrue(
			oppdaterteStatuser.statuser.containsAll(
				listOf(
					venterPaOppstartInaktiv, deltarInktiv
				)
			)
		)
		assertTrue(oppdaterteStatuser.statuser
			.map { it.status }
			.containsAll(listOf(VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET))
		)
		assertEquals(oppdaterteStatuser.current.status, HAR_SLUTTET)
		assertEquals(oppdaterteStatuser.current.endretDato, now)
	}


	@Test
	fun `deaktiver - deaktiverer en status - sammme status, men deaktivert`() {
		val status = DeltakerStatus.nyAktiv(status = DELTAR)
		val deaktivert = status.deaktiver()
		assertTrue(status.aktiv)
		assertFalse(deaktivert.aktiv)
		assertEquals(status.id, deaktivert.id)
		assertEquals(status.status, deaktivert.status)
		assertEquals(status.endretDato, deaktivert.endretDato)
	}
}

