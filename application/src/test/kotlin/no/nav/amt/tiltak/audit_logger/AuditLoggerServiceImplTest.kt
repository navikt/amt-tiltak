package no.nav.amt.tiltak.audit_logger

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.amt.tiltak.application.audit_logger.AuditLoggerServiceImpl
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.port.AuditEventSeverity
import no.nav.amt.tiltak.core.port.AuditEventType
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.common.audit_log.cef.CefMessage
import no.nav.common.audit_log.log.AuditLogger
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class AuditLoggerServiceImplTest {

	val navAnsattService: NavAnsattService = mockk()

	val deltakerService: DeltakerService = mockk()

	val auditLogger: AuditLogger = mockk()

	lateinit var auditLoggerService: AuditLoggerServiceImpl

	@BeforeEach
	fun setup() {
		auditLoggerService = AuditLoggerServiceImpl(auditLogger, navAnsattService, deltakerService)
	}

	@Test
	fun `navAnsattAuditLog - skal lage logmelding med riktig data`() {
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
			bruker = Bruker(
				id = UUID.randomUUID(),
				fornavn = "",
				mellomnavn = null,
				etternavn = "",
				fodselsnummer = "12345678900",
				navEnhet = null
			),
			startDato = null,
			sluttDato = null,
			status = DeltakerStatus(UUID.randomUUID(), Deltaker.Status.DELTAR, LocalDateTime.now(), LocalDateTime.now(), true),
			registrertDato = LocalDateTime.now(),
		)

		val messageSlot = slot<CefMessage>()

		every {
			auditLogger.log(capture(messageSlot))
		} returns Unit

		auditLoggerService.navAnsattAuditLog(
			navAnsattId,
			deltakerId,
			AuditEventType.ACCESS,
			AuditEventSeverity.INFO,
			"NAV ansatt har gjort oppslag"
		)

		val msg = messageSlot.captured

		msg.version shouldBe 0
		msg.deviceProduct shouldBe "AuditLogger"
		msg.deviceVendor shouldBe "amt-tiltak"
		msg.deviceVersion shouldBe "1.0"
		msg.name shouldBe "Sporingslogg"
		msg.severity shouldBe "INFO"
		msg.signatureId shouldBe "audit:access"
		msg.extension["msg"] shouldBe "NAV ansatt har gjort oppslag"
		msg.extension["suid"] shouldBe "Z1234"
		msg.extension["duid"] shouldBe "12345678900"
	}

}
