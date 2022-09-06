package no.nav.amt.tiltak.test.integration.kafka

import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.GjennomforingPayload
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Operation
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Tiltak
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

object MessageCreator {

	fun getGjennomforing(
		operation: Operation,
		id: UUID = UUID.randomUUID(),
		tiltak: Tiltak = (Tiltak(UUID.randomUUID(), "DEFAULT_TILTAK", "Default tiltak")),
		virksomhetsnummer: String = "123456789",
		navn: String = "INTEGRATION_TEST_GJENNOMFORING",
		status: GjennomforingPayload.Status = GjennomforingPayload.Status.GJENNOMFORES,
		startDato: LocalDate? = LocalDate.now().minusDays(7),
		sluttDato: LocalDate? = LocalDate.now().plusDays(7),
		registrertDato: LocalDateTime = LocalDateTime.now().minusDays(14),
		fremmoteDato: LocalDateTime? = null,
		ansvarligNavEnhetId: String = "INTEGRATION_TEST_NAV_ENHET",
		opprettetAar: Int = LocalDateTime.now().minusDays(14).year,
		lopeNr: Int
	): String {
		return JsonUtils.toJsonString(
			MessageWrapper(
				transactionId = UUID.randomUUID().toString(),
				type = "GJENNOMFORING",
				timestamp = LocalDateTime.now(),
				operation = operation,
				payload = GjennomforingPayload(
					id = id,
					tiltak = tiltak,
					virksomhetsnummer = virksomhetsnummer,
					navn = navn,
					status = status,
					startDato = startDato,
					sluttDato = sluttDato,
					registrertDato = registrertDato,
					fremmoteDato = fremmoteDato,
					ansvarligNavEnhetId = ansvarligNavEnhetId,
					opprettetAar = opprettetAar,
					lopenr = lopeNr
				)
			)
		)
	}
}
