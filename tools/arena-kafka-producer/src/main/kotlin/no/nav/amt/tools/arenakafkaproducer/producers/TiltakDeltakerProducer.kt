package no.nav.amt.tools.arenakafkaproducer.producers

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.amt.tools.arenakafkaproducer.domain.dto.ArenaOpType
import no.nav.amt.tools.arenakafkaproducer.domain.dto.ArenaTiltakDeltakerDTO
import no.nav.amt.tools.arenakafkaproducer.domain.dto.ArenaTiltakDeltakerKafkaDTO
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.time.LocalDateTime

@Component
class TiltakDeltakerProducer(
    kafkaProducer: KafkaProducerClientImpl<String, String>
) : Producer<ArenaTiltakDeltakerDTO, ArenaTiltakDeltakerKafkaDTO>(
    kafkaProducer = kafkaProducer,
    topic = "amt.aapen-arena-tiltakdeltakerendret-v1-q2"
) {
    private var position = 0

    override fun wrap(entry: ArenaTiltakDeltakerDTO): ArenaTiltakDeltakerKafkaDTO {
        return ArenaTiltakDeltakerKafkaDTO(
            table = "ARENA_GOLDENGATE.TILTAKDELTAKER",
            op_type = ArenaOpType.I,
            op_ts = LocalDateTime.now().format(operationTimestampFormatter),
            current_ts = LocalDateTime.now().toString(),
            pos = position++.toString(),
            after = entry,
            before = null
        )
    }

    override fun readFile(): List<ArenaTiltakDeltakerDTO> {
        val fileReader = FileInputStream("tools/arena-kafka-producer/data/arena_tiltak/TILTAKDELTAKER.json")
        return objectMapper.readValue(fileReader)
    }


}
