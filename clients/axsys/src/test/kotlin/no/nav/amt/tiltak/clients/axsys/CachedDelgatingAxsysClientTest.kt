package no.nav.amt.tiltak.clients.axsys

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.times

internal class CachedDelgatingAxsysClientTest {

	val delegate = Mockito.mock(AxsysClient::class.java)

	val cachedClient = CachedDelgatingAxsysClient(delegate)

	val brukerIdent = "AB12345"

	val enheter = Enheter(listOf(
		Enhet(
		enhetId = "1234",
		temaer = listOf("ABC", "DEF"),
		navn = "Bygdeby"
	)
	))

	@Test
	fun `hentTilganger - kaller to ganger - delegate blir bare kalt en gang og resultatet er likt`() {
		Mockito.`when`(delegate.hentTilganger(brukerIdent)).thenReturn(enheter)

		val response = cachedClient.hentTilganger(brukerIdent)
		val response2 = cachedClient.hentTilganger(brukerIdent)

		assertEquals(enheter, response)
		assertEquals(enheter, response2)

		Mockito.verify(delegate, times(1)).hentTilganger(brukerIdent)
	}

}
