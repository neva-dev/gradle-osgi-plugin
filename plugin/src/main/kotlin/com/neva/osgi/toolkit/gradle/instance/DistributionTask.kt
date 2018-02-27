package com.neva.osgi.toolkit.gradle.instance

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
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
    }

    @Input
    var packageManager: Any = "com.neva.osgi.toolkit:distribution-launcher:1.0.0"

    @Input
    var frameworkLauncher: Any = "com.neva.osgi.toolkit:framework-launcher:1.0.0"

    @Input
    var distributionDependency: Any = mapOf(
            "group" to "org.apache.felix",
            "name" to "org.apache.felix.main.distribution",
            "version" to "5.6.10",
            "ext" to "zip"
    )

    @TaskAction
    fun make() {
        logger.info("Creating OSGi distribution")

        val pkgManagerBundle = project.resolveDependency(packageManager)
        logger.info("Resolved package manager bundle: $pkgManagerBundle [exists: ${pkgManagerBundle.exists()}]")

        val frameworkLauncherApp = project.resolveDependency(frameworkLauncher)
        logger.info("Resolved framework launcher app: $frameworkLauncherApp [exists: ${frameworkLauncherApp.exists()}]")

        val distributionLauncherApp = project.resolveDependency(distributionDependency)
        logger.info("Resolved distribution launcher app: $distributionLauncherApp [exists: ${distributionLauncherApp.exists()}]")
    }

    fun Project.resolveDependency(dependencyNotation: Any): File {
        return configurations.detachedConfiguration(dependencies.create(dependencyNotation)).singleFile
    }

}
