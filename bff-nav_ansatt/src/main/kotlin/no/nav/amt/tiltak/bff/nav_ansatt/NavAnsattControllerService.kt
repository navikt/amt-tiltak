package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.bff.nav_ansatt.dto.DeltakerDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.EndringsmeldingDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.HentGjennomforingerDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.TiltakDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.VurderingDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.toDto
import no.nav.amt.tiltak.bff.nav_ansatt.response.MeldingerFraArrangorResponse
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Vurdering
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.VurderingService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class NavAnsattControllerService(
	private val endringsmeldingService: EndringsmeldingService,
	private val deltakerService: DeltakerService,
	private val gjennomforingService: GjennomforingService,
	private val vurderingService: VurderingService
) {

	fun hentEndringsmeldinger (gjennomforingId: UUID, tilgangTilSkjermede: Boolean): List<EndringsmeldingDto> {
		val endringsmeldinger = endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId)
		val deltakerMap = deltakerService.hentDeltakerMap(endringsmeldinger.map { it.deltakerId })

		return endringsmeldinger.map { endringsmelding ->
			val deltaker = deltakerMap[endringsmelding.deltakerId]
				?: throw NoSuchElementException("Fant ikke deltaker med id ${endringsmelding.deltakerId}")

			if(deltaker.erSkjermet && !tilgangTilSkjermede) {
				return@map endringsmelding.toDto(DeltakerDto(erSkjermet = true))
			}

			return@map endringsmelding.toDto(deltaker.toDto())
		}
	}

	fun hentMeldinger(gjennomforingId: UUID, tilgangTilSkjermede: Boolean): MeldingerFraArrangorResponse {
		val alleEndringsmeldinger = endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId)
		val alleVurderinger = vurderingService.hentAktiveVurderingerForGjennomforing(gjennomforingId)

		val deltakerIder = mutableListOf<UUID>()
		deltakerIder.addAll(alleEndringsmeldinger.map { it.deltakerId })
		deltakerIder.addAll(alleVurderinger.map { it.deltakerId })

		val deltakerMap = deltakerService.hentDeltakerMap(deltakerIder.distinct())

		val endringsmeldinger = alleEndringsmeldinger.map { endringsmelding ->
			val deltaker = deltakerMap[endringsmelding.deltakerId]
				?: throw java.util.NoSuchElementException("Fant ikke deltaker med id ${endringsmelding.deltakerId}")

			if(deltaker.erSkjermet && !tilgangTilSkjermede) {
				return@map endringsmelding.toDto(DeltakerDto(erSkjermet = true))
			}

			return@map endringsmelding.toDto(deltaker.toDto())
		}

		val vurderinger = alleVurderinger.map { vurdering ->
			val deltaker = deltakerMap[vurdering.deltakerId]
				?: throw java.util.NoSuchElementException("Fant ikke deltaker med id ${vurdering.deltakerId}")

			if(deltaker.erSkjermet && !tilgangTilSkjermede) {
				return@map vurdering.toDto(DeltakerDto(erSkjermet = true))
			}

			return@map vurdering.toDto(deltaker.toDto())
		}
		return MeldingerFraArrangorResponse(endringsmeldinger, vurderinger)
	}

	fun hentGjennomforinger(gjennomforingIder: List<UUID>) : List<HentGjennomforingerDto> {
		return gjennomforingService.getGjennomforinger(gjennomforingIder).map { gjennomforing ->
			val aktiveEndringsmeldinger = endringsmeldingService.hentAktiveEndringsmeldingerForGjennomforing(gjennomforing.id)
			val harSkjermede = aktiveEndringsmeldinger.any { deltakerService.erSkjermet(it.deltakerId) }

			return@map gjennomforing.toDto(aktiveEndringsmeldinger.size, harSkjermede)
		}
	}

	private fun Gjennomforing.toDto (antallAktiveEndringsmeldinger: Int, harSkjermede: Boolean) = HentGjennomforingerDto(
		id = id,
		navn = navn,
		lopenr = lopenr,
		opprettetAar = opprettetAar,
		arrangorNavn = arrangor.overordnetEnhetNavn ?: arrangor.navn,
		antallAktiveEndringsmeldinger = antallAktiveEndringsmeldinger,
		harSkjermedeDeltakere = harSkjermede,
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
			Endringsmelding.Type.DELTAKER_ER_AKTUELL -> EndringsmeldingDto.Type.DELTAKER_ER_AKTUELL
			Endringsmelding.Type.ENDRE_SLUTTDATO -> EndringsmeldingDto.Type.ENDRE_SLUTTDATO
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
	)
}
