package no.nav.amt.tiltak.ingestors.arena.processors

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Tiltaksleverandor
import no.nav.amt.tiltak.core.port.ArenaOrdsProxyConnector
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.core.port.TiltaksleverandorService
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.dto.ArenaTiltaksgjennomforing
import no.nav.amt.tiltak.ingestors.arena.exceptions.DependencyNotIngestedException
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
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
	private val ords: ArenaOrdsProxyConnector
) : AbstractArenaProcessor(repository) {

	private val logger = LoggerFactory.getLogger(javaClass)
	private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

	override fun insert(data: ArenaData) {
		val newFields = jsonObject(data.after, ArenaTiltaksgjennomforing::class.java)

		val tiltak = tiltakService.getTiltakFromArenaId(newFields.TILTAKSKODE)
			?: throw DependencyNotIngestedException("Tiltak med ID ${newFields.TILTAKSKODE} er ikke ingested.")

		val tiltaksleverandor = addTiltaksleverandor(newFields)

		tiltakService.addUpdateTiltaksinstans(
			arenaId = newFields.TILTAKGJENNOMFORING_ID.toInt(),
			tiltakId = tiltak.id,
			tiltaksleverandorId = tiltaksleverandor.id,
			navn = newFields.LOKALTNAVN
				?: throw DataIntegrityViolationException("Forventet at LOKALTNAVN ikke er null"),
			status = null,
			oppstartDato = stringToLocalDate(newFields.DATO_FRA),
			sluttDato = stringToLocalDate(newFields.DATO_TIL),
			registrertDato = stringToLocalDateTime(newFields.REG_DATO),
			fremmoteDato = datoKlokketidToLocalDateTime(newFields.DATO_FREMMOTE, newFields.KLOKKETID_FREMMOTE)
		)
	}

	override fun update(data: ArenaData) {
		val newFields = jsonObject(data.after, ArenaTiltaksgjennomforing::class.java)

		val virksomhetsnummer = ords.hentVirksomhetsnummer(newFields.ARBGIV_ID_ARRANGOR.toString())

		val tiltaksleverandor = tiltaksleverandorService.getTiltaksleverandorByVirksomhetsnummer(virksomhetsnummer)
			?: throw DependencyNotIngestedException("Tiltaksleverandør med virksomhetsnummer $virksomhetsnummer er ikke ingested enda.")

		val tiltak = tiltakService.getTiltakFromArenaId(newFields.TILTAKSKODE)
			?: throw DependencyNotIngestedException("Tilktak med ArenaId $virksomhetsnummer er ikke ingested enda.")


		tiltakService.addUpdateTiltaksinstans(
			arenaId = newFields.TILTAKGJENNOMFORING_ID.toInt(),
			tiltakId = tiltak.id,
			tiltaksleverandorId = tiltaksleverandor.id,
			navn = newFields.LOKALTNAVN
				?: throw DataIntegrityViolationException("Forventet at LOKALTNAVN ikke er null"),
			status = null,
			oppstartDato = stringToLocalDate(newFields.DATO_FRA),
			sluttDato = stringToLocalDate(newFields.DATO_TIL),
			registrertDato = stringToLocalDateTime(newFields.REG_DATO),
			fremmoteDato = datoKlokketidToLocalDateTime(newFields.DATO_FREMMOTE, newFields.KLOKKETID_FREMMOTE)
		)

	}

	override fun delete(data: ArenaData) {
		TODO("Not yet implemented")
	}

	private fun insertUpdate(data: ArenaData) {

	}

	private fun addTiltaksleverandor(fields: ArenaTiltaksgjennomforing): Tiltaksleverandor {
		val virksomhetsnummer = ords.hentVirksomhetsnummer(fields.ARBGIV_ID_ARRANGOR.toString())
		return tiltaksleverandorService.addTiltaksleverandor(virksomhetsnummer)
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
