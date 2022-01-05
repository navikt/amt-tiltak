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

	"status - AKTUELL - returnerer VENTER_PÅ_OPPSTART" {
		converter.convert("AKTUELL", null, null, null) shouldBe VENTER_PA_OPPSTART
	}
	"status - AKTUELL og har startdato i fortid - returnerer GJENNOMFORES" {
		converter.convert("AKTUELL", yesterday, null, null) shouldBe GJENNOMFORES
	}
	"status - AKTUELL og har startdato i fremtid - returnerer VENTER_PÅ_OPPSTART" {
		converter.convert("AKTUELL", tomorrow, null, null) shouldBe VENTER_PA_OPPSTART
	}
	"status - AKTUELL og har sluttdato i fortid - returnerer HAR_SLUTTET" {
		converter.convert("AKTUELL", yesterday.minusDays(1), yesterday, null) shouldBe HAR_SLUTTET
	}
	"status - AKTUELL og har sluttdato i fremtid - returnerer GJENNOMFORES" {
		converter.convert("AKTUELL", yesterday, tomorrow, null) shouldBe GJENNOMFORES
	}

	"status - AVSLAG og mangler startdato - returnerer IKKE_AKTUELL" {
		converter.convert("AVSLAG", null, null, null) shouldBe IKKE_AKTUELL
	}


	"status - DELAVB og mangler startdato - returnerer IKKE_AKTUELL" {
		converter.convert("DELAVB", null, null, null) shouldBe IKKE_AKTUELL
	}
	"status - DELAVB og har startdato før endretdato - returnerer HAR_SLUTTET" {
		converter.convert("DELAVB", yesterday.minusDays(1), null, yesterday) shouldBe HAR_SLUTTET
	}
	"status - DELAVB og har startdato etter endretdato - returnerer IKKE_AKTUELL" {
		converter.convert("DELAVB", yesterday, null, yesterday.minusDays(1)) shouldBe IKKE_AKTUELL
	}
	"status - DELAVB og har startdato i fremtid - returnerer IKKE_AKTUELL" {
		converter.convert("DELAVB", tomorrow, null, null) shouldBe IKKE_AKTUELL
	}

	"status - FULLF og mangler startdato - returnerer IKKE_AKTUELL" {
		converter.convert("FULLF", null, null, null) shouldBe IKKE_AKTUELL
	}
	"status - FULLF og har startdato før endretdato - returnerer HAR_SLUTTET" {
		converter.convert("FULLF", yesterday.minusDays(1), null, yesterday) shouldBe HAR_SLUTTET
	}
	"status - FULLF og har startdato etter endretdato - returnerer IKKE_AKTUELL" {
		converter.convert("FULLF", yesterday, null, yesterday.minusDays(1)) shouldBe IKKE_AKTUELL
	}
	"status - FULLF og har startdato i fremtid - returnerer IKKE_AKTUELL" {
		converter.convert("FULLF", tomorrow, null, null) shouldBe IKKE_AKTUELL
	}

	"status - GJENN - returnerer VENTER_PÅ_OPPSTART" {
		converter.convert("GJENN", null, null, null) shouldBe VENTER_PA_OPPSTART
	}
	"status - GJENN og har startdato i fortid - returnerer GJENNOMFORES" {
		converter.convert("GJENN", yesterday, null, null) shouldBe GJENNOMFORES
	}
	"status - GJENN og har startdato i fremtid - returnerer VENTER_PÅ_OPPSTART" {
		converter.convert("GJENN", tomorrow, null, null) shouldBe VENTER_PA_OPPSTART
	}
	"status - GJENN og har sluttdato i fortid - returnerer HAR_SLUTTET" {
		converter.convert("GJENN", yesterday.minusDays(1), yesterday, null) shouldBe HAR_SLUTTET
	}
	"status - GJENN og har sluttdato i fremtid - returnerer GJENNOMFORES" {
		converter.convert("GJENN", yesterday, tomorrow, null) shouldBe GJENNOMFORES
	}


	"status - GJENN_AVB og mangler startdato - returnerer IKKE_AKTUELL" {
		converter.convert("GJENN_AVB", null, null, null) shouldBe IKKE_AKTUELL
	}
	"status - GJENN_AVB og har startdato før endretdato - returnerer HAR_SLUTTET" {
		converter.convert("GJENN_AVB", yesterday.minusDays(1), null, yesterday) shouldBe HAR_SLUTTET
	}
	"status - GJENN_AVB og har startdato etter endretdato - returnerer IKKE_AKTUELL" {
		converter.convert("GJENN_AVB", yesterday, null, yesterday.minusDays(1)) shouldBe IKKE_AKTUELL
	}
	"status - GJENN_AVB og har startdato i fremtid - returnerer IKKE_AKTUELL" {
		converter.convert("GJENN_AVB", tomorrow, null, null) shouldBe IKKE_AKTUELL
	}


	"status - GJENN_AVL og mangler startdato - returnerer IKKE_AKTUELL" {
		converter.convert("GJENN_AVL", null, null, null) shouldBe IKKE_AKTUELL
	}
	"status - GJENN_AVL og har startdato før endretdato - returnerer HAR_SLUTTET" {
		converter.convert("GJENN_AVL", yesterday.minusDays(1), null, yesterday) shouldBe HAR_SLUTTET
	}
	"status - GJENN_AVL og har startdato etter endretdato - returnerer IKKE_AKTUELL" {
		converter.convert("GJENN_AVL", yesterday, null, yesterday.minusDays(1)) shouldBe IKKE_AKTUELL
	}
	"status - GJENN_AVL og har startdato i fremtid - returnerer IKKE_AKTUELL" {
		converter.convert("GJENN_AVL", tomorrow, null, null) shouldBe IKKE_AKTUELL
	}


	"status - IKKAKTUELL - returnerer IKKE_AKTUELL" {
		converter.convert("IKKAKTUELL", null, null, null) shouldBe IKKE_AKTUELL
	}


	"status - IKKEM og mangler startdato - returnerer IKKE_AKTUELL" {
		converter.convert("IKKEM", null, null, null) shouldBe IKKE_AKTUELL
	}
	"status - IKKEM og har startdato før endretdato - returnerer HAR_SLUTTET" {
		converter.convert("IKKEM", yesterday.minusDays(1), null, yesterday) shouldBe HAR_SLUTTET
	}
	"status - IKKEM og har startdato etter endretdato - returnerer IKKE_AKTUELL" {
		converter.convert("IKKEM", yesterday, null, yesterday.minusDays(1)) shouldBe IKKE_AKTUELL
	}
	"status - IKKEM og har startdato i fremtid - returnerer IKKE_AKTUELL" {
		converter.convert("IKKEM", tomorrow, null, null) shouldBe IKKE_AKTUELL
	}


	"status - INFOMOETE - returnerer VENTER_PÅ_OPPSTART" {
		converter.convert("INFOMOETE", null, null, null) shouldBe VENTER_PA_OPPSTART
	}
	"status - INFOMOETE og har startdato i fortid - returnerer GJENNOMFORES" {
		converter.convert("INFOMOETE", yesterday, null, null) shouldBe GJENNOMFORES
	}
	"status - INFOMOETE og har startdato i fremtid - returnerer VENTER_PÅ_OPPSTART" {
		converter.convert("INFOMOETE", tomorrow, null, null) shouldBe VENTER_PA_OPPSTART
	}
	"status - INFOMOETE og har sluttdato i fortid - returnerer HAR_SLUTTET" {
		converter.convert("INFOMOETE", yesterday.minusDays(1), yesterday, null) shouldBe HAR_SLUTTET
	}
	"status - INFOMOETE og har sluttdato i fremtid - returnerer GJENNOMFORES" {
		converter.convert("INFOMOETE", yesterday, tomorrow, null) shouldBe GJENNOMFORES
	}

	"status - JATAKK - returnerer VENTER_PÅ_OPPSTART" {
		converter.convert("JATAKK", null, null, null) shouldBe VENTER_PA_OPPSTART
	}
	"status - JATAKK og har startdato i fortid - returnerer GJENNOMFORES" {
		converter.convert("JATAKK", yesterday, null, null) shouldBe GJENNOMFORES
	}
	"status - JATAKK og har startdato i fremtid - returnerer VENTER_PÅ_OPPSTART" {
		converter.convert("JATAKK", tomorrow, null, null) shouldBe VENTER_PA_OPPSTART
	}
	"status - JATAKK og har sluttdato i fortid - returnerer HAR_SLUTTET" {
		converter.convert("JATAKK", yesterday.minusDays(1), yesterday, null) shouldBe HAR_SLUTTET
	}
	"status - JATAKK og har sluttdato i fremtid - returnerer GJENNOMFORES" {
		converter.convert("JATAKK", yesterday, tomorrow, null) shouldBe GJENNOMFORES
	}

	"status - NEITAKK - returnerer IKKE_AKTUELL" {
		converter.convert("NEITAKK", null, null, null) shouldBe IKKE_AKTUELL
	}


	"status - TILBUD - returnerer VENTER_PÅ_OPPSTART" {
		converter.convert("TILBUD", null, null, null) shouldBe VENTER_PA_OPPSTART
	}
	"status - TILBUD og har startdato i fortid - returnerer GJENNOMFORES" {
		converter.convert("TILBUD", yesterday, null, null) shouldBe GJENNOMFORES
	}
	"status - TILBUD og har startdato i fremtid - returnerer VENTER_PÅ_OPPSTART" {
		converter.convert("TILBUD", tomorrow, null, null) shouldBe VENTER_PA_OPPSTART
	}
	"status - TILBUD og har sluttdato i fortid - returnerer HAR_SLUTTET" {
		converter.convert("TILBUD", yesterday.minusDays(1), yesterday, null) shouldBe HAR_SLUTTET
	}
	"status - TILBUD og har sluttdato i fremtid - returnerer GJENNOMFORES" {
		converter.convert("TILBUD", yesterday, tomorrow, null) shouldBe GJENNOMFORES
	}


	"status - VENTELISTE - returnerer VENTER_PÅ_OPPSTART" {
		converter.convert("VENTELISTE", null, null, null) shouldBe VENTER_PA_OPPSTART
	}
	"status - VENTELISTE og har startdato i fortid - returnerer GJENNOMFORES" {
		converter.convert("VENTELISTE", yesterday, null, null) shouldBe GJENNOMFORES
	}
	"status - VENTELISTE og har startdato i fremtid - returnerer VENTER_PÅ_OPPSTART" {
		converter.convert("VENTELISTE", tomorrow, null, null) shouldBe VENTER_PA_OPPSTART
	}
	"status - VENTELISTE og har sluttdato i fortid - returnerer HAR_SLUTTET" {
		converter.convert("VENTELISTE", yesterday.minusDays(1), yesterday, null) shouldBe HAR_SLUTTET
	}
	"status - VENTELISTE og har sluttdato i fremtid - returnerer GJENNOMFORES" {
		converter.convert("VENTELISTE", yesterday, tomorrow, null) shouldBe GJENNOMFORES
	}

})
