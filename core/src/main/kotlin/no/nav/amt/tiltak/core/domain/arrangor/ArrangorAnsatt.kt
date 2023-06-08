package no.nav.amt.tiltak.core.domain.arrangor

import java.util.UUID

data class ArrangorAnsatt(
	val id: UUID,
	val personalia: PersonaliaDto,
	val arrangorer: List<TilknyttetArrangorDto>
) {
	data class PersonaliaDto(
		val personident: String,
		val personId: UUID?,
		val navn: Navn
	)

	data class Navn(
		val fornavn: String,
		val mellomnavn: String?,
		val etternavn: String
	)

	data class TilknyttetArrangorDto(
		val arrangorId: UUID,
		val arrangor: Arrangor,
		val overordnetArrangor: Arrangor?,
		val roller: List<AnsattRolle>,
		val veileder: List<VeilederDto>,
		val koordinator: List<UUID>
	)

	data class Arrangor(
		val id: UUID,
		val navn: String,
		val organisasjonsnummer: String
	)

	data class VeilederDto(
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
}
