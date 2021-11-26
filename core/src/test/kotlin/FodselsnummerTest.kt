import no.nav.amt.tiltak.core.domain.tiltak.Fodselsnummer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.NumberFormatException

class FodselsnummerTest {

	@Test
	fun `Fodselsnummer - 11 siffer - kan instansieres`() {
		Fodselsnummer("00000000000")
	}

	@Test
	fun `Fodselsnummer - ugyldige tegn - kan ikke instansieres`() {
		assertThrows<NumberFormatException> { Fodselsnummer("11a19000000") }
	}

	@Test
	fun `Fodselsnummer - for mange tegn - kan ikke instansieres`() {
		assertThrows<RuntimeException> { Fodselsnummer("111219031291") }
	}

	@Test
	fun `toFodselDato() - født etter 2000 - gir gyldig dato`() {
		val fDato = Fodselsnummer("04020776600").toFodselDato()
		assertEquals("2007-02-04", fDato.toString())
	}

	@Test
	fun `toFodselDato() - født etter 2000 2 - gir gyldig dato`() {
		val fDato = Fodselsnummer("27090964400").toFodselDato()
		assertEquals("2009-09-27", fDato.toString())
	}

	@Test
	fun `toFodselDato() - født 1900 - gir gyldig dato`() {
		val fDato = Fodselsnummer("20034426600").toFodselDato()
		assertEquals("1944-03-20", fDato.toString())
	}

	@Test
	fun `toFodselDato() - født 1900 2 - gir gyldig dato`() {
		val fDato = Fodselsnummer("03059620700").toFodselDato()
		assertEquals("1996-05-03", fDato.toString())
	}

}
