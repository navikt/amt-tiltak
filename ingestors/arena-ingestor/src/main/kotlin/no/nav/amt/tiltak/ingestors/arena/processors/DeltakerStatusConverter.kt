package no.nav.amt.tiltak.ingestors.arena.processors

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime


private typealias ConversionStrategy = (StatusDates) -> Deltaker.Status

@Service
internal class DeltakerStatusConverter(
	private val meterRegistry: MeterRegistry
) {

	private val alltidIkkeAktuell : ConversionStrategy = {
		Deltaker.Status.IKKE_AKTUELL
	}

	private val gjennomforendeStatus : ConversionStrategy = {
		if (it.startDatoPassert() && it.sluttDatoPassert())
			Deltaker.Status.HAR_SLUTTET
		else if (it.startDatoPassert())
			Deltaker.Status.GJENNOMFORES
		else Deltaker.Status.VENTER_PA_OPPSTART
	}

	private val avsluttendeStatus : ConversionStrategy = {
		if (it.endretEtterStartDato())
			Deltaker.Status.HAR_SLUTTET
		else
			Deltaker.Status.IKKE_AKTUELL
	}

	private val alleStatuser: Map<String, ConversionStrategy> = mapOf(

		"DELAVB" to avsluttendeStatus, // Deltakelse avbrutt
		"FULLF" to avsluttendeStatus, // Fullført
		"GJENN_AVB" to avsluttendeStatus, // Gjennomføring avbrutt
		"GJENN_AVL" to avsluttendeStatus, // Gjennomføring avlyst
		"IKKEM" to avsluttendeStatus, // Ikke møtt

		"GJENN" to gjennomforendeStatus, // Gjennomføres
		"INFOMOETE" to gjennomforendeStatus, // Informasjonmøte
		"JATAKK" to gjennomforendeStatus, // Takket ja  til tilbud
		"VENTELISTE" to gjennomforendeStatus, // Venteliste
		"AKTUELL" to gjennomforendeStatus, // Aktuell
		"TILBUD" to gjennomforendeStatus, // Godkjent tiltaksplass

		"IKKAKTUELL" to alltidIkkeAktuell, // Ikke aktuell
		"AVSLAG" to alltidIkkeAktuell, // Fått avslag
		"NEITAKK" to alltidIkkeAktuell, // Takket nei til tilbud
	)

	internal fun convert(
		deltakerStatusCode: String?,
		startDato: LocalDate?,
		sluttDato: LocalDate?,
		datoStatusEndring: LocalDate?
	): Deltaker.Status {
		requireNotNull(deltakerStatusCode) { "deltakerStatsKode kan ikke være null" }

		return alleStatuser.getValue(deltakerStatusCode)(StatusDates(startDato, sluttDato, datoStatusEndring))
			.also {
				meterRegistry.counter(
					"amt.tiltak.deltaker.status",
					listOf(Tag.of("arena", deltakerStatusCode), Tag.of("amt-tiltak", it.name))
				).increment()
			}
	}

}

private data class StatusDates(
	private val start: LocalDate?,
	private val end: LocalDate?,
	private val datoStatusEndring: LocalDate?
) {

	fun startDatoPassert() = start?.isBefore(LocalDate.now()) ?: false
	fun sluttDatoPassert() = end?.isBefore(LocalDate.now()) ?: false
	fun endretEtterStartDato() = datoStatusEndring?.isAfter(start) ?: false

}
