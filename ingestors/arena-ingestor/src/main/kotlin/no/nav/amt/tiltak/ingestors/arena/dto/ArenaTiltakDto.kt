package no.nav.amt.tiltak.ingestors.arena.dto

data class ArenaTiltak(
	val TILTAKSNAVN: String, // Ex: Lønnstilskudd av lengre varighet
	val TILTAKSGRUPPEKODE: String, // Ex: UTFAS
	val REG_DATO: String, // Ex: 2005-12-17 11:05:03
	val REG_USER: String, // Ex: ZZ123
	val MOD_DATO: String, // Ex: 2021-04-14 22:11:51,
	val MOD_USER: String, // Ex: SIAMO,
	val TILTAKSKODE: String, // Ex: LONNTILL,
	val DATO_FRA: String, // Ex: 2005-12-18 00:00:00,
	val DATO_TIL: String, // Ex: 2010-12-31 00:00:00,
	val AVSNITT_ID_GENERELT: Int?,
	val STATUS_BASISYTELSE: String, // Ex: J/N
	val ADMINISTRASJONKODE: String, // IND/X/X
	val STATUS_KOPI_TILSAGN: String, // Ex: J/N
	val ARKIVNOKKEL: String, // Ex: 123
	val STATUS_ANSKAFFELSE: String, // Ex: J/N
	val MAKS_ANT_PLASSER: Int?,
	val MAKS_ANT_SOKERE: Int,
	val STATUS_FAST_ANT_PLASSER: String?, // Ex: J/N
	val STATUS_SJEKK_ANT_DELTAKERE: String?, // Ex: J/N
	val STATUS_KALKULATOR: String, // Ex: J/N
	val RAMMEAVTALE: String, // Ex: IKKE
	val OPPLAERINGSGRUPPE: String?,
	val HANDLINGSPLAN: String,
	val STATUS_SLUTTDATO: String, // Ex: J/N
	val MAKS_PERIODE: Int?, // Ex: 35
	val STATUS_MELDEPLIKT: String?, // Ex: J/N
	val STATUS_VEDTAK: String, // Ex: J/N
	val STATUS_IA_AVTALE: String, // Ex: J/N
	val STATUS_TILLEGGSSTONADER: String, // Ex: J/N
	val STATUS_UTDANNING: String, // Ex: J/N
	val AUTOMATISK_TILSAGNSBREV: String, // Ex: J/N
	val STATUS_BEGRUNNELSE_INNSOKT: String, // Ex: J/N
	val STATUS_HENVISNING_BREV: String, // Ex: J/N
	val STATUS_KOPIBREV: String, // Ex: J/N
)

data class ArenaTiltakKafkaDto(
	override val table: String,
	override val op_type: ArenaOpType,
	override val op_ts: String,
	override val current_ts: String,
	override val pos: String,
	override val after: ArenaTiltak?,
	override val before: ArenaTiltak?,
) : GenericArenaKafkaDto<ArenaTiltak>
