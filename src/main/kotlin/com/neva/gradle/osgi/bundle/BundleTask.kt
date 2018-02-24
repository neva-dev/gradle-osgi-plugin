package com.neva.gradle.osgi.bundle

import com.beust.klaxon.Klaxon
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
        get() = project.file(BundlePlugin.METADATA_FILE)

    @get:InputFile
    val artifactFile
        get() = project.file((project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar).archivePath)

    @get:Internal
    val dependencyConfig
        get() = project.configurations.getByName(BundlePlugin.DEPENDENCIES_CONFIG_NAME)

    @get:InputFiles
    val dependencyFiles
        get() = dependencyConfig.resolve().filter { isOsgiBundle(it) }

    init {
        group = "OSGi"
        description = "Create OSGi bundle"
        extension = "bundle"

        project.afterEvaluate {
            into("osgi", { spec -> spec.from(metadataFile) })
            into("osgi/artifact", { spec -> spec.from(artifactFile) })
            into("osgi/dependencies", { spec -> spec.from(dependencyFiles) })
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
        generateMetadata()
        super.copy()
    }

    private fun generateMetadata() {
        GFileUtils.mkdirs(metadataFile.parentFile)

        val descriptor = BundleDescriptor.of(project)
        val json = Klaxon().toJsonString(descriptor) // TODO pretty print

        metadataFile.printWriter().use { it.print(json) }
    }

}