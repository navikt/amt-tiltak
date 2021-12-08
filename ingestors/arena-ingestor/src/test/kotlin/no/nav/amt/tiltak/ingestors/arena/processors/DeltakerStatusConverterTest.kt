package no.nav.amt.tiltak.ingestors.arena.processors

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker.Status.*
import java.time.LocalDate

private val tomorrow = LocalDate.now().plusDays(1)
private val yesterday = LocalDate.now().minusDays(1)

/* Kjente statuser
	"AKTUELL", // Aktuell
	"AVSLAG", // Fått avslag
	"DELAVB", // Deltakelse avbrutt
	"FULLF", // Fullført
	"GJENN", // Gjennomføres
	"GJENN_AVB", // Gjennomføring avbrutt
	"GJENN_AVL", // Gjennomføring avlyst
	"IKKAKTUELL", // Ikke aktuell
	"IKKEM", // Ikke møtt
	"INFOMOETE", // Informasjonmøte
	"JATAKK", // Takket ja  til tilbud
	"NEITAKK", // Takket nei til tilbud
	"TILBUD", // Godkjent tiltaksplass
	"VENTELISTE" // Venteliste
*/


class DeltakerStatusConverterTest : StringSpec({

	val converter = DeltakerStatusConverter(SimpleMeterRegistry())

	"status - AKTUELL og mangler startdato - returnerer IKKE_AKTUELL" {
		converter.convert("AKTUELL", null, null) shouldBe IKKE_AKTUELL
	}


	"status - AVSLAG og mangler startdato - returnerer IKKE_AKTUELL" {
		converter.convert("AVSLAG", null, null) shouldBe IKKE_AKTUELL
	}


	"status - DELAVB og mangler startdato - returnerer IKKE_AKTUELL" {
		converter.convert("DELAVB", null, null) shouldBe IKKE_AKTUELL
	}
	"status - DELAVB og har startdato i fortid - returnerer HAR_SLUTTET" {
		converter.convert("DELAVB", yesterday, null) shouldBe HAR_SLUTTET
	}
	"status - DELAVB og har startdato i fremtid - returnerer IKKE_AKTUELL" {
		converter.convert("DELAVB", tomorrow, null) shouldBe IKKE_AKTUELL
	}


	"status - FULLF - returnerer HAR_SLUTTET" {
		converter.convert("FULLF", null, null) shouldBe HAR_SLUTTET
	}


	"status - GJENN - returnerer VENTER_PÅ_OPPSTART" {
		converter.convert("GJENN", null, null) shouldBe VENTER_PA_OPPSTART
	}
	"status - GJENN og har startdato i fortid - returnerer GJENNOMFORES" {
		converter.convert("GJENN", yesterday, null) shouldBe GJENNOMFORES
	}
	"status - GJENN og har startdato i fremtid - returnerer VENTER_PÅ_OPPSTART" {
		converter.convert("GJENN", tomorrow, null) shouldBe VENTER_PA_OPPSTART
	}
	"status - GJENN og har sluttdato i fortid - returnerer HAR_SLUTTET" {
		converter.convert("GJENN", yesterday.minusDays(1), yesterday) shouldBe HAR_SLUTTET
	}
	"status - GJENN og har sluttdato i fremtid - returnerer GJENNOMFORES" {
		converter.convert("GJENN", yesterday, tomorrow) shouldBe GJENNOMFORES
	}


	"status - GJENN_AVB og mangler startdato - returnerer IKKE_AKTUELL" {
		converter.convert("GJENN_AVB", null, null) shouldBe IKKE_AKTUELL
	}
	"status - GJENN_AVB og har startdato i fortid - returnerer HAR_SLUTTET" {
		converter.convert("GJENN_AVB", yesterday, null) shouldBe HAR_SLUTTET
	}
	"status - GJENN_AVB og har startdato i fremtid - returnerer IKKE_AKTUELL" {
		converter.convert("GJENN_AVB", tomorrow, null) shouldBe IKKE_AKTUELL
	}


	"status - GJENN_AVL og mangler startdato - returnerer IKKE_AKTUELL" {
		converter.convert("GJENN_AVL", null, null) shouldBe IKKE_AKTUELL
	}
	"status - GJENN_AVL og har startdato i fortid - returnerer HAR_SLUTTET" {
		converter.convert("GJENN_AVL", yesterday, null) shouldBe HAR_SLUTTET
	}
	"status - GJENN_AVL og har startdato i fremtid - returnerer IKKE_AKTUELL" {
		converter.convert("GJENN_AVL", tomorrow, null) shouldBe IKKE_AKTUELL
	}


	"status - IKKAKTUELL - returnerer IKKE_AKTUELL" {
		converter.convert("IKKAKTUELL", null, null) shouldBe IKKE_AKTUELL
	}


	"status - IKKEM og mangler startdato - returnerer IKKE_AKTUELL" {
		converter.convert("IKKEM", null, null) shouldBe IKKE_AKTUELL
	}
	"status - IKKEM og har startdato i fortid - returnerer HAR_SLUTTET" {
		converter.convert("IKKEM", yesterday, null) shouldBe HAR_SLUTTET
	}
	"status - IKKEM og har startdato i fremtid - returnerer IKKE_AKTUELL" {
		converter.convert("IKKEM", tomorrow, null) shouldBe IKKE_AKTUELL
	}


	"status - INFOMOETE - returnerer IKKE_AKTUELL" {
		converter.convert("INFOMOETE", null, null) shouldBe IKKE_AKTUELL
	}


	"status - JATAKK - returnerer IKKE_AKTUELL" {
		converter.convert("JATAKK", null, null) shouldBe IKKE_AKTUELL
	}


	"status - NEITAKK - returnerer IKKE_AKTUELL" {
		converter.convert("NEITAKK", null, null) shouldBe IKKE_AKTUELL
	}


	"status - TILBUD - returnerer VENTER_PÅ_OPPSTART" {
		converter.convert("TILBUD", null, null) shouldBe VENTER_PA_OPPSTART
	}


	"status - VENTELISTE - returnerer IKKE_AKTUELL" {
		converter.convert("VENTELISTE", null, null) shouldBe IKKE_AKTUELL
	}

})
