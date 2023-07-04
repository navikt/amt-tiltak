package no.nav.amt.tiltak.kafka.ansatt_ingestor.model

import no.nav.amt.tiltak.core.domain.arrangor.ArrangorAnsatt
import java.util.UUID

data class AnsattDto(
	val id: UUID,
	val source: String?,
	val personalia: ArrangorAnsatt.PersonaliaDto,
	val arrangorer: List<TilknyttetArrangor>
)

data class Arrangor(
	val id: UUID,
	val navn: String,
	val organisasjonsnummer: String,
	val overordnetArrangorId: UUID?
)

data class TilknyttetArrangor(
	val arrangorId: UUID,
	val arrangor: Arrangor?,
	val overordnetArrangor: Arrangor?,
	val roller: List<ArrangorAnsatt.AnsattRolle>,
	val veileder: List<ArrangorAnsatt.VeilederDto>,
	val koordinator: List<UUID>
)
