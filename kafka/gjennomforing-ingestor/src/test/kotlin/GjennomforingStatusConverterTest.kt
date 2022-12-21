import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.kafka.tiltaksgjennomforing_ingestor.GjennomforingStatusConverter

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GjennomforingStatusConverterTest {

	@Test
	fun `convert() - konverterer avlyst til AVSLUTTET`() {
		GjennomforingStatusConverter.convert("AVLYST") shouldBe Gjennomforing.Status.AVSLUTTET
	}

	@Test
	fun `convert() - konverterer planlagt til IKKE_STARTET`() {
		GjennomforingStatusConverter.convert("PLANLAGT") shouldBe Gjennomforing.Status.IKKE_STARTET
	}

	@Test
	fun `convert() - konverterer planlagt til GJENNOMFORES`() {
		GjennomforingStatusConverter.convert("GJENNOMFOR") shouldBe Gjennomforing.Status.GJENNOMFORES
	}

	@Test
	fun `convert() - ukjent status - kaster exception `() {
		assertThrows<RuntimeException> { GjennomforingStatusConverter.convert("UKJENT STATUS") }
	}
}
