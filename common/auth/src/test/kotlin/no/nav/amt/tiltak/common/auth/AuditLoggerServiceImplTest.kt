package no.nav.amt.tiltak.common.auth

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.common.audit_log.cef.CefMessage
import no.nav.common.audit_log.log.AuditLogger
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class AuditLoggerServiceImplTest {

	val navAnsattService: NavAnsattService = mockk()

	val deltakerService: DeltakerService = mockk()

	val auditLogger: AuditLogger = mockk()

	lateinit var auditLoggerService: AuditLoggerServiceImpl

	@BeforeEach
	fun setup() {
		auditLoggerService = AuditLoggerServiceImpl(
			auditLogger,
			navAnsattService,
			deltakerService
		)
	}

	@Test
	fun `navAnsattBehandletEndringsmeldingAuditLog - skal lage logmelding med riktig data`() {
		val navAnsattId = UUID.randomUUID()
		val deltakerId = UUID.randomUUID()

		every {
			navAnsattService.getNavAnsatt(navAnsattId)
		} returns NavAnsatt(
			id = navAnsattId,
			navIdent = "Z1234",
			navn = ""
		)

		every {
			deltakerService.hentDeltaker(deltakerId)
		} returns Deltaker(
			id = deltakerId,
			gjennomforingId = UUID.randomUUID(),
			fornavn = "",
			mellomnavn = null,
			etternavn = "",
			personIdent = "12345678900",
			navEnhet = null,
			navVeilederId = null,
			epost = null,
			telefonnummer = null,
			startDato = null,
			sluttDato = null,
			status = DeltakerStatus(UUID.randomUUID(), DeltakerStatus.Type.DELTAR, null, LocalDateTime.now(), LocalDateTime.now(), true),
			registrertDato = LocalDateTime.now(),
			erSkjermet = false,
			endretDato = LocalDateTime.now(),
			adressebeskyttelse = null
		)

		val messageSlot = slot<CefMessage>()

		every {
			auditLogger.log(capture(messageSlot))
		} returns Unit

		auditLoggerService.navAnsattBehandletEndringsmeldingAuditLog(navAnsattId, deltakerId)

		val msg = messageSlot.captured

		msg.version shouldBe 0
		msg.deviceProduct shouldBe "amt-tiltak"
		msg.deviceVendor shouldBe "Tiltaksadministrasjon"
		msg.deviceVersion shouldBe "1.0"
		msg.name shouldBe "Sporingslogg"
		msg.severity shouldBe "INFO"
		msg.signatureId shouldBe "audit:access"
		msg.extension["msg"] shouldBe "NAV-ansatt har lest melding fra tiltaksarrangoer om oppstartsdato paa tiltak for aa registrere dette."
		msg.extension["suid"] shouldBe "Z1234"
		msg.extension["duid"] shouldBe "12345678900"
	}
}
