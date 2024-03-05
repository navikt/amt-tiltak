package no.nav.amt.tiltak.kafka.deltaker_ingestor

import no.nav.amt.tiltak.core.domain.tiltak.Adresse
import no.nav.amt.tiltak.core.domain.tiltak.Adressebeskyttelse
import no.nav.amt.tiltak.core.domain.tiltak.DeltakelsesInnhold
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerHistorikk
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Kilde
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class DeltakerDto(
	val id: UUID,
	val deltakerlisteId: UUID,
	val personalia: DeltakerPersonaliaDto,
	val status: DeltakerStatusDto,
	val dagerPerUke: Float?,
	val prosentStilling: Double?,
	val oppstartsdato: LocalDate?,
	val sluttdato: LocalDate?,
	val innsoktDato: LocalDate,
	val forsteVedtakFattet: LocalDate?,
	val bestillingTekst: String?,
	val navKontor: String?,
	val navVeileder: DeltakerNavVeilederDto?,
	val deltarPaKurs: Boolean,
	val kilde: Kilde?,
	val innhold: DeltakelsesInnhold?,
	val historikk: List<DeltakerHistorikk>?,
	val sistEndretAv: UUID?,
	val sistEndretAvEnhet: UUID?,
	val sistEndret: LocalDateTime?,
)

data class DeltakerPersonaliaDto(
	val personId: UUID?,
	val personident: String,
	val navn: Navn,
	val kontaktinformasjon: DeltakerKontaktinformasjonDto,
	val skjermet: Boolean,
	val adresse: Adresse?,
	val adressebeskyttelse: Adressebeskyttelse?,
) {
	data class Navn(
		val fornavn: String,
		val mellomnavn: String?,
		val etternavn: String,
	)

	data class DeltakerKontaktinformasjonDto(
		val telefonnummer: String?,
		val epost: String?,
	)
}

data class DeltakerNavVeilederDto(
	val id: UUID,
	val navn: String,
	val epost: String?,
	val telefonnummer: String?,
)

data class DeltakerStatusDto(
	val id: UUID?,
	val type: DeltakerStatus.Type,
	val aarsak: DeltakerStatus.Aarsak?,
	val gyldigFra: LocalDateTime,
	val opprettetDato: LocalDateTime,
)
