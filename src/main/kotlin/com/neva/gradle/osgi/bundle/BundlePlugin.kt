package com.neva.gradle.osgi.bundle

import aQute.bnd.gradle.BundleTaskConvention
import com.beust.klaxon.Klaxon
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.zeroturnaround.zip.ZipUtil

open class BundlePlugin : Plugin<Project> {

    companion object {
        const val METADATA_FILE = "build/tmp/osgi/metadata.json"

        const val DEPENDENCY_CONFIG_NAME = "bundle"

        const val DEPENDENCIES_CONFIG_NAME = "bundles"

        const val BND_FILE = "bnd.bnd"

        const val BND_CONVENTION_PLUGIN = "bundle"
    }

    override fun apply(project: Project) {
        project.run({ configure() })
    }

    private fun Project.configure() {
        logger.info("Applying bundle plugin")

        setupDependentPlugins()
        setupBndTool()
        setupConfigurations()
        setupTasks()
    }

    private fun Project.setupDependentPlugins() {
        plugins.apply(BasePlugin::class.java)
        plugins.apply(JavaPlugin::class.java)
    }

    private fun Project.setupBndTool() {
        val jar = tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar
        val bundleConvention = BundleTaskConvention(jar)

        convention.plugins[BND_CONVENTION_PLUGIN] = bundleConvention

        val bndFile = file(BND_FILE)
        if (bndFile.isFile) {
            bundleConvention.setBndfile(bndFile)
        }

        jar.doLast {
            bundleConvention.buildBundle()
        }
    }

    private fun Project.setupConfigurations() {
        val bundleConfig = configurations.create(DEPENDENCY_CONFIG_NAME, { it.isTransitive = false })
        val bundlesConfig = configurations.create(DEPENDENCIES_CONFIG_NAME, { it.isTransitive = false })
        val compileConfig = configurations.getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)

        afterEvaluate {
            val bundleConfigs = mutableListOf(bundleConfig)

            bundleConfigs += configurations.getByName("bundle").resolve().flatMap { bundle ->
                val depConfigs = mutableListOf<Configuration>()

                if (ZipUtil.containsEntry(bundle, METADATA_FILE)) {
                    val input = ZipUtil.unpackEntry(bundle, METADATA_FILE).inputStream()
                    val descriptor = Klaxon().parse<BundleDescriptor>(input)
                    if (descriptor != null) {
                        depConfigs += descriptor.dependencies.map { dependency ->
                            val depConfigName = "bundle_${dependency.notation}"
                            val depConfig = configurations.create(depConfigName)
                            dependencies.add(depConfigName, dependency.notation)

                            depConfig
                        }
                    } else {
                        throw BundleException("Cannot parse bundle descriptor from file: $bundle")
                    }
                }

                depConfigs
            }

            bundlesConfig.setExtendsFrom(bundleConfigs)
            compileConfig.extendsFrom(bundlesConfig)
        }
    }

    private fun Project.setupTasks() {
        val build = tasks.getByName(LifecycleBasePlugin.BUILD_TASK_NAME)
        val bundle = tasks.create(BundleTask.NAME, BundleTask::class.java)
        val jar = tasks.getByName(JavaPlugin.JAR_TASK_NAME)

        bundle.dependsOn(jar)
        build.dependsOn(bundle)
    }

}