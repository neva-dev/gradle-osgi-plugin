package com.neva.osgi.toolkit.gradle.bundle

import aQute.bnd.gradle.BundleTaskConvention
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test

open class BundlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.configure()
    }

    private fun Project.configure() {
        logger.info("Applying OSGi bundle plugin")

        applyDependentPlugins()
        setupJavaPlugin()
    }

    private fun Project.applyDependentPlugins() {
        plugins.apply(BasePlugin::class.java)
        plugins.apply(JavaPlugin::class.java)
    }

    private fun Project.setupJavaPlugin() {
        val javaConvention = convention.getPlugin(JavaPluginConvention::class.java)
        val jar = tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar
        val test = tasks.getByName(JavaPlugin.TEST_TASK_NAME) as Test

        // Nice defaults
        javaConvention.sourceCompatibility = JavaVersion.VERSION_1_8
        javaConvention.targetCompatibility = JavaVersion.VERSION_1_8

        tasks.withType(JavaCompile::class.java, {
            it.options.encoding = "UTF-8"
            it.options.compilerArgs = it.options.compilerArgs + "-Xlint:deprecation"
            it.options.isIncremental = true
        })

        // Do not force to redeclare 'compile' and 'testCompile' dependencies
        val testSourceSet = javaConvention.sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME)
        val compileOnlyConfig = configurations.getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)

        testSourceSet.compileClasspath += compileOnlyConfig
        testSourceSet.runtimeClasspath += compileOnlyConfig

        // Make SCR metadata be generated while unit testing
        gradle.projectsEvaluated { test.classpath += project.files(jar.archivePath) }

        // Use BND tool to make valid OSGi bundle basing on JAR
        val bundleConvention = BundleTaskConvention(jar)
        this.convention.plugins[BND_CONVENTION_PLUGIN] = bundleConvention

        val bndFile = file(BND_FILE)
        if (bndFile.isFile) {
            bundleConvention.setBndfile(bndFile)
        }

        jar.doLast {
            bundleConvention.buildBundle()
        }
    }

    companion object {

        const val BND_FILE = "bnd.bnd"

        const val BND_CONVENTION_PLUGIN = "bundle"

    }
}
