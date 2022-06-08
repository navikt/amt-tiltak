package no.nav.amt.tiltak.deltaker.controllers

import no.nav.amt.tiltak.core.port.SkjermetPersonService
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDetaljerDbo
import no.nav.amt.tiltak.deltaker.repositories.GetDeltakerDetaljerQuery
import no.nav.amt.tiltak.tiltak.dto.*
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
open class TiltakDeltakerPresentationService(
	private val template: NamedParameterJdbcTemplate,
	private val skjermetPersonService: SkjermetPersonService
) {

	open fun getDeltakerDetaljerById(deltakerId: UUID): TiltakDeltakerDetaljerDto {
		val deltaker = GetDeltakerDetaljerQuery(template).query(deltakerId)
			?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Deltaker med id $deltakerId finnes ikke")

		val erSkjermet = skjermetPersonService.erSkjermet(deltaker.fodselsnummer)

		return deltaker.toDto(erSkjermet)
	}

	private fun DeltakerDetaljerDbo.toDto(erSkjermet: Boolean): TiltakDeltakerDetaljerDto {
		val hasVeileder = veilederNavn != null

		val veileder: NavVeilederDto? = if (hasVeileder) {
			NavVeilederDto(
				veilederNavn!!,
				veilederTelefonnummer,
				veilederEpost
			)
		} else null

		return TiltakDeltakerDetaljerDto(
			id = deltakerId,
			fornavn = fornavn,
			mellomnavn = mellomnavn,
			etternavn = etternavn,
			fodselsnummer = fodselsnummer,
			telefonnummer = telefonnummer,
			epost = epost,
			navEnhet = navEnhetNavn?.let { NavEnhetDto(it) },
			navVeileder = veileder,
			erSkjermetPerson = erSkjermet,
			startDato = startDato,
			sluttDato = sluttDato,
			registrertDato = registrertDato,
			status = DeltakerStatusDto(type = status, endretDato = statusOpprettet),
			gjennomforing = GjennomforingDto(
				id = gjennomforingId,
				navn = gjennomforingNavn,
				startDato = gjennomforingStartDato,
				sluttDato = gjennomforingSluttDato,
				status = gjennomforingStatus,
				tiltak = TiltakDto(
					tiltaksnavn = tiltakNavn,
					tiltakskode = tiltakKode
				),
				arrangor = ArrangorDto(
					virksomhetNavn = virksomhetNavn,
					organisasjonNavn = organisasjonNavn
				)
			)
		)
	}

}
