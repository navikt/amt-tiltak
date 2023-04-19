package no.nav.amt.tiltak.data_publisher.publish

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime

class ArrangorAnsattPublishQueryTest: FunSpec({
	val dataSource = SingletonPostgresContainer.getDataSource()
	val template = NamedParameterJdbcTemplate(dataSource)

	val query = ArrangorAnsattPublishQuery(template)

	test("isGyldig") {
		query.isGyldig(null) shouldBe true
		query.isGyldig(LocalDateTime.now().minusDays(1)) shouldBe false
		query.isGyldig(LocalDateTime.now().plusMinutes(1)) shouldBe true
		query.isGyldig(LocalDate.of(3000,1,1).atStartOfDay()) shouldBe true
	}

})
