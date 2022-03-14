package no.nav.amt.navansatt

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.lang.IllegalArgumentException
import java.time.LocalTime

class BucketTest : StringSpec ({

	// bucket 0:   [W100527, W100758, W100824, W996906, W998919]
	// bucket 50:  [W100172, W101063, W999686, W999752, W999983]
	// bucket 287:  [W100229, W100526, W996839, W996905, W998918, W999809]

	"Bucket.forNavIdent - identer beregner lik bucket - calculates same bucket" {
		// bucket 0
		Bucket.forNavIdent("W100758") shouldBe Bucket.forNavIdent("W998919")
		// bucket somewhere inbetween
		Bucket.forNavIdent("W101063") shouldBe Bucket.forNavIdent("W999752")
		// last bucket (287)
		Bucket.forNavIdent("W100526") shouldBe Bucket.forNavIdent("W998918")

		// og ulik bucket
		Bucket.forNavIdent("W100526") shouldNotBe Bucket.forNavIdent("W999752")
	}

	"Bucket.forCurrentTime - from tidspunkt - ender i samme bucket innenfor 5 minutter" {
		// bucket 0
		Bucket.forTidspunkt(LocalTime.MIDNIGHT) shouldBe
			Bucket.forTidspunkt(LocalTime.MIDNIGHT.plusMinutes(1))
		// bucket somewhere inbetween
		Bucket.forTidspunkt(LocalTime.MIDNIGHT.plusMinutes(10)) shouldBe
			Bucket.forTidspunkt(LocalTime.MIDNIGHT.plusMinutes(14).plusSeconds(59))
		// last bucket (287)
		Bucket.forTidspunkt(LocalTime.MIDNIGHT.minusMinutes(5)) shouldBe
			Bucket.forTidspunkt(LocalTime.MIDNIGHT.minusNanos(1))

		// first and last should differ - it should change at midnight
		Bucket.forTidspunkt(LocalTime.MIDNIGHT) shouldNotBe
			Bucket.forTidspunkt(LocalTime.MIDNIGHT.minusNanos(1))
	}

	"Bucket - første og siste bucket bør være lik, uavhengig av opprettelsesmetode" {
		// bucket 0
		Bucket.forTidspunkt(LocalTime.MIDNIGHT) shouldBe Bucket.forNavIdent("W100758")

		// last bucket (287)
		Bucket.forTidspunkt(LocalTime.MIDNIGHT.minusMinutes(5)) shouldBe
			Bucket.forNavIdent("W998918")
	}

	"Bucket har endret algoritme for å velge bucket - reevaluer hele testen" {
		shouldThrow<IllegalArgumentException> {
			Bucket(288)
		}
	}

})
