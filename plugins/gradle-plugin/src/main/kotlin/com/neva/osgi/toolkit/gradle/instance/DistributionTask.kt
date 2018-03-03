package com.neva.osgi.toolkit.gradle.instance

import com.neva.osgi.toolkit.commons.domain.Instance
import com.neva.osgi.toolkit.commons.utils.ResourceOperations
import com.neva.osgi.toolkit.gradle.internal.Formats
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
        classifier = "distribution"
        extension = "jar"

        project.afterEvaluate {
            from(project.zipTree(project.resolveDependency(distributionLauncher)))
            from(distributionDir, { it.into(Instance.DISTRIBUTION_PATH) })
        }
    }

    @Input
    var distributionLauncher: Any = "com.neva.osgi.toolkit:distribution-launcher:1.0.0"

    @Input
    var frameworkLauncher: Any = "com.neva.osgi.toolkit:framework-launcher:1.0.0"

    @Input
    var frameworkLauncherMainClass: String = "com.neva.osgi.toolkit.framework.launcher.Launcher"

    @Input
    var packageManager: Any = "com.neva.osgi.toolkit:web-manager:1.0.0"

    @Input
    var distribution: Any = mapOf(
            "group" to "org.apache.felix",
            "name" to "org.apache.felix.main.distribution",
            "version" to "5.6.10",
            "ext" to "zip"
    )

    @get:OutputDirectory
    val distributionDir: File
        get() = project.file("${InstancePlugin.TMP_PATH}/${Instance.DISTRIBUTION_PATH}")

    @get:OutputFile
    val metadataFile: File
        get() = project.file("${InstancePlugin.TMP_PATH}/${Instance.METADATA_FILE}")

    @TaskAction
    override fun copy() {
        logger.info("Creating OSGi distribution")

        logger.info("Downloading and extracting distribution: $distribution")
        unpackDistribution()

        logger.info("Downloading framework launcher and including it into distribution: $frameworkLauncher")
        includeFrameworkLauncherJar()

        logger.info("Including framework launcher scripts")
        includeFrameworkLauncherScripts()

        logger.info("Downloading package manager and including it into distribution: $packageManager")
        includeWebManager()

        logger.info("Generating metadata file")
        generateMetadataFile()

        logger.info("Composing distribution jar")
        super.copy()
        logger.info("Created OSGi distribution successfully.")
    }

    // TODO detect if zip has at first level only dir (if yes, skip it, currently hardcoded)
    private fun unpackDistribution() {
        val distributionZip = project.resolveDependency(distribution)
        ZipUtil.unpack(distributionZip, distributionDir, { it.substringAfter("/") })
    }

    private fun includeFrameworkLauncherJar() {
        val source = project.resolveDependency(frameworkLauncher)
        val target = File(distributionDir, "bin/${source.name}")

        GFileUtils.mkdirs(source.parentFile)
        FileUtils.copyFile(source, target)
    }

    private fun includeFrameworkLauncherScripts() {
        ResourceOperations.copyDir("OSGI-INF/toolkit/distribution", distributionDir, true)
    }

    private fun includeWebManager() {
        val source = project.resolveDependency(packageManager)
        val target = File(distributionDir, "bundle/${source.name}")

        GFileUtils.mkdirs(source.parentFile)
        FileUtils.copyFile(source, target)
    }

    private fun generateMetadataFile() {
        GFileUtils.mkdirs(metadataFile.parentFile)

        val metadata = DistributionMetadata.of(project)
        val json = Formats.toJson(metadata)

        metadataFile.printWriter().use { it.print(json) }
    }

    private fun Project.resolveDependency(dependencyNotation: Any): File {
        val dependency = dependencies.create(dependencyNotation)
        val config = configurations.detachedConfiguration(dependency).apply { isTransitive = false }

        return config.singleFile
    }

}
