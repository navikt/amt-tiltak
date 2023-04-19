package no.nav.amt.tiltak.data_publisher.model

import no.nav.common.json.JsonUtils
import org.springframework.util.DigestUtils
import java.time.LocalDate
import java.util.*

data class DeltakerPublishDto(
	val id: UUID,
	val deltakerlisteId: UUID,
	val personalia: DeltakerPersonaliaDto,
	val status: String?,
	val dagerPerUke: Int?,
	val prosentStilling: Double?,
	val oppstartsdato: LocalDate?,
	val sluttdato: LocalDate?,
	val innsoktDato: LocalDate,
	val bestillingTekst: String?,
	val navKontor: String?,
	val navVeileder: DeltakerNavVeilederDto?
) {
	fun digest() = DigestUtils.md5DigestAsHex(JsonUtils.toJson(this).toByteArray())
}

data class DeltakerPersonaliaDto(
	val personident: String,
	val navn: Navn,
	val kontaktinformasjon: DeltakerKontaktinformasjonDto,
	val skjermet: Boolean
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
