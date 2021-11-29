package no.nav.amt.tiltak.tiltak.deltaker.controllers

import no.nav.amt.tiltak.tiltak.controllers.dto.*
import no.nav.amt.tiltak.tiltak.deltaker.queries.DeltakerDetaljerDbo
import no.nav.amt.tiltak.tiltak.deltaker.queries.GetDeltakerDetaljerQuery
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class TiltakDeltakerPresentationService(
	private val template: NamedParameterJdbcTemplate
) {

	fun getDeltakerDetaljerById(deltakerId: UUID): TiltakDeltakerDetaljerDto {
		return GetDeltakerDetaljerQuery(template).query(deltakerId)?.toDto()
			?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Deltaker med id $deltakerId finnes ikke")
	}

	private fun DeltakerDetaljerDbo.toDto(): TiltakDeltakerDetaljerDto {
		val hasVeileder = veilederFornavn != null
			|| veilederEtternavn != null
			|| veilederEpost != null
			|| veilederTelefonnummer != null

		val veileder: NavVeilederDTO? = if (hasVeileder) {
			NavVeilederDTO(
				veilederFornavn,
				veilederEtternavn,
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
			navKontor = NavKontorDTO(
				"Test Kontor",
				"Test gata 1337, 1337 Oslo"
			),
			navVeileder = veileder,
			startdato = oppstartDato,
			sluttdato = sluttDato,
			status = status,
			tiltakInstans = TiltakInstansDto(
				id = tiltakInstansId,
				navn = tiltakInstansNavn,
				oppstartdato = tiltakInstansOppstartDato,
				sluttdato = tiltakInstansSluttDato,
				status = tiltakInstansStatus,
				tiltak = TiltakDto(
					tiltaksnavn = tiltakNavn,
					tiltakskode = tiltakKode
				)
			)
		)
	}

}
