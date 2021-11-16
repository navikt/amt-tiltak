package no.nav.amt.tiltak.ingestors.arena.dto

// @SONAR_START@
data class ArenaTiltakDeltakerDTO(
	val TILTAKDELTAKER_ID: Long,
	val PERSON_ID: Long,
	val TILTAKGJENNOMFORING_ID: Long,
	val DELTAKERSTATUSKODE: String, // Ex: TILBUD
	val DELTAKERTYPEKODE: String, // Ex: INNSOKT
	val AARSAKVERDIKODE_STATUS: String?,
	val OPPMOTETYPEKODE: String?,
	val PRIORITET: Int?,
	val BEGRUNNELSE_INNSOKT: String?,
	val BEGRUNNELSE_PRIORITERING: String?,
	val REG_DATO: String, // Ex: 2021-09-25 14:04:28
	val REG_USER: String, // Ex: ZZZ1234
	val MOD_DATO: String, // Ex: 2021-09-25 14:26:11
	val MOD_USER: String, // Ex: ZZZ1234
	val DATO_SVARFRIST: String?, // timestamp
	val DATO_FRA: String?, // Ex: 2021-09-25 00:00:00
	val DATO_TIL: String?, // Ex: 2021-09-25 00:00:00
	val BEGRUNNELSE_STATUS: String?,
	val PROSENT_DELTID: Float,
	val BRUKERID_STATUSENDRING: String,	// Ex: ZZZ1234
	val DATO_STATUSENDRING: String?, // Ex: 2021-09-25 14:24:51
	val AKTIVITET_ID: Long,
	val BRUKERID_ENDRING_PRIORITERING: String?,
	val DATO_ENDRING_PRIORITERING: String?, // Ex: 2021-09-25 14:24:51
	val DOKUMENTKODE_SISTE_BREV: String?,
	val STATUS_INNSOK_PAKKE: String?, // Ex: J/N
	val STATUS_OPPTAK_PAKKE: String?, // Ex: J/N
	val OPPLYSNINGER_INNSOK: String?,
	val PARTISJON: Int?,
	val BEGRUNNELSE_BESTILLING: String?,
	val ANTALL_DAGER_PR_UKE: Int?
)
// @SONAR_STOP@
