package com.neva.gradle.osgi.bundle

import com.neva.gradle.osgi.internal.Formats
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.util.GFileUtils
import java.io.File
import aQute.bnd.osgi.Jar as Bundle

open class BundleTask : Zip() {

    companion object {
        const val NAME = "osgiBundle"
    }

    @get:OutputFile
    val metadataFile: File
        get() = project.file("${BundlePlugin.TMP_PATH}/${BundlePlugin.METADATA_FILE}")

    @get:InputFiles
    @get:Optional
    val artifactFile
        get() = project.file((project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar).archivePath)

    @get:Internal
    val dependencies: Map<Dependency, File>
        get() {
            val config = project.configurations.getByName(BundlePlugin.ALL_CONFIG_NAME)
            val dependencies = config.allDependencies.fold(mutableMapOf<Dependency, File?>(), { r, d ->
                r[d] = config.files(d).singleOrNull(); r
            })

            @Suppress("unchecked_cast")
            return dependencies.filterValues { it != null && isOsgiBundle(it) } as Map<Dependency, File>
        }

    @get:InputFiles
    val dependencyFiles
        get() = dependencies.values

    init {
        group = "OSGi"
        description = "Create OSGi bundle"
        extension = "bundle"

        project.afterEvaluate {
            into(BundlePlugin.OSGI_PATH, { it.from(metadataFile) })
            into(BundlePlugin.ARTIFACT_PATH, { it.from(artifactFile) })
            dependencies.forEach { d, f -> into("${BundlePlugin.DEPENDENCIES_PATH}/${d.group}", { it.from(f) }) }
        }
    }

    private fun isOsgiBundle(file: File): Boolean {
        return try {
            !aQute.bnd.osgi.Jar(file).manifest.mainAttributes.getValue("Bundle-SymbolicName").isNullOrBlank()
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

        val metadata = BundleMetadata.of(project)
        val json = Formats.toJson(metadata)

        metadataFile.printWriter().use { it.print(json) }
    }

}