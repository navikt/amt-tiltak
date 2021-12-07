package no.nav.amt.tiltak.ingestors.arena.domain

import no.nav.amt.tiltak.ingestors.arena.dto.ArenaOpType

internal enum class OperationType {
    INSERT,
    UPDATE,
    DELETE;

    companion object {
        fun fromArena(string: ArenaOpType): OperationType {
            return when (string) {
                ArenaOpType.I -> INSERT
                ArenaOpType.U -> UPDATE
                ArenaOpType.D -> DELETE
            }
        }
    }

}
