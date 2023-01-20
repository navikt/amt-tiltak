package no.nav.amt.tiltak.test_utils

import java.lang.AssertionError
import java.time.Duration
import java.time.LocalDateTime

object AsyncUtils {

	fun eventually(
		until: Duration = Duration.ofSeconds(10),
		interval: Duration = Duration.ofMillis(100),
		func: () -> Unit
	) {
		val untilTime = LocalDateTime.now().plusNanos(until.toNanos())

		var throwable: Throwable = IllegalStateException()

		while (LocalDateTime.now().isBefore(untilTime)) {
			try {
				func()
				return
			} catch (t: Throwable) {
				throwable = t
				Thread.sleep(interval.toMillis())
			}
		}

		throw AssertionError(throwable)
	}
}
