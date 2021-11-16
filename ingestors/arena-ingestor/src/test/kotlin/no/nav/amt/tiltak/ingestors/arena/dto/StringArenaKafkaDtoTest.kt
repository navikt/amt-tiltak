package no.nav.amt.tiltak.ingestors.arena.dto

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.Month

internal class StringArenaKafkaDtoTest : FunSpec({

	test("parsing of timestamp with millis should parse correctly") {
		val tsString = "2021-11-01 15:52:02.673090"

		val dto = StringArenaKafkaDto(
			"",
			ArenaOpType.I,
			tsString,
			"",
			"",
			null,
			null
		)

		val timestamp = dto.getOperationTimestamp()

		timestamp.year shouldBe 2021
		timestamp.month shouldBe Month.NOVEMBER
		timestamp.dayOfMonth shouldBe 1
		timestamp.hour shouldBe 15
		timestamp.minute shouldBe 52
		timestamp.second shouldBe 2
	}

})
