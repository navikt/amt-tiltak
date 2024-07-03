package no.nav.amt.tiltak.data_publisher.model

import no.nav.amt.tiltak.core.domain.tiltak.Adresse
import no.nav.amt.tiltak.core.domain.tiltak.Adressebeskyttelse
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Kilde
import no.nav.amt.tiltak.core.domain.tiltak.Vurdering
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
	val dagerPerUke: Float?,
	val prosentStilling: Double?,
	val oppstartsdato: LocalDate?,
	val sluttdato: LocalDate?,
	val innsoktDato: LocalDate,
	val bestillingTekst: String?,
	val navKontor: String?,
	val navVeileder: DeltakerNavVeilederDto?,
	val deltarPaKurs: Boolean,
	val vurderingerFraArrangor: List<Vurdering>?,
	val kilde: Kilde?,
	val forsteVedtakFattet: LocalDate?,
	val sistEndretAv: UUID?,
	val sistEndretAvEnhet: UUID?,
) {
	fun digest() = DigestUtils.md5DigestAsHex(JsonUtils.toJson(this).toByteArray())
}

data class DeltakerPersonaliaDto(
	val personident: String,
	val navn: Navn,
	val kontaktinformasjon: DeltakerKontaktinformasjonDto,
	val skjermet: Boolean,
	val adresse: Adresse?,
	val adressebeskyttelse: Adressebeskyttelse?
)

data class Navn(
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String
)

data class DeltakerStatusDto(
	val id: UUID?,
    val type: DeltakerStatus.Type,
    val aarsak: DeltakerStatus.Aarsak?,
	val aarsaksbeskrivelse: String?,
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
	val epost: String?,
	val telefonnummer: String?
)
