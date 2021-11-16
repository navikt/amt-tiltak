package no.nav.amt.tools.arenakafkaproducer.producers

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.amt.tools.arenakafkaproducer.domain.dto.ArenaOpType
import no.nav.amt.tools.arenakafkaproducer.domain.dto.ArenaTiltaksgjennomforingDTO
import no.nav.amt.tools.arenakafkaproducer.domain.dto.ArenaTiltaksgjennomforingKafkaDTO
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.time.LocalDateTime

@Component
class TiltakGjennomforingProducer(
    kafkaProducer: KafkaProducerClientImpl<String, String>
) : Producer<ArenaTiltaksgjennomforingDTO, ArenaTiltaksgjennomforingKafkaDTO>(
    kafkaProducer = kafkaProducer,
    topic = "amt.aapen-arena-tiltakgjennomforingendret-v1-q2"
) {
    private var position = 0

    override fun wrap(entry: ArenaTiltaksgjennomforingDTO): ArenaTiltaksgjennomforingKafkaDTO {
        return ArenaTiltaksgjennomforingKafkaDTO(
            table = "ARENA_GOLDENGATE.TILTAKGJENNOMFORING",
            op_type = ArenaOpType.I,
            op_ts = LocalDateTime.now().format(operationTimestampFormatter),
            current_ts = LocalDateTime.now().toString(),
            pos = position++.toString(),
            after = entry,
            before = null
        )

    }

    override fun readFile(): List<ArenaTiltaksgjennomforingDTO> {
        val fileReader = FileInputStream("tools/arena-kafka-producer/data/arena_tiltak/TILTAKGJENNOMFORING.json")
        return objectMapper.readValue(fileReader)
    }


}
