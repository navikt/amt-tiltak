package no.nav.amt.tiltak.ansatt

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.port.GjennomforingService
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.*

class ArrangorAnsattTilgangServiceImplTest : FunSpec({

	lateinit var ansattRepository: AnsattRepository

	lateinit var ansattRolleRepository: AnsattRolleRepository

	lateinit var gjennomforingService: GjennomforingService

	lateinit var arrangorAnsattTilgangServiceImpl: ArrangorAnsattTilgangServiceImpl

	val personligIdent = "fnr"

	val ansattId = UUID.randomUUID()

	val gjennomforingId = UUID.randomUUID()

	val arrangorId = UUID.randomUUID()

	fun gjennomforing(gjennomforingId: UUID, arrangorId: UUID): Gjennomforing {
		return Gjennomforing(
			id = gjennomforingId,
			tiltak = Tiltak(UUID.randomUUID(), "", ""),
			arrangorId = arrangorId,
			navn = "",
			status = Gjennomforing.Status.GJENNOMFORES,
			startDato = null,
			sluttDato = null,
			registrertDato = LocalDateTime.now(),
			fremmoteDato = null
		)
	}

	beforeEach {
		ansattRepository = mockk()

		ansattRolleRepository = mockk()

		gjennomforingService = mockk()

		arrangorAnsattTilgangServiceImpl = ArrangorAnsattTilgangServiceImpl(
			ansattRepository, ansattRolleRepository, gjennomforingService
		)

		every {
			ansattRepository.getByPersonligIdent(personligIdent)
		} returns AnsattDbo(
			ansattId,
			personligIdent,
			"",
			"",
			LocalDateTime.now(),
			LocalDateTime.now()
		)
	}

	test("verifiserTilgangTilGjennomforing skal kaste exception hvis ikke tilgang") {
		every {
			ansattRolleRepository.hentArrangorIderForAnsatt(ansattId)
		} returns listOf(arrangorId)

		every {
			gjennomforingService.getGjennomforing(gjennomforingId)
		} returns gjennomforing(gjennomforingId, UUID.randomUUID())

		val exception = shouldThrowExactly<ResponseStatusException> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilGjennomforing(personligIdent, gjennomforingId)
		}

		exception.status shouldBe HttpStatus.FORBIDDEN
	}

	test("verifiserTilgangTilGjennomforing skal ikke kaste exception hvis tilgang") {
		every {
			ansattRolleRepository.hentArrangorIderForAnsatt(ansattId)
		} returns listOf(arrangorId)

		every {
			gjennomforingService.getGjennomforing(gjennomforingId)
		} returns gjennomforing(gjennomforingId, arrangorId)

		shouldNotThrow<Throwable> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilGjennomforing(personligIdent, gjennomforingId)
		}
	}

	test("verifiserTilgangTilArrangor skal kaste exception hvis ikke tilgang") {
		every {
			ansattRolleRepository.hentArrangorIderForAnsatt(ansattId)
		} returns listOf(UUID.randomUUID())

		val exception = shouldThrowExactly<ResponseStatusException> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilArrangor(personligIdent, arrangorId)
		}

		exception.status shouldBe HttpStatus.FORBIDDEN
	}

	test("verifiserTilgangTilArrangor skal ikke kaste exception hvis tilgang") {
		every {
			ansattRolleRepository.hentArrangorIderForAnsatt(ansattId)
		} returns listOf(arrangorId)

		shouldNotThrow<Throwable> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilArrangor(personligIdent, arrangorId)
		}
	}

})
