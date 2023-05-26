package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.bff.nav_ansatt.dto.DeltakerDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.EndringsmeldingDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.HentGjennomforingerDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.TiltakDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.toDto
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.core.port.GjennomforingService
import org.springframework.stereotype.Service
import java.util.*

@Service
class NavAnsattControllerService(
	private val endringsmeldingService: EndringsmeldingService,
	private val deltakerService: DeltakerService,
	private val gjennomforingService: GjennomforingService
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
			Endringsmelding.Type.TILBY_PLASS -> EndringsmeldingDto.Type.TILBY_PLASS
			Endringsmelding.Type.ENDRE_SLUTTDATO -> EndringsmeldingDto.Type.ENDRE_SLUTTDATO
		}
	}

	private fun Deltaker.toDto() = DeltakerDto(
		fornavn = fornavn,
		mellomnavn = mellomnavn,
		etternavn = etternavn,
		fodselsnummer = personIdent,
		erSkjermet = erSkjermet,
	)
}
