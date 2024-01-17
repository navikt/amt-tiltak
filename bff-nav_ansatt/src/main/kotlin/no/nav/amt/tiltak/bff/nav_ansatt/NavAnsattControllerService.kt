package no.nav.amt.tiltak.bff.nav_ansatt

import io.getunleash.Unleash
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
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class NavAnsattControllerService(
	private val endringsmeldingService: EndringsmeldingService,
	private val deltakerService: DeltakerService,
	private val gjennomforingService: GjennomforingService,
	private val vurderingService: VurderingService,
	private val unleashClient: Unleash,
) {
	private val log = LoggerFactory.getLogger(javaClass)

	fun hentEndringsmeldinger(gjennomforingId: UUID, tilgangTilSkjermede: Boolean): List<EndringsmeldingDto> {
		val endringsmeldinger = hentEndringsmeldingerForGjennomforing(gjennomforingId)
		val deltakerMap = deltakerService.hentDeltakerMap(endringsmeldinger.map { it.deltakerId }).filterValues { !it.harAdressebeskyttelse() }

		return endringsmeldinger.mapNotNull { endringsmelding ->
			val deltaker = getDeltaker(endringsmelding.deltakerId, deltakerMap) ?: return@mapNotNull null

			if(deltaker.erSkjermet && !tilgangTilSkjermede) {
				return@mapNotNull endringsmelding.toDto(DeltakerDto(erSkjermet = true))
			}

			return@mapNotNull endringsmelding.toDto(deltaker.toDto())
		}
	}

	fun hentMeldinger(gjennomforingId: UUID, tilgangTilSkjermede: Boolean): MeldingerFraArrangorResponse {
		val alleEndringsmeldinger = hentEndringsmeldingerForGjennomforing(gjennomforingId)
		val alleVurderinger = vurderingService.hentAktiveVurderingerForGjennomforing(gjennomforingId)

		val deltakerIder = mutableListOf<UUID>()
		deltakerIder.addAll(alleEndringsmeldinger.map { it.deltakerId })
		deltakerIder.addAll(alleVurderinger.map { it.deltakerId })

		val deltakerMap = deltakerService.hentDeltakerMap(deltakerIder.distinct()).filterValues { !it.harAdressebeskyttelse() }

		val endringsmeldinger = alleEndringsmeldinger.mapNotNull { endringsmelding ->
			val deltaker = getDeltaker(endringsmelding.deltakerId, deltakerMap) ?: return@mapNotNull null

			if(deltaker.erSkjermet && !tilgangTilSkjermede) {
				return@mapNotNull endringsmelding.toDto(DeltakerDto(erSkjermet = true))
			}

			return@mapNotNull endringsmelding.toDto(deltaker.toDto())
		}

		val vurderinger = alleVurderinger.mapNotNull { vurdering ->
			val deltaker = getDeltaker(vurdering.deltakerId, deltakerMap) ?: return@mapNotNull null

			if(deltaker.erSkjermet && !tilgangTilSkjermede) {
				return@mapNotNull vurdering.toDto(DeltakerDto(erSkjermet = true))
			}

			return@mapNotNull vurdering.toDto(deltaker.toDto())
		}
		return MeldingerFraArrangorResponse(endringsmeldinger, vurderinger)
	}

	fun hentGjennomforinger(gjennomforingIder: List<UUID>) : List<HentGjennomforingerDto> {
		return gjennomforingService.getGjennomforinger(gjennomforingIder).map { gjennomforing ->
			val aktiveEndringsmeldinger = hentEndringsmeldingerForGjennomforing(gjennomforing.id)
				.filter { it.status == Endringsmelding.Status.AKTIV }
			val harSkjermede = aktiveEndringsmeldinger.any { deltakerService.erSkjermet(it.deltakerId) }

			return@map gjennomforing.toDto(aktiveEndringsmeldinger.size, harSkjermede)
		}
	}


	private fun hentEndringsmeldingerForGjennomforing(gjennomforingId: UUID): List<Endringsmelding> {
		if (unleashClient.isEnabled("amt.enable-komet-deltakere")) {
			val gjennomforing = gjennomforingService.getGjennomforing(gjennomforingId)
			if (gjennomforing.tiltak.kode in listOf("ARBFORB")) {
				return emptyList()
			}
		}
		return endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId)
	}

	private fun getDeltaker(deltakerId: UUID, deltakerMap: Map<UUID, Deltaker>): Deltaker? {
		deltakerMap[deltakerId]?.let { return it }
		log.warn("Fant ikke deltaker med id $deltakerId")
		return null
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
	)
}
