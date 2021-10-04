package no.nav.amt.tiltak.ingestors.arena.dto

data class ArenaTiltaksgjennomforing(
	val TILTAKGJENNOMFORING_ID: Long,
	val SAK_ID: Long,
	val TILTAKSKODE: String, // Ex: SPA
	val ANTALL_DELTAKERE: Int,
	val ANTALL_VARIGHET: Int?,
	val DATO_FRA: String?, // Ex: 2002-09-26 00:00:00
	val DATO_TIL: String, // Ex: 2002-09-26 00:00:00
	val FAGPLANKODE: String?,
	val MAALEENHET_VARIGHET: String?,
	val TEKST_FAGBESKRIVELSE: String?,
	val TEKST_KURSSTED: String?,
	val TEKST_MAALGRUPPE: String?,
	val STATUS_TREVERDIKODE_INNSOKNING: String, // Ex: J/N
	val REG_DATO: String, // Ex: 2002-10-03 20:48:22,
	val REG_USER: String,
	val MOD_DATO: String, // Ex: 2007-10-06 00:28:33
	val MOD_USER: String,
	val LOKALTNAVN: String?,
	val TILTAKSTATUSKODE: String, // Ex: AVSLUTT
	val PROSENT_DELTID: Float,
	val KOMMENTAR: String?,
	val ARBGIV_ID_ARRANGOR: Long,
	val PROFILELEMENT_ID_GEOGRAFI: String?, // Might be Int
	val KLOKKETID_FREMMOTE: String?,
	val DATO_FREMMOTE: String?, // Ex: 2002-09-26 00:00:00,
	val BEGRUNNELSE_STATUS: String?,
	val AVTALE_ID: Long?,
	val AKTIVITET_ID: Long?,
	val DATO_INNSOKNINGSTART: String?,
	val GML_FRA_DATO: String, // Ex: 2002-09-24 00:00:00,
	val GML_TIL_DATO: String, // Ex: 2002-09-28 00:00:00,
	val AETAT_FREMMOTEREG: String, // Ex: 1234 (NavEnhetId)
	val AETAT_KONTERINGSSTED: String, // Ex: 1234 (NavEnhetId)
	val OPPLAERINGNIVAAKODE: String?,
	val TILTAKGJENNOMFORING_ID_REL: String?,
	val VURDERING_GJENNOMFORING: String?,
	val PROFILELEMENT_ID_OPPL_TILTAK: String?,
	val DATO_OPPFOLGING_OK: String?,
	val PARTISJON: Long,
	val MAALFORM_KRAVBREV: String
)

data class ArenaTiltaksgjennomforingKafkaDto(
	override val table: String,
	override val op_type: ArenaOpType,
	override val op_ts: String,
	override val current_ts: String,
	override val pos: String,
	override val after: ArenaTiltaksgjennomforing?,
	override val before: ArenaTiltaksgjennomforing?,
) : GenericArenaKafkaDto<ArenaTiltaksgjennomforing>
