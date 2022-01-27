package no.nav.amt.tiltak.core.domain.tiltak

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker.Status.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime


class DeltakerTest {

	val today = LocalDate.now()
	val yesterday = today.minusDays(1)
	val tomorrow = today.plusDays(1)

	@Test
	fun `updatestatus - status oppdateres - endres til DELTAR`() {
		val deltaker = Deltaker(
			startDato = yesterday,
			sluttDato = tomorrow,
			statuser = DeltakerStatuser(listOf(DeltakerStatus.nyAktiv(
					status = VENTER_PA_OPPSTART,
					endretDato = LocalDateTime.now().minusWeeks(1)
			))),
			registrertDato = LocalDateTime.now().minusWeeks(1),
		)
		val updatedDeltaker = deltaker.update(DELTAR, yesterday, tomorrow, LocalDateTime.now())

		assertNotEquals(updatedDeltaker, deltaker)
		assertEquals(updatedDeltaker.status, DELTAR)
	}


	@Test
	fun `updatestatus - startdato endres - endres til tomorrow`() {
		val deltaker = Deltaker(
			startDato = yesterday,
			sluttDato = LocalDate.now().plusWeeks(1),
			statuser = DeltakerStatuser(listOf(DeltakerStatus(
				status = VENTER_PA_OPPSTART,
				endretDato = LocalDateTime.now().minusWeeks(1),
				aktiv = true
			))),
			registrertDato = LocalDateTime.now().minusWeeks(1),
		)
		val updatedDeltaker = deltaker.update(VENTER_PA_OPPSTART, tomorrow,  LocalDate.now().plusWeeks(1), LocalDateTime.now())

		assertNotEquals(updatedDeltaker, deltaker)
		assertEquals(updatedDeltaker.startDato, tomorrow)
	}

	@Test
	fun `updatestatus - sluttdatodato endres - endres til tomorrow`() {
		val deltaker = Deltaker(
			startDato = LocalDate.now().minusWeeks(1),
			sluttDato = yesterday,
			statuser = DeltakerStatuser(listOf(DeltakerStatus.nyAktiv(
				status = Deltaker.Status.DELTAR,
				endretDato = LocalDateTime.now().minusWeeks(1),
			))),
			registrertDato = LocalDateTime.now().minusWeeks(1),
		)
		val updatedDeltaker = deltaker.update(DELTAR, LocalDate.now().minusWeeks(1),  tomorrow, LocalDateTime.now())

		assertNotEquals(updatedDeltaker, deltaker)
		assertEquals(updatedDeltaker.sluttDato, tomorrow)
	}

	@Test
	fun `updatestatus - status, startdato og sluttdato ikke uendret - forblir uendret`() {
		val deltaker = Deltaker(
			startDato = LocalDate.now().minusWeeks(1),
			sluttDato = yesterday,
			statuser = DeltakerStatuser(listOf(DeltakerStatus.nyAktiv(
				status = DELTAR,
				endretDato = LocalDateTime.now().minusWeeks(1),
			))),
			registrertDato = LocalDateTime.now().minusWeeks(1),
		)

		val updatedDeltaker = deltaker.update(
			DELTAR,
			LocalDate.now().minusWeeks(1),
			yesterday,
			LocalDateTime.now()
		)

		assertEquals(updatedDeltaker, deltaker)
	}

	@Test
	fun `progressstatus - status er VENTER_PA_OPPSTART, startdato i dag, sluttdato null - status endres til DELTAR`() {
		val deltaker = deltaker(status = VENTER_PA_OPPSTART, startDato = today).progressStatus()
		assertEquals(DELTAR, deltaker.status)
	}

	@Test
	fun `progressstatus - status er VENTER_PA_OPPSTART, startdato i dag, sluttdato i morgen - status endres til DELTAR`() {
		val deltaker = deltaker(status = VENTER_PA_OPPSTART, startDato = today, sluttDato = tomorrow).progressStatus()
		assertEquals(DELTAR, deltaker.status)
	}

	@Test
	fun `progressstatus - status er VENTER_PA_OPPSTART, startdato i morgen - status endres ikke`() {
		val deltaker = deltaker(status = VENTER_PA_OPPSTART, startDato = tomorrow).progressStatus()
		assertEquals(VENTER_PA_OPPSTART, deltaker.status)
	}

	@Test
	fun `progressstatus - status er VENTER_PA_OPPSTART, startdato i gar, sluttdato i dag - status endres til DELTAR`() {
		val deltaker = deltaker(status = VENTER_PA_OPPSTART, startDato = yesterday, sluttDato = today).progressStatus()
		assertEquals(DELTAR, deltaker.status)
	}

	@Test
	fun `progressstatus - status er VENTER_PA_OPPSTART, mangler startdato - status blir VENTER_PA_OPPSTART`() {
		val deltaker = deltaker(status = VENTER_PA_OPPSTART, startDato = null, sluttDato = tomorrow).progressStatus()
		assertEquals(VENTER_PA_OPPSTART, deltaker.status)
	}

	@Test
	fun `progressstatus - status er VENTER_PA_OPPSTART, startdato i gar, sluttdato i gar - status endres til HAR_SLUTTET`() {
		val deltaker = deltaker(status = VENTER_PA_OPPSTART, startDato = yesterday, sluttDato = yesterday).progressStatus()
		assertEquals(HAR_SLUTTET, deltaker.status)
	}

	@Test
	fun `progressstatus - status er DELTAR, sluttdato i dag - status endres ikke`() {
		val deltaker = deltaker(status = VENTER_PA_OPPSTART, startDato = yesterday, sluttDato = today).progressStatus()
		assertEquals(DELTAR, deltaker.status)
	}

	@Test
	fun `progressstatus - status er DELTAR, sluttdato i morgen - status endres ikke`() {
		val deltaker = deltaker(status = VENTER_PA_OPPSTART, startDato = yesterday, sluttDato = tomorrow).progressStatus()
		assertEquals(DELTAR, deltaker.status)
	}

	private fun deltaker(status: Deltaker.Status, startDato: LocalDate? = null, sluttDato: LocalDate? = null):Deltaker {
		val statuser = DeltakerStatuser(listOf(DeltakerStatus.nyAktiv(status)))
		return Deltaker(statuser = statuser, startDato = startDato, sluttDato = sluttDato, registrertDato = LocalDateTime.now())

	}

}
