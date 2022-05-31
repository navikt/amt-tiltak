package no.nav.amt.tiltak.core.domain.tiltak

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker.Status.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


class DeltakerTest {

	val today = LocalDate.now()
	val yesterday = today.minusDays(1)
	val tomorrow = today.plusDays(1)

	@Test
	fun `oppdaterStatus - status oppdateres - endres til DELTAR`() {
		val deltaker = Deltaker(
			startDato = yesterday,
			sluttDato = tomorrow,
			statuser = DeltakerStatuser(listOf(DeltakerStatus.nyAktiv(
					status = VENTER_PA_OPPSTART,
					gjelderFra = LocalDateTime.now().minusWeeks(1)
			))),
			registrertDato = LocalDateTime.now().minusWeeks(1),
			gjennomforingId = UUID.randomUUID()
		)
		val oppdatertStatus = deltaker.oppdaterStatus(DELTAR, LocalDateTime.now())

		assertNotEquals(oppdatertStatus.statuser, deltaker.status)
		assertEquals(oppdatertStatus.current.status, DELTAR)

	}

	@Test
	fun `oppdaterStatus - status, startdato og sluttdato ikke uendret - forblir uendret`() {
		val deltaker = Deltaker(
			startDato = LocalDate.now().minusWeeks(1),
			sluttDato = yesterday,
			statuser = DeltakerStatuser(listOf(DeltakerStatus.nyAktiv(
				status = DELTAR,
				gjelderFra = LocalDateTime.now().minusWeeks(1),
			))),
			registrertDato = LocalDateTime.now().minusWeeks(1),
			gjennomforingId = UUID.randomUUID()
		)

		val updatedDeltaker = deltaker.oppdaterStatus(
			DELTAR,
			LocalDateTime.now()
		)

		assertEquals(updatedDeltaker, deltaker.statuser)
	}

	@Test
	fun `oppdater - status oppdateres - endres til DELTAR`() {
		val deltaker = Deltaker(
			id = UUID.randomUUID(),
			startDato = yesterday,
			sluttDato = tomorrow,
			statuser = DeltakerStatuser(listOf(DeltakerStatus.nyAktiv(
				status = VENTER_PA_OPPSTART,
				gjelderFra = LocalDateTime.now().minusWeeks(1)
			))),
			registrertDato = LocalDateTime.now().minusWeeks(1),
			gjennomforingId = UUID.randomUUID()
		)
		val nyDeltaker = Deltaker(
			id = deltaker.id,
			startDato = tomorrow,
			sluttDato = LocalDate.now().plusWeeks(1),
			statuser = DeltakerStatuser(listOf(DeltakerStatus.nyAktiv(
				status = DELTAR,
				gjelderFra = LocalDateTime.now().minusHours(3)
			))),
			registrertDato = LocalDateTime.now().minusWeeks(1),
			dagerPerUke = 3,
			prosentStilling = 100F,
			gjennomforingId = UUID.randomUUID()
		)
		val uuid = UUID.randomUUID()
		val deltakerInserted = deltaker.oppdater(nyDeltaker)
		val expectedStatus = deltaker.statuser.medNy(nyDeltaker.status, nyDeltaker.statuser.current.statusGjelderFra)
		val actual = deltakerInserted.copy(
			statuser = DeltakerStatuser(deltakerInserted.statuser.statuser.map { it.copy(id = uuid) })
		)
		val expected = nyDeltaker.copy(
			statuser = DeltakerStatuser(expectedStatus.statuser.map { it.copy(id = uuid) })
		)

		assertEquals(expected, actual)
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
		val deltaker = deltaker(status = DELTAR, startDato = yesterday, sluttDato = today).progressStatus()
		assertEquals(DELTAR, deltaker.status)
	}

	@Test
	fun `progressstatus - status er DELTAR, sluttdato i morgen - status endres ikke`() {
		val deltaker = deltaker(status = VENTER_PA_OPPSTART, startDato = yesterday, sluttDato = tomorrow).progressStatus()
		assertEquals(DELTAR, deltaker.status)
	}

	private fun deltaker(status: Deltaker.Status, startDato: LocalDate? = null, sluttDato: LocalDate? = null):Deltaker {
		val statuser = DeltakerStatuser(listOf(DeltakerStatus.nyAktiv(status)))
		return Deltaker(statuser = statuser, startDato = startDato, sluttDato = sluttDato, registrertDato = LocalDateTime.now(), gjennomforingId = UUID.randomUUID())

	}

}
