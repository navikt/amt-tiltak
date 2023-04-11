package no.nav.amt.tiltak.data_publisher.model

import no.nav.common.json.JsonUtils
import org.springframework.util.DigestUtils
import java.time.LocalDateTime
import java.util.*

data class EndringsmeldingPublishDto(
	val id: UUID,
	val deltakerId: UUID,
	val utfortAvNavAnsattId: UUID?,
	val opprettetAvArrangorAnsattId: UUID,
	val utfortTidspunkt: LocalDateTime?,
	val status: String,
	val type: String,
	val innhold: String,
	val createdAt: LocalDateTime
) {
	fun digest() = DigestUtils.md5DigestAsHex(JsonUtils.toJson(this).toByteArray())
}
