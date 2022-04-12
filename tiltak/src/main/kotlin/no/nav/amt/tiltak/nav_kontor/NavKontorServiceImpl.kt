package no.nav.amt.tiltak.nav_kontor

import no.nav.amt.tiltak.core.domain.tiltak.NavKontor
import no.nav.amt.tiltak.core.port.NavKontorService
import org.springframework.stereotype.Service

@Service
open class NavKontorServiceImpl(
	private val navKontorRepository: NavKontorRepository
) : NavKontorService {

	override fun hentNavKontorer(enhetIder: List<String>): List<NavKontor> {
		return navKontorRepository.hentEnheter(enhetIder)
			.map { NavKontor(id = it.id, enhetId = it.enhetId, navn = it.navn) }
	}

}
