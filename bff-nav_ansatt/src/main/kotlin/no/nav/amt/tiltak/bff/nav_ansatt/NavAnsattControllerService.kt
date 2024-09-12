package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.bff.nav_ansatt.dto.DeltakerDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.EndringsmeldingDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.HentGjennomforingerDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.TiltakDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.VurderingDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.toDto
import no.nav.amt.tiltak.bff.nav_ansatt.response.MeldingerFraArrangorResponse
import no.nav.amt.tiltak.common.auth.AdGruppe
import no.nav.amt.tiltak.core.domain.tiltak.Adressebeskyttelse
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Vurdering
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.UnleashService
import no.nav.amt.tiltak.core.port.VurderingService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class NavAnsattControllerService(
	private val endringsmeldingService: EndringsmeldingService,
	private val deltakerService: DeltakerService,
	private val gjennomforingService: GjennomforingService,
	private val vurderingService: VurderingService,
	private val unleashService: UnleashService
) {
	private val log = LoggerFactory.getLogger(javaClass)

	companion object {
		fun harTilgangTilDeltaker(deltaker: Deltaker, tilganger: List<AdGruppe>): Boolean {
			val tilgangTilMuligAdressebeskyttetDeltaker = when (deltaker.adressebeskyttelse) {
				Adressebeskyttelse.STRENGT_FORTROLIG_UTLAND,
				Adressebeskyttelse.STRENGT_FORTROLIG -> tilganger.contains(AdGruppe.TILTAKSANSVARLIG_STRENGT_FORTROLIG_ADRESSE_GRUPPE)
				Adressebeskyttelse.FORTROLIG -> tilganger.contains(AdGruppe.TILTAKSANSVARLIG_FORTROLIG_ADRESSE_GRUPPE)
				null -> true
			}

			val tilgangTilMuligSkjermetDeltaker = if (deltaker.erSkjermet) {
				tilganger.contains(AdGruppe.TILTAKSANSVARLIG_EGNE_ANSATTE_GRUPPE)
			} else {
				true
			}

			return tilgangTilMuligAdressebeskyttetDeltaker && tilgangTilMuligSkjermetDeltaker
		}
	}

	fun hentEndringsmeldinger(gjennomforingId: UUID, tilganger: List<AdGruppe>): List<EndringsmeldingDto> {
		val endringsmeldinger = hentEndringsmeldingerForGjennomforing(gjennomforingId)
		val deltakerMap = deltakerService.hentDeltakerMap(endringsmeldinger.map { it.deltakerId })

		return endringsmeldinger.mapNotNull { endringsmelding -> tilEndringsmeldingDto(endringsmelding, deltakerMap, tilganger) }
	}

	fun hentMeldinger(gjennomforingId: UUID, tilganger: List<AdGruppe>): MeldingerFraArrangorResponse {
		val alleEndringsmeldinger = hentEndringsmeldingerForGjennomforing(gjennomforingId)
		val alleVurderinger = vurderingService.hentAktiveVurderingerForGjennomforing(gjennomforingId)

		val deltakerIder = mutableListOf<UUID>()
		deltakerIder.addAll(alleEndringsmeldinger.map { it.deltakerId })
		deltakerIder.addAll(alleVurderinger.map { it.deltakerId })

		val deltakerMap = deltakerService.hentDeltakerMap(deltakerIder.distinct())

		val endringsmeldinger = alleEndringsmeldinger.mapNotNull { endringsmelding ->
			tilEndringsmeldingDto(endringsmelding, deltakerMap, tilganger)
		}

		val vurderinger = alleVurderinger.mapNotNull { vurdering ->
			tilVurderingDto(vurdering, deltakerMap, tilganger)
		}
		return MeldingerFraArrangorResponse(endringsmeldinger, vurderinger)
	}

	fun hentGjennomforinger(gjennomforingIder: List<UUID>) : List<HentGjennomforingerDto> {
		return gjennomforingService.getGjennomforinger(gjennomforingIder).map { gjennomforing ->
			val aktiveEndringsmeldinger = hentEndringsmeldingerForGjennomforing(gjennomforing.id)
				.filter { it.status == Endringsmelding.Status.AKTIV }
			val alleVurderinger = vurderingService.hentAktiveVurderingerForGjennomforing(gjennomforing.id)

			val deltakere = deltakerService.hentDeltakere(
				aktiveEndringsmeldinger.map { it.deltakerId }
				.plus(alleVurderinger.map { it.deltakerId })
			)
			val harSkjermede = deltakere.any { it.erSkjermet }
			val adressebeskyttelser = deltakere.mapNotNull { it.adressebeskyttelse }.distinct()

			return@map gjennomforing.toDto(aktiveEndringsmeldinger.size, harSkjermede, adressebeskyttelser)
		}
	}


	private fun hentEndringsmeldingerForGjennomforing(gjennomforingId: UUID): List<Endringsmelding> {
		val gjennomforing = gjennomforingService.getGjennomforing(gjennomforingId)
		if (unleashService.erKometMasterForTiltakstype(gjennomforing.tiltak.kode)) {
			return emptyList()
		}
		return endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId)
	}

	private fun tilEndringsmeldingDto(
		endringsmelding: Endringsmelding,
		deltakerMap: Map<UUID, Deltaker>,
		tilganger: List<AdGruppe>,
	): EndringsmeldingDto? {
		val deltaker = getDeltaker(endringsmelding.deltakerId, deltakerMap) ?: return null

		return if (harTilgangTilDeltaker(deltaker, tilganger)) {
			endringsmelding.toDto(deltaker.toDto())
		} else {
			endringsmelding.toDto(DeltakerDto(erSkjermet = deltaker.erSkjermet, adressebeskyttelse = deltaker.adressebeskyttelse))
		}
	}


	private fun tilVurderingDto(
		vurdering: Vurdering,
		deltakerMap: Map<UUID, Deltaker>,
		tilganger: List<AdGruppe>,
	): VurderingDto? {
		val deltaker = getDeltaker(vurdering.deltakerId, deltakerMap) ?: return null

		return if (harTilgangTilDeltaker(deltaker, tilganger)) {
			vurdering.toDto(deltaker.toDto())
		} else {
			vurdering.toDto(DeltakerDto(erSkjermet = deltaker.erSkjermet, adressebeskyttelse = deltaker.adressebeskyttelse))
		}
	}


	private fun getDeltaker(deltakerId: UUID, deltakerMap: Map<UUID, Deltaker>): Deltaker? {
		deltakerMap[deltakerId]?.let { return it }
		log.warn("Fant ikke deltaker med id $deltakerId")
		return null
	}

	private fun Gjennomforing.toDto (
		antallAktiveEndringsmeldinger: Int,
		harSkjermede: Boolean,
		adressebeskyttelser: List<Adressebeskyttelse>
	) = HentGjennomforingerDto(
		id = id,
		navn = navn,
		lopenr = lopenr,
		opprettetAar = opprettetAar,
		arrangorNavn = arrangor.overordnetEnhetNavn ?: arrangor.navn,
		antallAktiveEndringsmeldinger = antallAktiveEndringsmeldinger,
		harSkjermedeDeltakere = harSkjermede,
		adressebeskyttelser = adressebeskyttelser,
		tiltak = TiltakDto(
			kode = tiltak.kode,
			navn = tiltak.navn,
		),
		status = status,
		startDato = startDato,
		sluttDato = sluttDato
	)

	private fun Endringsmelding.toDto(deltakerDto: DeltakerDto) = EndringsmeldingDto(
		id = id,
		deltaker = deltakerDto,
		status = status.toDto(),
		innhold = innhold?.toDto(),
		opprettetDato = opprettet,
		utfortTidspunkt = utfortTidspunkt,
		type = type.toDto()
	)

	private fun Endringsmelding.Type.toDto(): EndringsmeldingDto.Type {
		return when (this) {
			Endringsmelding.Type.LEGG_TIL_OPPSTARTSDATO -> EndringsmeldingDto.Type.LEGG_TIL_OPPSTARTSDATO
			Endringsmelding.Type.ENDRE_OPPSTARTSDATO -> EndringsmeldingDto.Type.ENDRE_OPPSTARTSDATO
			Endringsmelding.Type.FORLENG_DELTAKELSE -> EndringsmeldingDto.Type.FORLENG_DELTAKELSE
			Endringsmelding.Type.AVSLUTT_DELTAKELSE -> EndringsmeldingDto.Type.AVSLUTT_DELTAKELSE
			Endringsmelding.Type.DELTAKER_IKKE_AKTUELL -> EndringsmeldingDto.Type.DELTAKER_IKKE_AKTUELL
			Endringsmelding.Type.ENDRE_DELTAKELSE_PROSENT -> EndringsmeldingDto.Type.ENDRE_DELTAKELSE_PROSENT
			Endringsmelding.Type.ENDRE_SLUTTDATO -> EndringsmeldingDto.Type.ENDRE_SLUTTDATO
			Endringsmelding.Type.ENDRE_SLUTTAARSAK -> EndringsmeldingDto.Type.ENDRE_SLUTTAARSAK
		}
	}

	private fun Vurdering.toDto(deltakerDto: DeltakerDto) = VurderingDto(
		id = id,
		deltaker = deltakerDto,
		vurderingstype = vurderingstype,
		begrunnelse = begrunnelse,
		opprettetDato = gyldigFra
	)

	private fun Deltaker.toDto() = DeltakerDto(
		fornavn = fornavn,
		mellomnavn = mellomnavn,
		etternavn = etternavn,
		fodselsnummer = personIdent,
		erSkjermet = erSkjermet,
		adressebeskyttelse = adressebeskyttelse,
	)
}
