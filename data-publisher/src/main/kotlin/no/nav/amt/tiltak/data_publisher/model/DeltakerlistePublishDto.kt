package no.nav.amt.tiltak.data_publisher.model

import no.nav.common.json.JsonUtils
import org.springframework.util.DigestUtils
import java.time.LocalDate
import java.util.UUID

data class DeltakerlistePublishDto(
	val id: UUID,
	val navn: String,
	val status: DeltakerlisteStatus,
	val arrangor: DeltakerlisteArrangorDto,
	val tiltak: TiltakDto,
	val startDato: LocalDate,
	val sluttDato: LocalDate?,
	val erKurs: Boolean
) {
	fun digest() = DigestUtils.md5DigestAsHex(JsonUtils.toJson(this).toByteArray())
}

data class DeltakerlisteArrangorDto(
	val id: UUID,
	val organisasjonsnummer: String,
	val navn: String
)

data class TiltakDto(
	val navn: String,
	val type: String,
)

enum class DeltakerlisteStatus {
	PLANLAGT, GJENNOMFORES, AVSLUTTET
}
