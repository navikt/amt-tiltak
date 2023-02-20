package no.nav.amt.tiltak.kafka.config

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.atomic.AtomicInteger

@Configuration
open class KafkaMetrics(
	registry: MeterRegistry,
	private val jdbcTemplate: NamedParameterJdbcTemplate
) {
	private val antallFeiledeKafkameldingerGauge = registry.gauge(
		"amt_tiltak_antall_feilede_kafkameldinger", AtomicInteger(0)
	)

	@Scheduled(cron = "0 */5 * ? * *")
	open fun updateMetrics() {
		val antallFeiledeMeldinger = getAntallFeiledeKafkameldinger()

		antallFeiledeKafkameldingerGauge?.set(antallFeiledeMeldinger)
	}

	private fun getAntallFeiledeKafkameldinger(): Int {
		val sql = """
			SELECT count(*) as antall from kafka_consumer_record;
		""".trimIndent()

		return jdbcTemplate.query(sql) { rs, _ -> rs.getInt("antall") }.first()
	}
}
