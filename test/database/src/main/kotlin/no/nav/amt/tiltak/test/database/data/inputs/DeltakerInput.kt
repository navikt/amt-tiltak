package no.nav.amt.tiltak.test.database.data.inputs

import no.nav.amt.tiltak.core.domain.tiltak.DeltakelsesInnhold
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerHistorikk
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Kilde
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

data class DeltakerInput(
	val id: UUID,
	val brukerId: UUID,
	val gjennomforingId: UUID,
	val startDato: LocalDate,
	val sluttDato: LocalDate,
	val dagerPerUke: Float,
	val prosentStilling: Float,
	val registrertDato: LocalDateTime,
	val innsokBegrunnelse: String?,
	val createdAt: ZonedDateTime = ZonedDateTime.of(2022, 2, 13, 0, 0, 0, 0, ZoneId.systemDefault()),
	val endretDato: LocalDateTime = LocalDateTime.now(),
	val innhold: DeltakelsesInnhold?,
	val kilde: Kilde,
	val forsteVedtakFattet: LocalDate?,
	val historikk: List<DeltakerHistorikk>?,
	val sistEndretAv: UUID?,
	val sistEndretAvEnhet: UUID?
) {
	fun toDeltaker(brukerInput: BrukerInput, statusInput: DeltakerStatusInput) = Deltaker(
		id = id,
		gjennomforingId = gjennomforingId,
		fornavn = brukerInput.fornavn,
		mellomnavn = brukerInput.mellomnavn,
		etternavn = brukerInput.etternavn,
		telefonnummer = brukerInput.telefonnummer,
		erSkjermet = brukerInput.erSkjermet,
		epost = brukerInput.epost,
		personIdent = brukerInput.personIdent,
		navEnhet = brukerInput.navEnhet?.toNavEnhet(),
		navVeilederId = brukerInput.ansvarligVeilederId,
		startDato = startDato,
		sluttDato = sluttDato,
		status = DeltakerStatus(
			id = statusInput.id,
			type = DeltakerStatus.Type.valueOf(statusInput.status),
			aarsak = null,
			gyldigFra = statusInput.gyldigFra,
			opprettetDato = statusInput.createdAt.toLocalDateTime(),
			aktiv = statusInput.aktiv
		),
		registrertDato = registrertDato,
		dagerPerUke = dagerPerUke,
		prosentStilling = prosentStilling,
		innsokBegrunnelse = innsokBegrunnelse,
		endretDato = endretDato,
		adressebeskyttelse = brukerInput.adressebeskyttelse,
		innhold = innhold,
		kilde = kilde,
		forsteVedtakFattet = forsteVedtakFattet,
		historikk = historikk,
		sistEndretAv = sistEndretAv,
		sistEndretAvEnhet = sistEndretAvEnhet
	)
}
