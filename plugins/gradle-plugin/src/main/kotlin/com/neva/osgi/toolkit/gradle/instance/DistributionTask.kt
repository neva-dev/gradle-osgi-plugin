package com.neva.osgi.toolkit.gradle.instance

import com.neva.osgi.toolkit.commons.domain.Instance
import com.neva.osgi.toolkit.commons.domain.Package
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
    var distributionLauncher: Any = "com.neva.osgi.toolkit:distribution-launcher:1.0.0"

    @Input
    var frameworkLauncher: Any = "com.neva.osgi.toolkit:framework-launcher:1.0.0"

    @Input
    var frameworkLauncherMainClass: String = "com.neva.osgi.toolkit.framework.launcher.Launcher"

    @Input
    var basePackage: Any = "com.neva.osgi.toolkit:base:1.0.0"

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
        logger.info("Creating OSGi distribution.")

        unpackDistribution()
        includeFrameworkLauncherJar()
        includeFrameworkLauncherScripts()
        includeBasePackage()
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

        val source = project.resolveDependency(frameworkLauncher)
        val target = File(distributionDir, "bin/${source.name}")

        GFileUtils.mkdirs(source.parentFile)
        FileUtils.copyFile(source, target)
    }

    private fun includeFrameworkLauncherScripts() {
        logger.info("Including framework launcher scripts")

        ResourceOperations.copyDir("OSGI-INF/toolkit/distribution", distributionDir, true)
    }

    private fun includeBasePackage() {
        logger.info("Downloading base package and including it into distribution")

        val baseZip = project.resolveDependency(basePackage)

        ZipUtil.unpack(baseZip, distributionDir, { name ->
            if (name.startsWith(Package.DEPENDENCIES_PATH + "/")) {
                "bundle/${name.substringAfter(Package.DEPENDENCIES_PATH + "/")}"
            } else {
                null
            }
        })
    }

    private fun generateMetadataFile() {
        logger.info("Generating metadata file")

        GFileUtils.mkdirs(metadataFile.parentFile)

        val metadata = DistributionMetadata.of(project)
        val json = Formats.toJson(metadata)

        metadataFile.printWriter().use { it.print(json) }
    }

    private fun Project.resolveDependency(dependencyNotation: Any): File {
        logger.info("Resolving distribution dependency: $dependencyNotation")

        val dependency = dependencies.create(dependencyNotation)
        val config = configurations.detachedConfiguration(dependency).apply {
            description = dependencyNotation.toString()
            isTransitive = false
        }

        return config.singleFile
    }

}
