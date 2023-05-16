package no.nav.amt.tiltak.bff.internal

import jakarta.servlet.http.HttpServletRequest
import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@Unprotected
@RestController
@RequestMapping("/internal/api/arrangor")
class ArrangorClientTestController(
	private val arrangorClient: AmtArrangorClient
) {

	@GetMapping("{id}")
	fun getById(@PathVariable("id") id: UUID, request: HttpServletRequest): AmtArrangorClient.ArrangorDto {
		return isInternal(request).let { internal ->
			if (internal) when (val res = arrangorClient.hentArrangor(id)) {
				is AmtArrangorClient.Result.OK -> res.result
				is AmtArrangorClient.Result.NotFound -> throw NoSuchElementException()
			}
			else throw UnauthorizedException("Not authorized to access this resource")
		}
	}

	@GetMapping("/organisasjonsnummer/{orgNr}")
	fun getByOrgNr(@PathVariable("orgNr") orgNr: String, request: HttpServletRequest): AmtArrangorClient.ArrangorDto {
		return isInternal(request).let { internal ->
			if (internal) when (val res = arrangorClient.hentArrangor(orgNr)) {
				is AmtArrangorClient.Result.OK -> res.result
				is AmtArrangorClient.Result.NotFound -> throw NoSuchElementException()
			}
			else throw UnauthorizedException("Not authorized to access this resource")
		}
	}

	private fun isInternal(request: HttpServletRequest) = request.remoteAddr == "127.0.0.1"


}
