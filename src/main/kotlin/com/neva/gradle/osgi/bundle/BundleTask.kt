package com.neva.gradle.osgi.bundle

import com.beust.klaxon.Klaxon
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.util.GFileUtils
import java.io.File

open class BundleTask : Jar() {

    companion object {
        const val NAME = "osgiBundle"
    }

    init {
        group = "OSGi"
        description = "Create OSGi bundle"

        into("bundle", { spec ->
            spec.from(metadataFile)
        })
    }

    @Internal
    val metadataFile: File = project.file("build/${BundlePlugin.METADATA_FILE}")

    @TaskAction
    override fun copy() {
        generateMetadata()
        super.copy()
    }

    private fun generateMetadata() {
        GFileUtils.mkdirs(metadataFile.parentFile)

        val descriptor = BundleDescriptor.from(project.configurations.getByName("bundles"))
        val json = Klaxon().toJsonString(descriptor)

        metadataFile.printWriter().use { it.print(json) }
    }

}