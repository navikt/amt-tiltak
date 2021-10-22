package no.nav.amt.tiltak.tiltak.dbo

import no.nav.amt.tiltak.tiltak.utils.UpdateStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class TiltakDboTest {

	@Test
	internal fun `update() should return NO_CHANGES if the objects are equal`() {
		val tiltak = TiltakDbo(
			1,
			UUID.randomUUID(),
			"1",
			"'Navn",
			"Type",
			LocalDateTime.now(),
			LocalDateTime.now()
		)

		val updated = tiltak.update(tiltak.copy())

		assertEquals(updated.status, UpdateStatus.NO_CHANGE)
		assertNull(updated.updatedObject)
	}

	@Test
	internal fun `update() should return UPDATED with updated object if navn has changed`() {
		val tiltak = TiltakDbo(
			1,
			UUID.randomUUID(),
			"1",
			"'Navn",
			"Type",
			LocalDateTime.now(),
			LocalDateTime.now()
		)

		val updatedNavn = "UpdatedNavn"

		val updated = tiltak.update(
			tiltak.copy(
				navn = updatedNavn
			)
		)

		assertEquals(updated.status, UpdateStatus.UPDATED)
		assertNotNull(updated.updatedObject)

		assertEquals(updatedNavn, updated.updatedObject?.navn)
	}

	@Test
	internal fun `update() should return UPDATED with updated object if type has changed`() {
		val tiltak = TiltakDbo(
			1,
			UUID.randomUUID(),
			"1",
			"'Navn",
			"Type",
			LocalDateTime.now(),
			LocalDateTime.now()
		)

		val updatedType = "UpdatedType"

		val updated = tiltak.update(
			tiltak.copy(
				type = updatedType
			)
		)

		assertEquals(updated.status, UpdateStatus.UPDATED)
		assertNotNull(updated.updatedObject)

		assertEquals(updatedType, updated.updatedObject?.type)
	}

	@Test
	internal fun `update() should return UPDATED with updated object if all fields has changed`() {
		val tiltak = TiltakDbo(
			1,
			UUID.randomUUID(),
			"1",
			"'Navn",
			"Type",
			LocalDateTime.now(),
			LocalDateTime.now()
		)

		val updatedNavn = "UpdatedNavn"
		val updatedType = "UpdatedType"

		val updated = tiltak.update(
			tiltak.copy(
				navn = updatedNavn,
				type = updatedType
			)
		)

		assertEquals(updated.status, UpdateStatus.UPDATED)
		assertNotNull(updated.updatedObject)

		assertEquals(updatedNavn, updated.updatedObject?.navn)
		assertEquals(updatedType, updated.updatedObject?.type)
	}
}
