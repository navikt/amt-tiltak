package no.nav.amt.tiltak.ingestors.arena.processors

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalTime

internal class StringUtils : FunSpec({

	test("asTime return time") {
		listOf(
			"09:00" to LocalTime.of(9, 0),
			"009:00" to LocalTime.MIDNIGHT,
			"09:000" to LocalTime.MIDNIGHT,
			"009:000" to LocalTime.MIDNIGHT,

			"15:46" to LocalTime.of(15, 46),
			"115:46" to LocalTime.MIDNIGHT,
			"15:461" to LocalTime.MIDNIGHT,
			"115:461" to LocalTime.MIDNIGHT,

			"15.46" to LocalTime.of(15, 46),
			"115.46" to LocalTime.MIDNIGHT,
			"15.461" to LocalTime.MIDNIGHT,
			"115.461" to LocalTime.MIDNIGHT,

			"1546" to LocalTime.of(15, 46),
			"11546" to LocalTime.MIDNIGHT,
			"15461" to LocalTime.MIDNIGHT,
			"115461" to LocalTime.MIDNIGHT,

			"asd" to LocalTime.MIDNIGHT,
			"9" to LocalTime.MIDNIGHT
		).forEach { (actual: String, expected: LocalTime) ->
			actual.asTime() shouldBe expected
		}
	}
})
