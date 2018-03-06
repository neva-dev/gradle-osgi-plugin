package com.neva.osgi.toolkit.gradle.instance

import com.neva.osgi.toolkit.commons.domain.Instance
import com.neva.osgi.toolkit.gradle.internal.Formats
import com.neva.osgi.toolkit.gradle.internal.ResourceOperations
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Zip
import org.gradle.util.GFileUtils
import org.zeroturnaround.zip.ZipUtil
import java.io.File

open class DistributionTask : Zip() {

    companion object {
        const val NAME = "osgiDistribution"
    }

    init {
        group = "OSGi"
        description = "Creates OSGi distribution"
        destinationDir = project.file("build/osgi/distributions")
        extension = "jar"

        project.afterEvaluate {
            from(project.zipTree(project.resolveDependency(distributionLauncher)))
            from(distributionDir, { it.into(Instance.DISTRIBUTION_PATH) })
        }
    }

    @Input
    var distribution: Any = mapOf(
            "group" to "org.apache.felix",
            "name" to "org.apache.felix.main.distribution",
            "version" to "5.6.10",
            "ext" to "zip"
    )

    @Input
    var distributionLauncher: Any = "com.neva.osgi.toolkit:distribution-launcher:1.0.0"

    @Input
    var frameworkLauncher: Any = "com.neva.osgi.toolkit:framework-launcher:1.0.0"

    @Input
    var frameworkLauncherMainClass: String = "com.neva.osgi.toolkit.framework.launcher.Launcher"

    @Input
    var baseBundles: List<Any> = mutableListOf(
            "org.osgi:osgi.core:6.0.0",
            "org.apache.felix:org.apache.felix.scr:2.0.14",
            "org.apache.felix:org.apache.felix.fileinstall:3.6.4",
            "com.neva.osgi.toolkit:web-manager:1.0.0"
    )

    @get:OutputDirectory
    val distributionDir: File
        get() = project.file("${InstancePlugin.TMP_PATH}/${Instance.DISTRIBUTION_PATH}")

    @get:OutputFile
    val metadataFile: File
        get() = project.file("${InstancePlugin.TMP_PATH}/${Instance.METADATA_FILE}")

    @TaskAction
    override fun copy() {
        logger.info("Creating OSGi distribution.")

        unpackDistribution()
        includeFrameworkLauncherJar()
        includeFrameworkLauncherScripts()
        includeBaseBundles()
        generateMetadataFile()
        packDistribution()

        logger.info("Created OSGi distribution successfully.")
    }

    private fun packDistribution() {
        logger.info("Composing distribution jar")
        super.copy()
    }

    // TODO detect if zip has at first level only dir (if yes, skip it, currently hardcoded)
    private fun unpackDistribution() {
        logger.info("Downloading and extracting distribution")

        val distributionZip = project.resolveDependency(distribution)
        ZipUtil.unpack(distributionZip, distributionDir, { it.substringAfter("/") })
    }

    private fun includeFrameworkLauncherJar() {
        logger.info("Downloading framework launcher and including it into distribution")

        project.includeDependency(frameworkLauncher, "bin")
    }

    private fun includeFrameworkLauncherScripts() {
        logger.info("Including framework launcher scripts")

        ResourceOperations.copyDir("OSGI-INF/toolkit/distribution", distributionDir, true)
    }

    private fun includeBaseBundles() {
        logger.info("Downloading base bundles and including them into distribution")

        baseBundles.forEach { project.includeDependency(it, "bundle") }
    }

    private fun generateMetadataFile() {
        logger.info("Generating metadata file")

        GFileUtils.mkdirs(metadataFile.parentFile)

        val metadata = DistributionMetadata.of(project)
        val json = Formats.toJson(metadata)

        metadataFile.printWriter().use { it.print(json) }
    }

    private fun Project.includeDependency(dependencyNotation: Any, path: String) {
        val source = project.resolveDependency(dependencyNotation)
        val target = File(distributionDir, "$path/${source.name}")

        logger.info("Copying distribution dependency '${source.name}' into '$path'")

        GFileUtils.mkdirs(source.parentFile)
        FileUtils.copyFile(source, target)
    }

    private fun Project.resolveDependency(dependencyNotation: Any): File {
        logger.info("Resolving distribution dependency '$dependencyNotation' into '$path'")

        val dependency = dependencies.create(dependencyNotation)
        val config = configurations.detachedConfiguration(dependency).apply {
            description = dependencyNotation.toString()
            isTransitive = false
        }

        return config.singleFile
    }

}
