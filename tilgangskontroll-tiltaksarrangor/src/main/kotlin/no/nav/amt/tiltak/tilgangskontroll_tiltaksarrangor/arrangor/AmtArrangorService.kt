package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.arrangor

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.common.utils.CacheUtils
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorAnsatt
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class AmtArrangorService(
	private val amtArrangorClient: AmtArrangorClient
) {
	private val personidentToAnsattCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofHours(1))
		.build<String, ArrangorAnsatt>()

	fun getAnsatt(ansattPersonident: String): ArrangorAnsatt? {
		return CacheUtils.tryCacheFirstNullable(personidentToAnsattCache, ansattPersonident) {
			return@tryCacheFirstNullable amtArrangorClient.hentAnsatt(ansattPersonident)?.tilArrangorAnsatt()
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
				deltakerlister = it.deltakerlister,
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
