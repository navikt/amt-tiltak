# Deltaker-v1 Kafka Topic

## Innhold

1. [Beskrivelse](#beskrivelse)
1. [Meldinger](#meldinger)
    1. [Key](#key)
    1. [Deltaker](#deltaker)
    1. [DeltakerStatus](#status)
    1. [Skjema](#skjema)

## Beskrivelse

På topicen `amt.deltaker-v1` publiseres det siste øyeblikksbildet av deltakere på følgende tiltakstyper:

- INDOPPFAG
- ARBFORB
- AVKLARAG
- VASV
- ARBRRHDAG
- DIGIOPPARB
- JOBBK
- GRUPPEAMO
- GRUFAGYRKE

Topicen inneholder deltakere som kan ha **adressebeskyttelse** (kode 6/7), og skjermede personer (egen ansatt).

Deltakere kan bli slettet, da vil det bli produsert en tombstone for den deltakeren.

Topicen er satt opp med evig retention og compaction, så den skal inneholde alle deltakere som har vært registrert på de
nevnte tiltakene. 

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
    "statusTekst": "Har sluttet",
    "aarsak": "FATT_JOBB",
    "aarsakTekst": "Fått jobb",
    "opprettetDato": "2023-10-24T11:47:48.254204"
  },
  "registrertDato": "2022-01-27T16:13:39",
  "dagerPerUke": 3,
  "prosentStilling": 50,
  "endretDato": "2023-10-24T11:47:48.254204",
  "kilde": "ARENA",
  "innhold": {
    "innhold": [
      {
        "tekst": "Karriereveiledning",
        "innholdskode": "karriereveiledning"
      },
      {
        "tekst": "Kartlegge hvordan helsen din påvirker muligheten din til å jobbe",
        "innholdskode": "kartlegge-helse"
      }
    ],
    "ledetekst": "Arbeidsforberedende trening er et tilbud for deg som først ønsker å jobbe i et tilrettelagt arbeidsmiljø. Du får veiledning og støtte av en veileder. Sammen kartlegger dere hvordan din kompetanse, interesser og ferdigheter påvirker muligheten din til å jobbe."
  },
  "deltakelsesmengder": [
    {
      "deltakelsesprosent": 50,
      "dagerPerUke": 3,
      "gyldigFra": "2022-02-25",
      "opprettet": "2022-01-27T00:00:00"
    }
  ]
}
```

### Key - deltakerId

- Format: `uuid`
- Beskrivelse: En unik id som identifiserer en enkelt deltaker / deltakelse på ett tiltak.

### Deltaker

| Felt                   | Format         | Beskrivelse                                                                                                                                                                                                                                                                                                                                                                                                                 |
|------------------------|----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **id**                 | `uuid`         | En unik id som identifiserer en enkelt deltaker / deltakelse på ett tiltak. Samme som `Key`                                                                                                                                                                                                                                                                                                                                 |
| **gjennomforingId**    | `uuid`         | En unik id som identifiserer en tiltaksgjennomføring fra [Team Valp](https://github.com/navikt/mulighetsrommet)                                                                                                                                                                                                                                                                                                             |
| **personIdent**        | `string`       | Gjeldende folkeregisterident for personen, hvis en folkeregisterident ikke finnes kan det være en av: npid eller aktør-id                                                                                                                                                                                                                                                                                                   |
| **startDato**          | `date\|null`   | Dagen deltakeren starter/startet på tiltaket                                                                                                                                                                                                                                                                                                                                                                                | 
| **sluttDato**          | `date\|null`   | Dagen deltakeren slutter/sluttet på tiltaket                                                                                                                                                                                                                                                                                                                                                                                |
| **status**             | `object`       | Nåværende status på deltakeren, forteller f.eks om deltakeren deltar på tiltaket akkurat nå eller venter på oppstart osv. Se [Status](#status)                                                                                                                                                                                                                                                                              |
| **registrertDato**     | `datetime`     | Datoen deltakeren er registrert i Arena. Det er litt ukjent hva som definerer en registrertDato i fremtiden når vi i Komet overtar opprettelsen av deltakere.                                                                                                                                                                                                                                                               |
| **dagerPerUke**        | `float\|null`  | Antall dager deltakeren deltar på tiltaket per uke. I Arena er det mulig å angi dette feltet som et desimaltall f.eks `2.5`, i ny løsning er det bare mulig å bruke heltall.<br /><br /> I ny løsning kan dette bare settes på tiltakstypene: <br/> - Arbeidsforberedende trening (AFT) <br/> - Varig tilrettelagt arbeid (VTA)                                                                                             |
| **prosentStilling**    | `float\|null`  | Prosentandelen deltakeren opptar av en tiltaksplass. Hva 100% innebærer av faktisk deltakelse vil variere fra tiltak til tiltak. <br /><br />I Arena er det mulig å angi dette feltet som et desimaltall f.eks `42.1`, i ny løsning er det bare mulig å bruke heltall.<br /><br /> I ny løsning kan dette bare settes på tiltakstypene: <br /> - Arbeidsforberedende trening (AFT) <br /> - Varig tilrettelagt arbeid (VTA) |
| **endretDato**         | `datetime`     | Tidsstempel for siste endring på deltakeren                                                                                                                                                                                                                                                                                                                                                                                 |
| **kilde**              | `string\|null` | Kilde for deltakeren. Kan være `null`, `ARENA` eller `KOMET`. Hvis kilden er `KOMET` ble deltakeren opprettet i Komets nye løsning. Hvis kilde er `null` eller `ARENA` ble deltakeren opprettet Arena.                                                                                                                                                                                                                      |
| **innhold**            | `object\|null` | Innhold for tiltaksdeltakelsen på strukturert format. Kun for deltakere som er opprettet hos Komet, eller som har fått lagt til innhold etter at Komet ble master for deltakeren.                                                                                                                                                                                                                                           |
| **deltakelsesmengder** | `list\|null`   | Periodiserte deltakelsesmengder. Finnes kun på deltakere som Komet er master for, men gamle meldinger på topic vil kunne mangle dette feltet uavhengig av hvem som er master. Listen vil kun inneholde elementer for deltakarer på AFT og VTA.                                                                                                                                                                          |

#### Status

| Felt              | Format         | Beskrivelse                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
|-------------------|----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **type**          | `string`       | En av følgende verdier: `VENTER_PA_OPPSTART`, `DELTAR`, `HAR_SLUTTET`, `IKKE_AKTUELL`, `FEILREGISTRERT`, `SOKT_INN`, `VURDERES`, `VENTELISTE`, `AVBRUTT`, `FULLFORT`, `PABEGYNT_REGISTRERING`, `UTKAST_TIL_PAMELDING`, `AVBRUTT_UTKAST` <br /><br /> Det er litt ulike typer statuser som kan settes på deltakere, basert på hvilke tiltak de deltar på. Hovedregelen er at `FULLFORT` og `AVBRUTT` kan kun settes på deltakere som går på tiltak hvor det er en felles oppstart, typisk kurs som `JOBBK`, `GRUPPEAMO`, `GRUFAGYRKE`, mens `HAR_SLUTTET` brukes kun på de andre tiltakene som har et "løpende" inntak og oppstart av deltakere. |
| **statusTekst**   | `string\|null` | Tekstrepresentasjon av statustypen (for visning).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| **aarsak**        | `string\|null` | En årsak kan finnes på enkelte typer statuser (`HAR_SLUTTET`, `IKKE_AKTUELL` og `AVBRUTT`) og er en av følgende verdier: `SYK`, `FATT_JOBB`, `TRENGER_ANNEN_STOTTE`, `FIKK_IKKE_PLASS`, `IKKE_MOTT`, `ANNET`, `AVLYST_KONTRAKT`, `UTDANNING`, `SAMARBEIDET_MED_ARRANGOREN_ER_AVBRUTT`                                                                                                                                                                                                                                                                                                                                                           |
| **aarsakTekst**   | `string\|null` | Tekstrepresentasjon av statusårsaken (for visning).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| **opprettetDato** | `datetime`     | Tidsstempel for når statusen ble opprettet                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |

For mer informasjon om når og hvordan deltakerstatuser settes og endres se mer utdypende dokumentasjon
på [Confluence](https://confluence.adeo.no/pages/viewpage.action?pageId=573710206).

#### Deltakelsesinnhold

| Felt          | Format         | Beskrivelse                                             |
|---------------|----------------|---------------------------------------------------------|
| **ledetekst** | `string\|null` | Generell informasjon om tiltakstypen. Kommer fra Valp.  |
| **innhold**   | `list`         | Liste over valgt innhold som gjelder denne deltakelsen. |

#### Innhold

| Felt             | Format   | Beskrivelse                                                 |
|------------------|----------|-------------------------------------------------------------|
| **tekst**        | `string` | Tekstlig beskrivelse av innholdselementet. Kommer fra Valp. |
| **innholdskode** | `string` | Kodeverdi for innholdselementet. Kommer fra Valp.           |

#### Deltakelsesmengde

| Felt                   | Format        | Beskrivelse                                          |
|------------------------|---------------|------------------------------------------------------|
| **deltakelsesprosent** | `float`       | Prosentandelen deltakeren opptar av en tiltaksplass. |
| **dagerPerUke**        | `float\|null` | Antall dager deltakeren deltar på tiltaket per uke.  |
| **gyldigFra**          | `date`        | Dato f.o.m. når deltakalesesmengden trer i kraft.    |
| **opprettet**          | `datetime`    | Når endringen ble opprettet.                         |

Mer informasjon om hvordan periodiserte deltakelsesmengder settes og endres kommer snart.

### Skjema

For oppdatert informasjon er det best å se siste versjon direkte:

- [DeltakerV1Dto](https://github.com/navikt/amt-deltaker/blob/main/src/main/kotlin/no/nav/amt/deltaker/deltaker/kafka/DeltakerV1Dto.kt) (Skjema for deltakere Komet er master for)
- [DeltakerV1Dto](https://github.com/navikt/amt-tiltak/blob/main/kafka/kafka-producer/src/main/kotlin/no/nav/amt/tiltak/kafka/producer/dto/DeltakerV1Dto.kt) (Skjema for deltakere Arena er master for) 
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
    val endretDato: LocalDateTime,
    val kilde: Kilde?,
    val innhold: DeltakelsesinnholdDto?,
    val deltakelsesmengder: List<DeltakelsesmengdeDto>?,
)

data class DeltakerStatusDto(
    val type: Type,
    val statusTekst: String?,
    val aarsak: Aarsak?,
    val aarsakTekst: String?,
    val opprettetDato: LocalDateTime,
) {

    enum class Aarsak {
        SYK, FATT_JOBB, TRENGER_ANNEN_STOTTE, FIKK_IKKE_PLASS, IKKE_MOTT, ANNET, AVLYST_KONTRAKT,
        UTDANNING, SAMARBEIDET_MED_ARRANGOREN_ER_AVBRUTT
    }

    enum class Type {
        UTKAST_TIL_PAMELDING, AVBRUTT_UTKAST,
        VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET, IKKE_AKTUELL, FEILREGISTRERT,
        SOKT_INN, VURDERES, VENTELISTE, AVBRUTT, FULLFORT,
        PABEGYNT_REGISTRERING,
    }

}

enum class Kilde {
    KOMET,
    ARENA
}

data class DeltakelsesinnholdDto(
    val ledetekst: String?,
    val innhold: List<InnholdDto>,
)

data class InnholdDto(
    val tekst: String,
    val innholdskode: String,
)

data class DeltakelsesmengdeDto(
    val deltakelsesprosent: Float,
    val dagerPerUke: Float?,
    val gyldigFra: LocalDate,
    val opprettet: LocalDateTime,
)

```
