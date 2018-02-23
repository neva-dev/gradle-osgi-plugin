package com.neva.gradle.osgi.bundle

import com.beust.klaxon.Klaxon
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Zip
import org.gradle.util.GFileUtils
import java.io.File

open class BundleTask : Zip() {

    companion object {
        const val NAME = "osgiBundle"
    }

    init {
        group = "OSGi"
        description = "Create OSGi bundle"

        from(metadataFile)
    }

    @get:OutputFile
    val metadataFile: File
          get() = project.file("build/${BundlePlugin.METADATA_FILE}")

    @TaskAction
    override fun copy() {
        generateMetadata()
        super.copy()
    }

    private fun generateMetadata() {
        GFileUtils.mkdirs(metadataFile.parentFile)

        val config = project.configurations.getByName(BundlePlugin.DEPENDENCIES_CONFIG_NAME)
        val descriptor = BundleDescriptor.from(config)
        val json = Klaxon().toJsonString(descriptor)

        metadataFile.printWriter().use { it.print(json) }
    }

}