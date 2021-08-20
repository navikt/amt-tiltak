package no.nav.amt.tiltak.adapter.input

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class TestController {

	@Unprotected
	@GetMapping
	fun test(): String {
		return "Hello World";
	}

}
