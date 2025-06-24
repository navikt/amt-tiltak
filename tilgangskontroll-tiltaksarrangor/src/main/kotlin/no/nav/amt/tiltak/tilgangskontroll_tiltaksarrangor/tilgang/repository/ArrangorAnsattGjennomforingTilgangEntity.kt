package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang.repository

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime
import java.util.UUID

@Table("arrangor_ansatt_gjennomforing_tilgang")
data class ArrangorAnsattGjennomforingTilgangEntity(
	@Id
	val id: UUID,

	@Column("ansatt_id")
	val ansattId: UUID,

	@Column
	val gjennomforingId: UUID,

	@Column("gyldig_fra")
	val gyldigFra: OffsetDateTime,

	@Column("gyldig_til")
	val gyldigTil: OffsetDateTime,

	@Column("created_at")
	val createdAt: OffsetDateTime = OffsetDateTime.now()
)
