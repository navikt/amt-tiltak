package no.nav.amt.tiltak.ingestors.aktor_v2_ingestor
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import no.nav.amt.tiltak.core.kafka.AktorV2Ingestor
import no.nav.amt.tiltak.core.port.BrukerService
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.springframework.stereotype.Component

@Component
class AktorV2IngestorImpl(
	schemaRegistryClient: SchemaRegistryClient,
	private val brukerService: BrukerService
	) : AktorV2Ingestor {
	private val deserializer = KafkaAvroDeserializer(schemaRegistryClient)

	override fun ingestKafkaRecord(key: String, value: ByteArray) {
		val response = deserializer.deserialize("", value) as GenericRecord
		ingest(response)
	}

	private fun ingest(genericRecord: GenericRecord) {

		val personIdenter = (genericRecord.get("identifikatorer") as GenericData.Array<GenericRecord>).map {
			val personIdent = it.get("idnummer").toString()
			val erGjeldende = (it.get("gjeldende")).toString().toBooleanStrict()

			PersonIdent(personIdent, erGjeldende)
		}

		brukerService.oppdaterPersonIdenter(
			personIdenter.find { it.erGjeldende }!!.ident,
			personIdenter.map { it.ident }
		)
	}
}

data class PersonIdent (
	val ident: String,
	val erGjeldende: Boolean
)
