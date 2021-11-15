package no.nav.amt.tools.arenakafkaproducer.domain.dto

enum class ArenaOpType(val type: String) {
	I("I"),	// insert
	U("U"),	// update
	D("D"),	// delete
}

interface GenericArenaKafkaDTO<T> {
	val table: String // Ex: ARENA_GOLDENGATE.TILTAKSAKTIVITET
	val op_type: ArenaOpType
	val op_ts: String // Ex: 2021-09-08 08:06:21.371796 (Er ikke ISO 8601)
	val current_ts: String // Ex: 2021-09-08T08:07:50.726030 (Er ISO 8601)
	val pos: String // Ex: 00000000000037764187
	val after: T?
	val before: T?
}
