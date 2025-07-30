package util

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.findByType
import kotlin.jvm.optionals.getOrNull

object LibrariesUtils {

    /**
     * Retrieve a library dependency from the specified version catalog.
     *
     * @param libraryAlias The alias of the library in the catalog.
     * @param versionCatalogName The name of the catalog (defaults to "libs").
     * @return The resolved [MinimalExternalModuleDependency].
     * @throws IllegalArgumentException if the catalog or library alias is not found.
     */
    fun Project.getLibraryValue(
		libraryAlias: String,
		versionCatalogName: String = "libs"
    ): MinimalExternalModuleDependency = extensions.findByType<VersionCatalogsExtension>()
		?.named(versionCatalogName)
		?.findLibrary(libraryAlias)
		?.getOrNull()
		?.get()
		?: throw IllegalArgumentException("Library alias '$libraryAlias' not found in version catalog '$versionCatalogName'.")
}
