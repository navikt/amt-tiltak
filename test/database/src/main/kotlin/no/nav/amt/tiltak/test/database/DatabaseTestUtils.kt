package no.nav.amt.tiltak.test.database

import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

object DatabaseTestUtils {

	private val tables = listOf(
		"nav_ansatt",
		"tiltaksleverandor",
		"tiltaksleverandor_ansatt",
		"tiltaksleverandor_ansatt_rolle",
		"tiltak",
		"tiltaksinstans",
		"bruker",
		"deltaker",
		"arena_data",
		"arena_tiltak_ids_ignored",
		"shedlock",
	)

	private val sequences = listOf(
		"arena_data_id_seq"
	)

	fun runScript(script: String, dataSource: DataSource) {
		val jdbcTemplate = JdbcTemplate(dataSource)
		jdbcTemplate.update(script)
	}

	fun runScriptFile(scriptFilePath: String, dataSource: DataSource) {
		val script = this::class.java.getResource(scriptFilePath).readText()
		runScript(script, dataSource)
	}

	fun cleanDatabase(dataSource: DataSource) {
		val jdbcTemplate = JdbcTemplate(dataSource)

		tables.forEach {
			jdbcTemplate.update("TRUNCATE TABLE $it CASCADE")
		}

		sequences.forEach {
			jdbcTemplate.update("ALTER SEQUENCE $it RESTART WITH 1")
		}
	}

}
