package no.nav.amt.tiltak.data_publisher.model

import no.nav.common.json.JsonUtils
import org.springframework.util.DigestUtils
import java.util.*

data class ArrangorAnsattPublishDto(
	val id: UUID,
	val personalia: PersonPublishDto,
	val arrangorer: List<TilknyttetArrangor>,
) {
	fun digest() = DigestUtils.md5DigestAsHex(JsonUtils.toJson(this).toByteArray())
}

data class PersonPublishDto(
	val personligIdent: String,
	val navn: Navn
)

data class Navn(
	val fornavn: String,
	val mellomnevn: String?,
	val etternavn: String
)

data class TilknyttetArrangor(
	val arrangorId: UUID,
	val roller: List<AnsattRolle>,
	val veileder: List<Veileder>,
	val koordinator: List<UUID>
)

data class Veileder(
	val deltakerId: UUID,
	val type: VeilederType
)

enum class AnsattRolle {
	KOORDINATOR,
	VEILEDER
}

enum class VeilederType {
	VEILEDER,
	MEDVEILEDER
}
