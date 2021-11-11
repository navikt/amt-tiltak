package no.nav.amt.tools.arenakafkaproducer.producers

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.amt.tools.arenakafkaproducer.domain.dto.ArenaOpType
import no.nav.amt.tools.arenakafkaproducer.domain.dto.ArenaTiltakDTO
import no.nav.amt.tools.arenakafkaproducer.domain.dto.ArenaTiltakKafkaDto
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.time.LocalDateTime

@Component
class TiltakProducer(
    kafkaProducer: KafkaProducerClientImpl<String, String>
) : Producer<ArenaTiltakDTO, ArenaTiltakKafkaDto>(
    kafkaProducer = kafkaProducer,
    topic = "amt.aapen-arena-tiltakendret-v1-q2",
) {
    private var position = 0

    override fun wrap(entry: ArenaTiltakDTO): ArenaTiltakKafkaDto {
        return ArenaTiltakKafkaDto(
            table = "ARENA_GOLDENGATE.TILTAK",
            op_type = ArenaOpType.I,
            op_ts = LocalDateTime.now().format(operationTimestampFormatter),
            current_ts = LocalDateTime.now().toString(),
            pos = position++.toString(),
            after = entry,
            before = null
        )
    }

    override fun readFile(): List<ArenaTiltakDTO> {
        val fileReader = FileInputStream("tools/arena-kafka-producer/data/arena_tiltak/TILTAK.json")
        return objectMapper.readValue(fileReader)
    }

}
