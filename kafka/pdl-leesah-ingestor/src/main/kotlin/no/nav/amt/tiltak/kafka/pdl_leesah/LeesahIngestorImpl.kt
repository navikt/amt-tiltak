package no.nav.amt.tiltak.kafka.pdl_leesah

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.kafka.LeesahIngestor
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.log.SecureLog.secureLog
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.avro.util.Utf8
import org.springframework.stereotype.Component

@Component
class LeesahIngestorImpl(
	private val deltakerService: DeltakerService,
	private val brukerService: BrukerService,
	schemaRegistryClient: SchemaRegistryClient,
) : LeesahIngestor {
	private val deserializer = KafkaAvroDeserializer(schemaRegistryClient)

	override fun ingestKafkaRecord(aktorId: String, value: ByteArray) {
		val leesahResponse = deserializer.deserialize("", value)
		when (leesahResponse) {
			is GenericRecord -> ingestGenericRecord(leesahResponse)
			else -> throw IllegalArgumentException("Ugyldig avro melding fra leesah")
		}
	}

	private fun ingestGenericRecord(genericRecord: GenericRecord) {
		val personIdenter = (genericRecord.get("personidenter") as GenericData.Array<Utf8>).map { it.toString() }
		val addressebeskyttelse = genericRecord.get("adressebeskyttelse") as GenericData.Record?
		val gradering = (addressebeskyttelse?.get("gradering") as GenericData.EnumSymbol?)?.toString()

		val erAddressebeskyttet = erAddressebeskyttet(gradering)

		if (erAddressebeskyttet) {
			val deltakere = personIdenter.flatMap { deltakerService.hentDeltakereMedPersonIdent(it) }
			if (deltakere.isNotEmpty()) {
				secureLog.info("Sletter addressebeskyttet deltaker med personidenter: $personIdenter")
			}

			deltakere.forEach {
				deltakerService.slettDeltaker(it.id)
				secureLog.info("Har slettet deltakere personidenter: ${it.personIdent}")

			}
		}

		val navn = JsonUtils.fromJsonString<Navn>(genericRecord.get("navn").toString())
		personIdenter.forEach { personIdent ->
			brukerService.updateBrukerByPersonIdent(
				personIdent = personIdent,
				fornavn = navn.fornavn,
				mellomnavn = navn.mellomnavn,
				etternavn =  navn.etternavn
			)
		}

	}

	private fun erAddressebeskyttet(gradering: String?): Boolean {
		return gradering != null && gradering != "UGRADERT"
	}

	private data class Navn(
		val fornavn: String,
		val mellomnavn: String?,
		val etternavn: String,
	)
}
