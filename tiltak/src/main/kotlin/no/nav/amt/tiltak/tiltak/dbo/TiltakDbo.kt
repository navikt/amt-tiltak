package no.nav.amt.tiltak.tiltak.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import java.time.LocalDateTime
import java.util.*

data class TiltakDbo(
    val internalId: Int,
    val externalId: UUID,
    val arenaId: String,
    val tiltaksleverandorInternalId: Int,
    val tiltaksleverandorExternalId: UUID,
    val navn: String,
    val type: String,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {

    fun toTiltak(): Tiltak {
        return Tiltak(
            id = externalId,
            tiltaksleverandorId = tiltaksleverandorExternalId,
            navn = navn,
            kode = type
        )
    }
}
