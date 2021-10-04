package no.nav.amt.tiltak.ingestors.arena.dto

data class ArenaDataDTO(
    val table: String,
    val op_type: String,
    val op_ts: String,
    val current_ts: String,
    val pos: String,
    val before: Map<String, Any>,
    val after: Map<String, Any>
)
