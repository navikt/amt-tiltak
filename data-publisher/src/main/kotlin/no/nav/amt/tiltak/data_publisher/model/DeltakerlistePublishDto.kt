package no.nav.amt.tiltak.data_publisher.model

import no.nav.common.json.JsonUtils
import org.springframework.util.DigestUtils
import java.time.LocalDate
import java.util.*

data class DeltakerlistePublishDto(
	val id: UUID,
	val arrangorId: UUID,
	val type: String,
	val navn: String,
	val status: String,
	val tiltakNavn: String,
	val startDato: LocalDate,
	val sluttDato: LocalDate?
) {
	fun digest() = DigestUtils.md5DigestAsHex(JsonUtils.toJson(this).toByteArray())
}
