package no.nav.amt.tiltak.data_publisher.model

import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.common.json.JsonUtils
import org.springframework.util.DigestUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class DeltakerPublishDto(
	val id: UUID,
	val deltakerlisteId: UUID,
	val personalia: DeltakerPersonaliaDto,
	val status: DeltakerStatusDto,
	val dagerPerUke: Int?,
	val prosentStilling: Double?,
	val oppstartsdato: LocalDate?,
	val sluttdato: LocalDate?,
	val innsoktDato: LocalDate,
	val bestillingTekst: String?,
	val navKontor: String?,
	val navVeileder: DeltakerNavVeilederDto?,
	val skjult: DeltakerSkjultDto?,
	val deltarPaKurs: Boolean
) {
	fun digest() = DigestUtils.md5DigestAsHex(JsonUtils.toJson(this).toByteArray())
}

data class DeltakerPersonaliaDto(
	val personident: String,
	val navn: Navn,
	val kontaktinformasjon: DeltakerKontaktinformasjonDto,
	val skjermet: Boolean
)

data class DeltakerSkjultDto(
	val skjultAvAnsattId: UUID,
	val dato: LocalDateTime
)

data class DeltakerStatusDto(
	val type: DeltakerStatus.Type,
	val aarsak: DeltakerStatus.Aarsak.Type?,
	val gyldigFra: LocalDateTime,
	val opprettetDato: LocalDateTime
)

data class DeltakerKontaktinformasjonDto(
	val telefonnummer: String?,
	val epost: String?
)

data class DeltakerNavVeilederDto(
	val id: UUID,
	val navn: String,
	val epost: String?
)
