package no.nav.amt.tiltak.kafka.nav_bruker_ingestor

import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.kafka.NavBrukerIngestor
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.NavEnhetService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class NavBrukerIngestorImpl(
	val brukerService: BrukerService,
	val navEnhetService: NavEnhetService,
	val deltakerService: DeltakerService,
	val navAnsattService: NavAnsattService,
) : NavBrukerIngestor {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun ingest(key: String, value: String?) {
		val personId = UUID.fromString(key)
		val brukerDto = value?.let { fromJsonString<NavBrukerDto>(it) }
			?: return handleTombstone(personId)

		val lagretBruker = brukerService.get(personId)

		brukerDto.navEnhet?.toModel()?.let { navEnhetService.upsert(it) }
		brukerDto.navVeilederId?.let { navAnsattService.opprettNavAnsattHvisIkkeFinnes(it) }

		brukerService.upsert(brukerDto.toModel())

		val deltakere = deltakerService.hentDeltakereMedPersonId(personId)

		// Hvis vi ikke har lagret bruker, bør vi ikke ha deltakere, men pga samtidighetsproblemer ved opprettelse
		// av deltaker og bruker så kan det skje, og da hender det at denne skriver utdatert deltakerdata til topic.
		// Det er kun endring i personident som skal trigge oppdatering på både deltaker-v1 og deltaker-v2
		if (lagretBruker != null && lagretBruker.personIdent != brukerDto.personident) {
			deltakere.forEach {
				deltakerService.publiserDeltakerPaKafka(
					deltakerId = it.id,
					endretDato = LocalDateTime.now(),
				)
			}
		} else if (harEndredePersonopplysninger(lagretBruker, brukerDto)) {
			deltakere.forEach { deltakerService.publiserDeltakerPaDeltakerV2Kafka(it.id) }
		}

		log.info("Håndterte melding for nav-bruker $key")
	}

	private fun handleTombstone(personId: UUID) {
		log.info("Mottok tombstone for nav-bruker $personId - sletter bruker og deltakere")
		val deltakere = deltakerService.hentDeltakereMedPersonId(personId)

		deltakere.forEach { deltakerService.slettDeltaker(it.id) }

		brukerService.slettBruker(personId)
	}

	private fun harEndredePersonopplysninger(bruker: Bruker?, brukerDto: NavBrukerDto): Boolean {
		if (bruker == null) {
			return false
		} else {
			return brukerDto.toModel() != bruker
		}
	}
}
