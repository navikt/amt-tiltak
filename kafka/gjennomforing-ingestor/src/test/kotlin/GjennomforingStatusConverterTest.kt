import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.kafka.tiltaksgjennomforing_ingestor.GjennomforingMessage
import no.nav.amt.tiltak.kafka.tiltaksgjennomforing_ingestor.GjennomforingStatusConverter
import org.junit.jupiter.api.Test

class GjennomforingStatusConverterTest {

	@Test
	fun `convert() - konverterer avlyst til AVSLUTTET`() {
		GjennomforingStatusConverter.convert(GjennomforingMessage.Status.AVLYST.name) shouldBe Gjennomforing.Status.AVSLUTTET
	}

	@Test
	fun `convert() - konverterer planlagt til IKKE_STARTET`() {
		GjennomforingStatusConverter.convert(GjennomforingMessage.Status.APENT_FOR_INNSOK.name) shouldBe Gjennomforing.Status.APENT_FOR_INNSOK
	}

	@Test
	fun `convert() - konverterer planlagt til GJENNOMFORES`() {
		GjennomforingStatusConverter.convert(GjennomforingMessage.Status.GJENNOMFORES.name) shouldBe Gjennomforing.Status.GJENNOMFORES
	}
}
