package com.neva.osgi.toolkit.gradle.instance

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.util.GFileUtils
import org.zeroturnaround.zip.ZipUtil
import java.io.File

// TODO shadow jar: distribution-launcher
// TODO put osgiPackage into OSGI-INF/packages/app.jar (then distribution-launcher will install it using package-manager)
// TODO put framework-launcher into OSGI-INF/distribution/framework-launcher.jar
// TODO put package-manager into OSGI-INF/distribution/bundle/package-manager.jar
open class DistributionTask : Jar() {

    companion object {
        const val NAME = "osgiDistribution"
    }

    init {
        group = "OSGi"
        description = "Creates OSGi distribution"
        classifier = "distribution"

        project.afterEvaluate {
            from(project.resolveDependency(packageManager), { it.into("OSGI-INF/packages") })
            from(distributionDir, { it.into("OSGI-INF/distribution") })
        }
    }

    @Input
    var packageManager: Any = "com.neva.osgi.toolkit:package-manager:1.0.0"

    @Input
    var distributionLauncher: Any = "com.neva.osgi.toolkit:distribution-launcher:1.0.0"

    @Input
    var frameworkLauncher: Any = "com.neva.osgi.toolkit:framework-launcher:1.0.0"

    @Input
    var distribution: Any = mapOf(
            "group" to "org.apache.felix",
            "name" to "org.apache.felix.main.distribution",
            "version" to "5.6.10",
            "ext" to "zip"
    )

    @OutputDirectory
    val distributionDir = project.file("build/tmp/osgi/distribution")

    @TaskAction
    override fun copy() {
        logger.info("Creating OSGi distribution")

        logger.info("Downloading and extracting distribution: $distribution")
        unpackDistribution()

        logger.info("Downloading framework launcher and including it into distribution: $frameworkLauncher")
        includeFrameworkLauncher()

        logger.info("Composing distribution jar")
        super.copy()
        logger.info("Created OSGi distribution successfully.")
    }

    private fun unpackDistribution() {
        val distributionZip = project.resolveDependency(distribution)
        ZipUtil.unpack(distributionZip, distributionDir)
    }

    private fun includeFrameworkLauncher() {
        val source = project.resolveDependency(frameworkLauncher)
        val target = File(distributionDir, "bin/${source.name}")

        GFileUtils.mkdirs(source.parentFile)
        FileUtils.copyFile(source, target)
    }

    fun Project.resolveDependency(dependencyNotation: Any): File {
        return configurations.detachedConfiguration(dependencies.create(dependencyNotation)).singleFile
    }

}
