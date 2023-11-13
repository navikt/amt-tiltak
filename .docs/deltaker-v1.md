# Deltaker-v1 Kafka Topic

## Innhold
1. [Beskrivelse](#beskrivelse)
1. [Meldinger](#meldinger)
    1. [Key](#key)
    1. [Deltaker](#deltaker)
    1. [DeltakerStatus](#status)
    1. [Skjema](#skjema)

## Beskrivelse
På topicen `amt-deltaker-v1` publiseres det siste øyeblikksblidet av deltakere på følgende tiltakstyper:

- INDOPPFAG
- ARBFORB
- AVKLARAG
- VASV
- ARBRRHDAG
- DIGIOPPARB
- JOBBK
- GRUPPEAMO
- GRUFAGYRKE

Topicen inneholder deltakere som kan ha **adressebeskyttelse** (kode 6/7).

Deltakere kan bli slettet, da vil det bli produsert en tombstone for den deltakeren.

Topicen er satt opp med evig retention og compaction, så den skal inneholde alle deltakere som har vært registrert på de nevnte tilakene. Det er noen unntak: F.eks. så blir deltakere som er historisert i Arena pga gjentatte deltakelser på samme tiltaksgjennomføring slettet hos oss.


Kilden til dataene om deltakerene er i hovedsak Arena per dags dato. I fremtiden vil vi Team Komet overta som kilde, når vi har utviklet nye løsninger for å kunne melde på og endre deltakere utenfor Arena.


## Meldinger

**Eksempel payload:**

```json
{
  "id": "bd3b6087-2029-481b-bcf0-e37354c00286",
  "gjennomforingId": "1487f7fe-156c-41d7-8d90-bf108dd1b4d2",
  "personIdent": "12345678942",
  "startDato": "2022-02-25",
  "sluttDato": "2022-05-20",
  "status": {
    "type": "HAR_SLUTTET",
    "aarsak": "FATT_JOBB",
    "opprettetDato": "2023-10-24T11:47:48.254204"
  },
  "registrertDato": "2022-01-27T16:13:39",
  "dagerPerUke": 3,
  "prosentStilling": 50,
  "endretDato": "2023-10-24T11:47:48.254204"
}
```

### Key - deltakerId
- Format: `uuid`
- Beskrivelse: En unik id som identifiserer en enkelt deltaker / deltakelse på ett tiltak.


### Deltaker

|Felt|Format|Beskrivelse|
|-|-|-|
|**id**| `uuid`|En unik id som identifiserer en enkelt deltaker / deltakelse på ett tiltak. Samme som `Key`|
|**gjennomforingId** |`uuid`|En unik id som identifiserer en tiltaksgjennomføring fra [Team Valp](https://github.com/navikt/mulighetsrommet)|
|**personIdent** |`string`|Gjeldende folkeregisterident for personen, hvis en folkeregisterident ikke finnes kan det være en av: npid eller aktør-id|
|**startDato** |`date\|null`|Dagen deltakeren starter/startet på tiltaket| 
|**sluttDato** |`date\|null`|Dagen deltakeren slutter/sluttet på tiltaket|
|**status** |`object`|Nåværende status på deltakeren, forteller f.eks om deltakeren deltar på tiltaket akkurat nå eller venter på oppstart osv. Se [Status](#status)|
|**registrertDato** |`datetime`|Datoen deltakeren er registrert i Arena. Det er litt ukjent hva som definerer en registrertDato i fremtiden når vi i Komet overtar opprettelsen av deltakere.|
|**dagerPerUke** |`float\|null`|Antall dager deltakeren deltar på tiltaket per uke. I Arena er det mulig å angi dette feltet som et desimaltall f.eks `2.5`, i ny løsning er det bare mulig å bruke heltall.<br /><br /> I ny løsning kan dette bare settes på tiltakstypene: <br/> - Arbeidsforberedende trening (AFT) <br/> - Varig tilrettelagt arbeid (VTA)|
|**prosentStilling** |`float\|null`|Prosentandelen deltakeren deltar på tiltaket per uke. Hva er 100%? Vi vet ikke, det vil variere fra tiltak til tiltak hvor mye tid det er forventet at en deltaker skal bruke.<br /><br />I Arena er det mulig å angi dette feltet som et desimaltall f.eks `42.1`, i ny løsning er det bare mulig å bruke heltall.<br /><br /> I ny løsning kan dette bare settes på tiltakstypene: <br /> - Arbeidsforberedende trening (AFT) <br /> - Varig tilrettelagt arbeid (VTA)|
|**endretDato** |`datetime`|Tidsstempel for siste endring på deltakeren|

#### Status

|Felt|Format|Beskrivelse|
|-|-|-|
|**type**|`string`|En av følgende verdier: `VENTER_PA_OPPSTART`, `DELTAR`, `HAR_SLUTTET`, `IKKE_AKTUELL`, `FEILREGISTRERT`, `SOKT_INN`, `VURDERES`, `VENTELISTE`, `AVBRUTT`, `PABEGYNT_REGISTRERING` <br /><br /> Det er litt ulike typer statuser som kan settes på deltakere, basert på hvilke tiltak de deltar på. Hovedregelen er at `SOKT_INN`, `VURDERES`, `VENTELISTE` og `AVBRUTT` kan kun settes på deltakere som går på tiltak hvor det er en felles oppstart, typisk kurs som `JOBBK`, `GRUPPEAMO`, `GRUFAGYRKE`.|
|**aarsak**|`string\|null`|En årsak kan finnes på enkelte typer statuser (`HAR_SLUTTET`, `IKKE_AKTUELL` og `AVBRUTT`) og er en av følgende verdier: `SYK`, `FATT_JOBB`, `TRENGER_ANNEN_STOTTE`, `FIKK_IKKE_PLASS`, `IKKE_MOTT`, `ANNET`, `AVLYST_KONTRAKT`|
|**opprettetDato**|`datetime`|Tidsstempel for når statusen ble opprettet|

For mer utypende informasjon om når og hvordan deltakerstatuser settes og endres se mer utdypende dokumentasjon på [Confluence](https://confluence.adeo.no/pages/viewpage.action?pageId=573710206).

### Skjema

For oppdatert informasjon er det beste å se siste versjon direkte:
- [DeltakerV1Dto](https://github.com/navikt/amt-tiltak/blob/main/kafka/kafka-producer/src/main/kotlin/no/nav/amt/tiltak/kafka/producer/dto/DeltakerV1Dto.kt)
- [DeltakerStatusDto](https://github.com/navikt/amt-tiltak/blob/main/kafka/kafka-producer/src/main/kotlin/no/nav/amt/tiltak/kafka/producer/dto/DeltakerStatusDto.kt)
- [DeltakerStatus.Type og DeltakerStatus.Aarsak](https://github.com/navikt/amt-tiltak/blob/main/core/src/main/kotlin/no/nav/amt/tiltak/core/domain/tiltak/DeltakerStatus.kt)

```kotlin
data class DeltakerV1Dto(
	val id: UUID,
	val gjennomforingId: UUID,
	val personIdent: String,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: DeltakerStatusDto,
	val registrertDato: LocalDateTime,
	val dagerPerUke: Float?,
	val prosentStilling: Float?,
	val endretDato: LocalDateTime
)


data class DeltakerStatusDto(
	val type: Type,
	val aarsak: Aarsak?,
	val opprettetDato: LocalDateTime,
) {

    enum class Aarsak {
        SYK, FATT_JOBB, TRENGER_ANNEN_STOTTE, FIKK_IKKE_PLASS, IKKE_MOTT, ANNET, AVLYST_KONTRAKT
    }

    enum class Type {
        VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET, IKKE_AKTUELL, FEILREGISTRERT,
        SOKT_INN, VURDERES, VENTELISTE, AVBRUTT, 
        PABEGYNT_REGISTRERING, 
    }

}
```
