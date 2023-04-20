package no.nav.amt.tiltak.data_publisher.model

import no.nav.common.json.JsonUtils
import org.springframework.util.DigestUtils
import java.util.*

data class ArrangorPublishDto(
	val id: UUID,
	val orgNavn: String,
	val orgNr: String,
	val overordnetArrangor: UUID?,
	val deltakerlister: List<UUID>,
) {
	fun digest() = DigestUtils.md5DigestAsHex(JsonUtils.toJson(this).toByteArray())
}
