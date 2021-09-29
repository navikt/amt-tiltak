package no.nav.amt.tiltak.connectors.brreg

import no.nav.amt.tiltak.connectors.brreg.dto.LinkDTO
import no.nav.amt.tiltak.connectors.brreg.dto.OverordnetEnhetDTO
import no.nav.amt.tiltak.connectors.brreg.dto.VirksomhetDTO
import no.nav.amt.tiltak.connectors.brreg.dto.toModel
import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Virksomhet
import no.nav.amt.tiltak.core.port.EnhetsregisterConnector
import org.slf4j.LoggerFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.Instant

@Service
class BronnoysundConnector: EnhetsregisterConnector {

    private val basePath = "https://data.brreg.no/enhetsregisteret/api"
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val restTemplate: RestTemplate

    init {
        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(5000)
        factory.setReadTimeout(60000)

        restTemplate = RestTemplate(factory)
    }

    override fun virksomhetsinformasjon(virksomhetsnummer: String): Virksomhet {
        val virksomhetsUrl = "$basePath/underenheter/$virksomhetsnummer"

        val virksomhet = getVirksomhet(virksomhetsUrl)
        val overordnetEnhet = getOverordnetEnhet(getLink(virksomhet.links, "overordnetEnhet"))

        return toModel(virksomhet, overordnetEnhet)
    }

    private fun getVirksomhet(url: String): VirksomhetDTO {
        val start = Instant.now()

        val response = restTemplate.getForEntity(url, VirksomhetDTO::class.java)

        if(!response.hasBody()) {
            throw ResponseStatusException(response.statusCode, "GET kall til $url returnerer ikke body")
        }

        logTimeOk(start, url)
        return response.body
    }

    private fun getOverordnetEnhet(url: String): OverordnetEnhetDTO {
        val start = Instant.now()

        val response = restTemplate.getForEntity(url, OverordnetEnhetDTO::class.java)

        if(!response.hasBody()) {
            throw ResponseStatusException(response.statusCode, "GET kall til $url returnerer ikke body")
        }

        logTimeOk(start, url)
        return response.body
    }

    private fun getLink(links: Map<String, LinkDTO>, name: String): String {
        if(links[name] == null) {
            throw UnsupportedOperationException("The link $name does not exist")
        }

        return links[name]!!.href
    }

    private fun toModel(virksomhet: VirksomhetDTO, overordnetEnhet: OverordnetEnhetDTO): Virksomhet {
        return Virksomhet(
            id = null,
            organisasjonsnummer = overordnetEnhet.organisasjonsnummer,
            organisasjonsnavn = overordnetEnhet.navn,
            organisasjonensAdresse = overordnetEnhet.forretningsadresse.toModel(),
            virksomhetsnummer = virksomhet.organisasjonsnummer,
            virksomhetsnavn = virksomhet.navn,
            postadresse = virksomhet.postadresse.toModel(),
            beliggenhetsadresse = virksomhet.beliggenhetsadresse.toModel()
        )
    }

    private fun logTimeOk(start: Instant, url: String) {
        val time = Duration.between(start, Instant.now()).toMillis()
        logger.info("GET [execution time: $time ms] Url: $url")
    }
}
