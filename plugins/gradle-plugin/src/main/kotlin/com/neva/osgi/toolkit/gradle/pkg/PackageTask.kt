package com.neva.osgi.toolkit.gradle.pkg

import com.neva.osgi.toolkit.commons.domain.Package
import com.neva.osgi.toolkit.gradle.internal.Formats
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.Jar
import org.gradle.util.GFileUtils
import java.io.File
import aQute.bnd.osgi.Jar as Bundle

open class PackageTask : Jar() {

    companion object {
        const val NAME = "osgiPackage"
    }

    @get:OutputFile
    val metadataFile: File
        get() = project.file("${PackagePlugin.TMP_PATH}/${Package.METADATA_FILE}")

    @get:InputFiles
    @get:Optional
    val artifactFile
        get() = project.file((project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar).archivePath)

    @get:Internal
    val dependencies: Map<Dependency, File>
        get() {
            val config = project.configurations.getByName(PackagePlugin.ALL_CONFIG_NAME)
            return config.allDependencies.fold(mutableMapOf(), { r, d ->
                val file = config.files(d).singleOrNull()
                if (file != null && file.exists() && isOsgiBundle(file)) {
                    r[d] = file
                }
                r
            })
        }

    @get:InputFiles
    val dependencyFiles
        get() = dependencies.values

    init {
        group = "OSGi"
        description = "Create OSGi package"

        project.afterEvaluate {
            into(Package.OSGI_PATH, { it.from(metadataFile) })
            into(Package.ARTIFACT_PATH, { it.from(artifactFile) })
            dependencies.forEach { d, f -> into("${Package.DEPENDENCIES_PATH}/${d.group}", { it.from(f) }) }
        }
    }

    private fun isOsgiBundle(file: File): Boolean {
        return try {
            !Bundle(file).manifest.mainAttributes.getValue("Bundle-SymbolicName").isNullOrBlank()
        } catch (e: Exception) {
            false
        }
    }

    @TaskAction
    override fun copy() {
        generateMetadataFile()
        super.copy()
    }

    private fun generateMetadataFile() {
        GFileUtils.mkdirs(metadataFile.parentFile)

        val metadata = PackageMetadata.of(project)
        val json = Formats.toJson(metadata)

        metadataFile.printWriter().use { it.print(json) }
    }

}
