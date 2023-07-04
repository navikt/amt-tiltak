package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.arrangor

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.common.utils.CacheUtils
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorAnsatt
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

@Service
class AmtArrangorService(
	private val amtArrangorClient: AmtArrangorClient
) {
	private val orgnummerToArrangorCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofHours(1))
		.build<String, Arrangor>()

	fun getAnsatt(ansattPersonident: String): ArrangorAnsatt? {
		return amtArrangorClient.hentAnsatt(ansattPersonident)?.tilArrangorAnsatt()
	}

	fun getAnsatt(ansattId: UUID): ArrangorAnsatt? {
		return amtArrangorClient.hentAnsatt(ansattId)?.tilArrangorAnsatt()
	}

	fun getArrangor(orgnummer: String): Arrangor? {
		return CacheUtils.tryCacheFirstNullable(orgnummerToArrangorCache, orgnummer) {
			return@tryCacheFirstNullable amtArrangorClient.hentArrangor(orgnummer)?.tilArrangor()
		}
	}
}

fun AmtArrangorClient.AnsattDto.tilArrangorAnsatt(): ArrangorAnsatt {
	return ArrangorAnsatt(
		id = id,
		personalia = ArrangorAnsatt.PersonaliaDto(
			personident = personalia.personident,
			personId = personalia.personId,
			navn = ArrangorAnsatt.Navn(
				fornavn = personalia.navn.fornavn,
				mellomnavn = personalia.navn.mellomnavn,
				etternavn = personalia.navn.etternavn
			)
		),
		arrangorer = arrangorer.map {
			ArrangorAnsatt.TilknyttetArrangorDto(
				arrangorId = it.arrangorId,
				arrangor = ArrangorAnsatt.Arrangor(
					id = it.arrangor.id,
					navn = it.arrangor.navn,
					organisasjonsnummer = it.arrangor.organisasjonsnummer
				),
				overordnetArrangor = it.overordnetArrangor?.let { overordnetArrangor ->
					ArrangorAnsatt.Arrangor(
						id = overordnetArrangor.id,
						navn = overordnetArrangor.navn,
						organisasjonsnummer = overordnetArrangor.organisasjonsnummer
					)
				},
				roller = it.roller.map { rolle -> ArrangorAnsatt.AnsattRolle.valueOf(rolle.name) },
				veileder = it.veileder.map { veileder ->
					ArrangorAnsatt.VeilederDto(
						deltakerId = veileder.deltakerId,
						type = ArrangorAnsatt.VeilederType.valueOf(veileder.type.name)
					)
				},
				koordinator = it.koordinator
			)
		}
	)
}

fun AmtArrangorClient.ArrangorMedOverordnetArrangor.tilArrangor(): Arrangor {
	return Arrangor(
		id = id,
		navn = navn,
		organisasjonsnummer = organisasjonsnummer,
		overordnetEnhetOrganisasjonsnummer = overordnetArrangor?.organisasjonsnummer,
		overordnetEnhetNavn = overordnetArrangor?.navn
	)
}

fun ArrangorAnsatt.tilArrangorAnsattRoller(): List<ArrangorAnsattRoller> {
	return arrangorer.map { tilknyttetArrangorDto ->
		ArrangorAnsattRoller(
			arrangor = Arrangor(
				id = tilknyttetArrangorDto.arrangor.id,
				navn = tilknyttetArrangorDto.arrangor.navn,
				organisasjonsnummer = tilknyttetArrangorDto.arrangor.organisasjonsnummer,
				overordnetEnhetOrganisasjonsnummer = tilknyttetArrangorDto.overordnetArrangor?.organisasjonsnummer,
				overordnetEnhetNavn = tilknyttetArrangorDto.overordnetArrangor?.navn
			),
			roller = tilknyttetArrangorDto.roller.map { ArrangorAnsattRolle.valueOf(it.name) }
		)
	}
}

data class ArrangorAnsattRoller(
	val arrangor: Arrangor,
	val roller: List<ArrangorAnsattRolle>,
)
