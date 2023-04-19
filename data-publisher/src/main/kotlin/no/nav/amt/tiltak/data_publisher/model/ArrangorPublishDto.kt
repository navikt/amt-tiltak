package no.nav.amt.tiltak.data_publisher.model

import no.nav.common.json.JsonUtils
import org.springframework.util.DigestUtils
import java.util.*

data class ArrangorPublishDto(
	val id: UUID,
	val organisasjon: OrganisasjonDto,
	val overordnetOrganisasjon: OrganisasjonDto?,
	val deltakerlister: List<UUID>,
) {
	fun digest() = DigestUtils.md5DigestAsHex(JsonUtils.toJson(this).toByteArray())
}

data class OrganisasjonDto(
	val nummer: String,
	val navn: String
)
