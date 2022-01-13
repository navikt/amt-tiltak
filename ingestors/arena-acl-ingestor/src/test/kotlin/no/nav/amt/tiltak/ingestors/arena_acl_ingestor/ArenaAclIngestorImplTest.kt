package no.nav.amt.tiltak.ingestors.arena_acl_ingestor

import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor.DeltakerProcessor
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor.GjennomforingProcessor

class ArenaAclIngestorImplTest : StringSpec({

	val deltakerProcessor: DeltakerProcessor = mockk()

	val gjennomforingProcessor: GjennomforingProcessor = mockk()

	val ingestor = ArenaAclIngestorImpl(deltakerProcessor, gjennomforingProcessor)

	"ingestKafkaMessageValue should parse DELTAKER message" {
		val json = """
			{
			  "transactionId": "0a99b548-c831-47c4-87f6-760e9800b29c",
			  "type": "DELTAKER",
			  "timestamp": "2022-01-10T11:46:44.799Z",
			  "operation": "CREATED",
			  "payload": {
			    "id": "73dde1e1-78e1-4386-b26f-005b3d32ca6e",
			    "gjennomforingId": "f8a2a279-1cdc-4984-87d8-27418b9a9f0e",
			    "personIdent": "12345678900",
			    "startDato": "2022-01-10",
			    "sluttDato": "2022-01-10",
			    "status": "DELTAR",
			    "dagerPerUke": 5,
			    "prosentDeltid": 100.0,
			    "registrertDato": "2022-01-10T10:45:32.800Z"
			  }
			}
		""".trimIndent()

		every {
			deltakerProcessor.processMessage(any())
		} returns Unit

		ingestor.ingestKafkaMessageValue(json)

		verify(exactly = 1) {
			deltakerProcessor.processMessage(any())
		}

	}

	"ingestKafkaMessageValue should parse GJENNOMFORING message" {
		val json = """
			{
			  "transactionId": "b3b46fc2-ad90-4dbb-abfb-2b767318258b",
			  "type": "GJENNOMFORING",
			  "timestamp": "2022-01-10T11:46:44.799Z",
			  "operation": "CREATED",
			  "payload": {
			    "id": "ec3af220-65e9-4a39-928a-a73af2b16e46",
				"tiltak": {
					"id": "f692434a-694f-4be4-a87d-ff325ebc5833",
					"kode": "INDOPPFAG",
					"navn": "Oppfølging"
				},
				"virksomhetsnummer": "1235543432",
				"navn": "Tiltak hos Muligheter AS",
				"status": "GJENNOMFORES",
				"startDato": "2022-01-10",
				"sluttDato": "2022-01-12",
				"registrertDato": "2022-01-08T11:46:44.799Z",
				"fremmoteDato": "2022-01-10T11:46:44.799Z"
			  }
			}
		""".trimIndent()

		every {
			gjennomforingProcessor.processMessage(any())
		} returns Unit

		ingestor.ingestKafkaMessageValue(json)

		verify(exactly = 1) {
			gjennomforingProcessor.processMessage(any())
		}
	}

})

