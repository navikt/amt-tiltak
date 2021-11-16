package no.nav.amt.tools.arenakafkaproducer.domain.dto

// @SONAR_START@
data class ArenaTiltaksgruppeDTO(
	val TILTAKSGRUPPENAVN: String, // Ex: Arbeidsforberedende trening
	val TILTAKSGRUPPEKODE: String, // Ex: AFT
	val REG_USER: String?, // Ex: SIAMO
	val REG_DATO: String?, // Ex: 2015-12-30 08:39:36
	val MOD_USER: String, // Ex: SIAMO
	val MOD_DATO: String, // Ex: 2015-12-30 08:39:36
	val DATO_FRA: String, // Ex: 2016-01-01 00:00:00
	val DATO_TIL: String, // Ex: 2099-01-01 00:00:00
	val VIS_LEGEOPPLYSNINGER_JN: String, // Ex: J/N
)

data class ArenaTiltaksgruppeKafkaDTO(
	override val table: String,
	override val op_type: ArenaOpType,
	override val op_ts: String,
	override val current_ts: String,
	override val pos: String,
	override val after: ArenaTiltaksgruppeDTO?,
	override val before: ArenaTiltaksgruppeDTO?,
) : GenericArenaKafkaDTO<ArenaTiltaksgruppeDTO>
// @SONAR_STOP@
