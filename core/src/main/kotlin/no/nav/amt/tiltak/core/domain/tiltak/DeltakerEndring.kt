package no.nav.amt.tiltak.core.domain.tiltak

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class DeltakerEndring(
	val id: UUID,
	val deltakerId: UUID,
	val endring: Endring,
	val endretAv: UUID,
	val endretAvEnhet: UUID,
	val endret: LocalDateTime,
) {
    data class Aarsak(
		val type: Type,
		val beskrivelse: String? = null,
    ) {
        init {
            if (beskrivelse != null && type != Type.ANNET) {
                error("Aarsak $type skal ikke ha beskrivelse")
            }
        }

        enum class Type {
            SYK, FATT_JOBB, TRENGER_ANNEN_STOTTE, UTDANNING, IKKE_MOTT, ANNET
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes(
        JsonSubTypes.Type(value = Endring.EndreStartdato::class, name = "EndreStartdato"),
        JsonSubTypes.Type(value = Endring.EndreSluttdato::class, name = "EndreSluttdato"),
        JsonSubTypes.Type(value = Endring.EndreDeltakelsesmengde::class, name = "EndreDeltakelsesmengde"),
        JsonSubTypes.Type(value = Endring.EndreBakgrunnsinformasjon::class, name = "EndreBakgrunnsinformasjon"),
        JsonSubTypes.Type(value = Endring.EndreInnhold::class, name = "EndreInnhold"),
        JsonSubTypes.Type(value = Endring.IkkeAktuell::class, name = "IkkeAktuell"),
        JsonSubTypes.Type(value = Endring.ForlengDeltakelse::class, name = "ForlengDeltakelse"),
        JsonSubTypes.Type(value = Endring.AvsluttDeltakelse::class, name = "AvsluttDeltakelse"),
        JsonSubTypes.Type(value = Endring.EndreSluttarsak::class, name = "EndreSluttarsak"),
    )
    sealed class Endring {
        data class EndreBakgrunnsinformasjon(
            val bakgrunnsinformasjon: String?,
        ) : Endring()

        data class EndreInnhold(
            val innhold: List<Innhold>,
        ) : Endring()

        data class EndreDeltakelsesmengde(
            val deltakelsesprosent: Float?,
            val dagerPerUke: Float?,
        ) : Endring()

        data class EndreStartdato(
            val startdato: LocalDate?,
        ) : Endring()

        data class EndreSluttdato(
            val sluttdato: LocalDate,
        ) : Endring()

        data class ForlengDeltakelse(
            val sluttdato: LocalDate,
        ) : Endring()

        data class IkkeAktuell(
			val aarsak: Aarsak?,
        ) : Endring()

        data class AvsluttDeltakelse(
			val aarsak: Aarsak,
			val sluttdato: LocalDate,
        ) : Endring()

        data class EndreSluttarsak(
			val aarsak: Aarsak,
        ) : Endring()
    }
}
