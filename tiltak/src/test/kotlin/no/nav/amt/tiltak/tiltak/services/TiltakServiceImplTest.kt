package no.nav.amt.tiltak.tiltak.services

import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.tiltak.dbo.TiltakDbo
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class TiltakServiceImplTest : FunSpec({

	test("Should cache tiltak") {
		val repository = mockk<TiltakRepository>()
		val service = TiltakServiceImpl(repository)
		val id = UUID.randomUUID()

		every {
			repository.getAll()
		} returns listOf(TiltakDbo(id, "", "", "", LocalDateTime.now(), LocalDateTime.now()))

		service.getTiltakById(id)
		service.getTiltakById(id)

		verify (exactly = 1) {
			repository.getAll()
		}
	}


})
