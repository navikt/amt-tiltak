package no.nav.amt.tiltak.test.database

import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.TestDataSeeder
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import javax.sql.DataSource

object DbTestDataUtils {

	private const val SCHEMA = "public"

	private const val FLYWAY_SCHEMA_HISTORY_TABLE_NAME = "flyway_schema_history"

	fun runScript(dataSource: DataSource, script: String) {
		val jdbcTemplate = JdbcTemplate(dataSource)
		jdbcTemplate.update(script)
	}

	fun runScriptFile(dataSource: DataSource, scriptFilePath: String) {
		val script = this::class.java.getResource(scriptFilePath).readText()
		runScript(dataSource, script)
	}

	fun cleanDatabase(dataSource: DataSource) {
		val jdbcTemplate = JdbcTemplate(dataSource)

		val tables = getAllTables(jdbcTemplate, SCHEMA).filter { it != FLYWAY_SCHEMA_HISTORY_TABLE_NAME }
		val sequences = getAllSequences(jdbcTemplate, SCHEMA)

		tables.forEach {
			jdbcTemplate.update("TRUNCATE TABLE $it CASCADE")
		}

		sequences.forEach {
			jdbcTemplate.update("ALTER SEQUENCE $it RESTART WITH 1")
		}
	}

	fun <V> parameters(vararg pairs: Pair<String, V>): MapSqlParameterSource {
		return MapSqlParameterSource().addValues(pairs.toMap())
	}

	fun cleanAndInitDatabaseWithTestData(dataSource: DataSource, seeder: (TestDataRepository) -> Unit = TestDataSeeder::insertDefaultTestData) {
		cleanDatabase(dataSource)
		TestDataSeeder.seed(dataSource, seeder)
	}

	private fun getAllTables(jdbcTemplate: JdbcTemplate, schema: String): List<String> {
		val sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ?"

		return jdbcTemplate.query(sql, { rs, _ -> rs.getString(1) }, schema)
	}

	private fun getAllSequences(jdbcTemplate: JdbcTemplate, schema: String): List<String> {
		val sql = "SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema = ?"

		return jdbcTemplate.query(sql, { rs, _ -> rs.getString(1) }, schema)
	}

}
