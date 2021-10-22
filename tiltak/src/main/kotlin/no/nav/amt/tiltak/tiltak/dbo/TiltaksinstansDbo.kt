package no.nav.amt.tiltak.tiltak.dbo

import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class TiltaksinstansDbo(
	val internalId: Int,
	val externalId: UUID,
	val arenaId: Int,
	val tiltaksleverandorInternalId: Int,
	val tiltaksleverandorExternalId: UUID,
	val tiltakInternalId: Int,
	val tiltakExternalId: UUID,
	val navn: String,
	val status: TiltakInstans.Status?,
	val oppstartDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrertDato: LocalDateTime?,
	val fremmoteDato: LocalDateTime?
) {

	fun toTiltaksinstans(): TiltakInstans {
		return TiltakInstans(
			id = externalId,
			tiltakId = tiltakExternalId,
			tiltaksarrangorId = tiltaksleverandorExternalId,
			navn = navn,
			status = status,
			oppstartDato = oppstartDato,
			sluttDato = sluttDato,
			registrertDato = registrertDato,
			fremmoteDato = fremmoteDato
		)
	}
}
