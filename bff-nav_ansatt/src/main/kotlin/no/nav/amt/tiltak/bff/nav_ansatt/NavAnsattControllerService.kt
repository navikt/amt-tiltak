package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.bff.nav_ansatt.dto.*
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
	)

	private fun Endringsmelding.toDto(deltakerDto: DeltakerDto) = EndringsmeldingDto(
		id = id,
		deltaker = deltakerDto,
		status = status.toDto(),
		innhold = innhold.toDto(),
		opprettetDato = opprettet,
	)

	private fun Deltaker.toDto() = DeltakerDto(
		fornavn = fornavn,
		mellomnavn = mellomnavn,
		etternavn = etternavn,
		fodselsnummer = personIdent,
		erSkjermet = erSkjermet,
	)
}
