package no.nav.amt.tiltak.common.utils

import com.github.benmanes.caffeine.cache.Caffeine
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.common.utils.CacheUtils.tryCacheFirstNotNull
import no.nav.amt.tiltak.common.utils.CacheUtils.tryCacheFirstNullable
import java.util.concurrent.atomic.AtomicInteger

class CacheUtilsTest : FunSpec({

    test("skal cache for samme key") {
		val cache = Caffeine.newBuilder()
			.maximumSize(5)
			.build<String, String>()

		val counter = AtomicInteger()
		val supplier = {
			counter.incrementAndGet()
			"value"
		}

		tryCacheFirstNotNull(cache, "key1", supplier)
		tryCacheFirstNotNull(cache, "key1", supplier)

		counter.get() shouldBe 1
	}

    test("skal ikke cache for forskjellig keys") {
		val cache = Caffeine.newBuilder()
			.maximumSize(5)
			.build<String, String>()

		val counter = AtomicInteger()
		val supplier = {
			counter.incrementAndGet()
			"value"
		}

		tryCacheFirstNotNull(cache, "key1", supplier)
		tryCacheFirstNotNull(cache, "key2", supplier)

		counter.get() shouldBe 2
	}

	test("skal ikke cache null") {
		val cache = Caffeine.newBuilder()
			.maximumSize(5)
			.build<String, String>()

		val counter = AtomicInteger()
		val supplier = {
			counter.incrementAndGet()
			null
		}

		tryCacheFirstNullable(cache, "key1", supplier)
		tryCacheFirstNullable(cache, "key1", supplier)

		counter.get() shouldBe 2
	}

})
