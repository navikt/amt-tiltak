package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.arrangor

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.common.utils.CacheUtils
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class AmtArrangorService(
	private val amtArrangorClient: AmtArrangorClient
) {
	private val personligIdentToRolleCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofHours(1))
		.build<String, List<ArrangorAnsattRoller>>()

	fun hentTiltaksarrangorRoller(ansattPersonident: String): List<ArrangorAnsattRoller> {
		return CacheUtils.tryCacheFirstNotNull(personligIdentToRolleCache, ansattPersonident) {
			val ansatt = amtArrangorClient.hentAnsatt(ansattPersonident)
			return@tryCacheFirstNotNull ansatt.tilArrangorAnsattRoller()
		}
	}
}

fun AmtArrangorClient.AnsattDto.tilArrangorAnsattRoller(): List<ArrangorAnsattRoller> {
	return arrangorer.map { tilknyttetArrangorDto ->
		ArrangorAnsattRoller(
			organisasjonsnummer = tilknyttetArrangorDto.arrangor.organisasjonsnummer,
			roller = tilknyttetArrangorDto.roller.map { ArrangorAnsattRolle.valueOf(it.name) }
		)
	}
}

data class ArrangorAnsattRoller(
	val organisasjonsnummer: String,
	val roller: List<ArrangorAnsattRolle>,
)
