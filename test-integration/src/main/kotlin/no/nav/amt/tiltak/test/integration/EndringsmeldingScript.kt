package no.nav.amt.tiltak.test.integration

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.inputs.EndringsmeldingInput
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.io.File
import java.time.LocalDate
import java.time.ZonedDateTime

import java.io.IOException
import java.text.ParseException
import java.time.format.DateTimeFormatter
import java.util.*


class CustomJsonDateDeserializer(): JsonDeserializer<ZonedDateTime>(){
    @Throws(IOException::class, JsonProcessingException::class)
	override fun deserialize(jsonParser: JsonParser, ctx: DeserializationContext): ZonedDateTime {
		var  format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.n z")
		var  date: kotlin.String = jsonParser.getText()
		try {
			return ZonedDateTime.parse(date, format)
		} catch ( e: ParseException){
			throw RuntimeException(e)
		}
	}
}

data class GammelEndringsmelding(
	val id: UUID,
	val deltaker_id: UUID,
	val start_dato: LocalDate?,
	val ferdiggjort_av_nav_ansatt_id: UUID?,
	val aktiv: Boolean,
	val opprettet_av_arrangor_ansatt_id: UUID,
	@JsonDeserialize(using = CustomJsonDateDeserializer::class)
	val created_at: ZonedDateTime,
	@JsonDeserialize(using = CustomJsonDateDeserializer::class)
	val modified_at: ZonedDateTime,
	@JsonDeserialize(using = CustomJsonDateDeserializer::class)
	val ferdiggjort_tidspunkt: ZonedDateTime?,
	val slutt_dato: LocalDate?
)

fun main() {
	val json = File("/Users/tormod/endringsmelding.json").readText()
	val endringmeldinger = JsonUtils.objectMapper.readValue(json, object : TypeReference<List<GammelEndringsmelding>>() {})

	val config = HikariConfig()

	config.jdbcUrl = "jdbc:postgresql://localhost:20001/amt-tiltak"
	config.username = "tormod.fossum@nav.no"
	config.password = ""
	config.maximumPoolSize = 3
	config.minimumIdle = 1

	val dataSource = HikariDataSource(config)

	val testRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

	endringmeldinger.forEach {
		val status = if (it.aktiv) Endringsmelding.Status.AKTIV else if (it.ferdiggjort_av_nav_ansatt_id != null) Endringsmelding.Status.UTFORT else Endringsmelding.Status.UTDATERT
		val type = if (it.start_dato != null) "ENDRE_OPPSTARTSDATO" else "FORLENG_DELTAKELSE"
		val innhold = if (type == "ENDRE_OPPSTARTSDATO") """{"oppstartsdato": "${it.start_dato!!}"}""" else """{"sluttdato": "${it.slutt_dato!!}"}"""
		println(innhold)
		testRepository.insertEndringsmelding(
			EndringsmeldingInput(
				id = it.id,
				deltakerId = it.deltaker_id,
				createdAt = it.created_at,
				modifiedAt = it.modified_at,
				utfortAvNavAnsattId = it.ferdiggjort_av_nav_ansatt_id,
				utfortTidspunkt = it.ferdiggjort_tidspunkt,
				opprettetAvArrangorAnsattId = it.opprettet_av_arrangor_ansatt_id,
				status = status,
				type = type,
				innhold = innhold,
			)
		)
	}


	//println(endringmeldinger)
}
