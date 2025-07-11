package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories


@TestConfiguration
@EnableJdbcRepositories
class JdbcTestConfiguration : AbstractJdbcConfiguration()
