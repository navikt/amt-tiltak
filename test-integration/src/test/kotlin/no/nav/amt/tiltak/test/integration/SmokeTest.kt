package no.nav.amt.tiltak.test.integration

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SmokeTest: IntegrationTestBase() {

	@Test
	internal fun firstTest() {
		true shouldBe true
	}
}
