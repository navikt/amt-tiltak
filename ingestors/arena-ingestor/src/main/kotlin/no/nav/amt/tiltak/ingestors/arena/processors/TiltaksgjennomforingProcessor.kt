package no.nav.amt.tiltak.ingestors.arena.processors

import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Virksomhet
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.core.port.TiltaksleverandorService
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.dto.ArenaTiltaksgjennomforing
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Component
class TiltaksgjennomforingProcessor(
	repository: ArenaDataRepository,
	private val tiltaksleverandorService: TiltaksleverandorService,
	private val tiltakService: TiltakService,
//    private val ords: ArenaOrdsProxyConnector
) : AbstractArenaProcessor(repository) {

	private val logger = LoggerFactory.getLogger(javaClass)
	private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

	override fun insert(data: ArenaData) {
		val newFields = jsonObject(data.after, ArenaTiltaksgjennomforing::class.java)

		val virksomhet = addVirksomhet(newFields)
		val tiltaksinstans = addTiltaksinstans(newFields.TILTAKSKODE, newFields)
	}

	override fun update(data: ArenaData) {
		TODO("Not yet implemented")
	}

	override fun delete(data: ArenaData) {
		TODO("Not yet implemented")
	}

	private fun addVirksomhet(fields: ArenaTiltaksgjennomforing): Virksomhet {
		TODO("Not yet implemented")
//		val virksomhetsnummer = ords.hentVirksomhetsnummer(fields.ARBGIV_ID_ARRANGOR.toString())
//		return tiltaksleverandorService.addVirksomhet(virksomhetsnummer)
	}

	private fun addTiltaksinstans(tiltakArenaId: String, fields: ArenaTiltaksgjennomforing): TiltakInstans {
		val arenaId = fields.TILTAKGJENNOMFORING_ID.toInt()

//        val unsavedTiltaksinstans = TiltakInstans(
//            id = null,
//            navn = fields.LOKALTNAVN!!,
//            status = null,
//            oppstartDato = stringToLocalDate(fields.DATO_FRA),
//            sluttDato = stringToLocalDate(fields.DATO_TIL),
//            registrertDato = stringToLocalDateTime(fields.REG_DATO),
//            fremmoteDato = datoKlokketidToLocalDateTime(fields.DATO_FREMMOTE, fields.KLOKKETID_FREMMOTE)
//        )
//
//        return tiltakService.addTiltaksinstans(arenaId, unsavedTiltaksinstans)
		TODO("Not yet implemented")
	}

	private fun stringToLocalDate(string: String?): LocalDate? {
		return if (string != null) LocalDate.parse(string, formatter) else null
	}

	private fun stringToLocalDateTime(string: String?): LocalDateTime? {
		return if (string != null) LocalDateTime.parse(string, formatter) else null
	}

	/**
	 * Har ingen eksempler på hvordan klokketid ser ut, så må vente med det til vi har et eksempel.
	 */
	private fun datoKlokketidToLocalDateTime(dato: String?, klokketid: String?): LocalDateTime? {
		if (dato == null) {
			return null
		}

		val date = LocalDate.parse(dato, formatter)

		val time = if (klokketid != null) {
			logger.warn("Det er ikke implementert en handler for klokketid, pattern: $klokketid")
			LocalTime.MIDNIGHT
		} else LocalTime.MIDNIGHT

		return LocalDateTime.of(date, time)
	}
}
