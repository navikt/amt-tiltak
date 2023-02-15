package no.nav.amt.tiltak.ingestors.aktor_v2_ingestor
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import no.nav.amt.tiltak.core.domain.tiltak.IdentType
import no.nav.amt.tiltak.core.kafka.AktorV2Ingestor
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.log.SecureLog.secureLog
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AktorV2IngestorImpl(
	schemaRegistryClient: SchemaRegistryClient,
	private val brukerService: BrukerService
	) : AktorV2Ingestor {
	private val deserializer = KafkaAvroDeserializer(schemaRegistryClient)

	private val log = LoggerFactory.getLogger(javaClass)
	override fun ingestKafkaRecord(key: String, value: ByteArray?) {
		if(value == null) {
			secureLog.error("Fikk tombstone for record med key=$key.")
			log.error("Fikk tombstone for kafka record. Se secure logs for key")
			throw IllegalStateException("Fikk tombstone for kafka record. Se secure logs for key")
		}

		val response = deserializer.deserialize("", value) as GenericRecord
		ingest(response)
	}

	private fun ingest(genericRecord: GenericRecord) {

		val personIdenter = (genericRecord.get("identifikatorer") as GenericData.Array<GenericRecord>).map {
			val personIdent = it.get("idnummer").toString()
			val type = when (val typeString = it.get("type").toString()) {
				"FOLKEREGISTERIDENT" -> Type.FOLKEREGISTERIDENT
				"AKTORID" -> Type.AKTORID
				"NPID" -> Type.NPID
				else -> throw IllegalStateException("Har mottatt ident med ukjent type $typeString")
			}

			val erGjeldende = (it.get("gjeldende")).toString().toBooleanStrict()

			PersonIdent(personIdent, type, erGjeldende)
		}

		val gjeldendeIdenter = personIdenter
			.filter { it.identType == Type.FOLKEREGISTERIDENT || it.identType == Type.NPID }
			.filter { it.erGjeldende }

		if (gjeldendeIdenter.size != 1) {
			secureLog.error("AktorV2 ingestor mottok bruker med ${gjeldendeIdenter.size} personident(er): $personIdenter")
			log.error("AktorV2 ingestor mottok bruker med ${gjeldendeIdenter.size} gjeldende ident(er). Se secure logs for detaljer")
			throw IllegalStateException("Kan ikke ingeste bruker med ${gjeldendeIdenter.size} gjeldende ident(er)")
		}

		brukerService.oppdaterPersonIdenter(
			gjeldendeIdenter.first().ident,
			gjeldendeIdenter.first().identType.toModel(),
			personIdenter.filter { !it.erGjeldende }.map { it.ident }
		)
	}
}

data class PersonIdent (
	val ident: String,
	val identType: Type,
	val erGjeldende: Boolean
)

enum class Type {
	FOLKEREGISTERIDENT, AKTORID, NPID;

	fun toModel(): IdentType {
		return when (this) {
			FOLKEREGISTERIDENT -> IdentType.FOLKEREGISTERIDENT
			NPID -> IdentType.NPID
			else -> throw IllegalStateException("type $this kan ikke konverteres til domenemodell")
		}
	}
}
