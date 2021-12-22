package no.nav.amt.tiltak.ingestors.arena.processors

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import org.springframework.stereotype.Service
import java.time.LocalDate


private typealias ConversionStrategy = (DateRange) -> Deltaker.Status

@Service
internal class DeltakerStatusConverter(
	private val meterRegistry: MeterRegistry
) {

	private val alleStatuser: Map<String, ConversionStrategy> = mapOf(
		"AKTUELL" to { Deltaker.Status.IKKE_AKTUELL }, // Aktuell
		"AVSLAG" to { Deltaker.Status.IKKE_AKTUELL }, // Fått avslag
		"DELAVB" to { if (it.startDatoPassert()) Deltaker.Status.HAR_SLUTTET else Deltaker.Status.IKKE_AKTUELL }, // Deltakelse avbrutt
		"FULLF" to { Deltaker.Status.HAR_SLUTTET }, // Fullført
		"GJENN" to
			{
				if (it.startDatoPassert() && it.sluttDatoPassert())
					Deltaker.Status.HAR_SLUTTET
				else if (it.startDatoPassert())
					Deltaker.Status.GJENNOMFORES
				else Deltaker.Status.VENTER_PA_OPPSTART
			}, // Gjennomføres
		"GJENN_AVB" to { if (it.startDatoPassert()) Deltaker.Status.HAR_SLUTTET else Deltaker.Status.IKKE_AKTUELL }, // Gjennomføring avbrutt
		"GJENN_AVL" to { if (it.startDatoPassert()) Deltaker.Status.HAR_SLUTTET else Deltaker.Status.IKKE_AKTUELL }, // Gjennomføring avlyst
		"IKKAKTUELL" to { Deltaker.Status.IKKE_AKTUELL }, // Ikke aktuell
		"IKKEM" to { if (it.startDatoPassert()) Deltaker.Status.HAR_SLUTTET else Deltaker.Status.IKKE_AKTUELL }, // Ikke møtt
		"INFOMOETE" to { Deltaker.Status.IKKE_AKTUELL }, // Informasjonmøte
		"JATAKK" to { Deltaker.Status.IKKE_AKTUELL }, // Takket ja  til tilbud
		"NEITAKK" to { Deltaker.Status.IKKE_AKTUELL }, // Takket nei til tilbud
		"TILBUD" to { Deltaker.Status.VENTER_PA_OPPSTART }, // Godkjent tiltaksplass
		"VENTELISTE" to { Deltaker.Status.IKKE_AKTUELL } // Venteliste
	)

	internal fun convert(
		deltakerStatusCode: String?,
		startDato: LocalDate?,
		sluttDato: LocalDate?
	): Deltaker.Status {
		requireNotNull(deltakerStatusCode) { "deltakerStatsKode kan ikke være null" }

		return alleStatuser.getValue(deltakerStatusCode)(DateRange(startDato, sluttDato))
			.also {
				meterRegistry.counter(
					"amt.tiltak.deltaker.status",
					listOf(Tag.of("arena", deltakerStatusCode), Tag.of("amt-tiltak", it.name))
				).increment()
			}
	}

}

private data class DateRange(
	val start: LocalDate?,
	val end: LocalDate?
) {

	fun startDatoPassert() = start?.isBefore(LocalDate.now()) ?: false
	fun sluttDatoPassert() = end?.isBefore(LocalDate.now()) ?: false

}
