package com.neva.gradle.osgi.bundle

import com.beust.klaxon.Klaxon
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.zeroturnaround.zip.ZipUtil

open class BundlePlugin : Plugin<Project> {

    companion object {
        const val METADATA_FILE = "bundle/metadata.json"

        const val DEPENDENCY_CONFIG_NAME = "bundle"

        const val DEPENDENCIES_CONFIG_NAME = "bundles"
    }

    override fun apply(project: Project) {
        project.run({ configure() })
    }

    private fun Project.configure() {
        logger.info("Applying bundle plugin")

        setupDependentPlugins()
        setupConfigurations()
        setupTasks()
    }

    private fun Project.setupDependentPlugins() {
        plugins.apply(BasePlugin::class.java)
        plugins.apply(JavaPlugin::class.java)
    }

    private fun Project.setupConfigurations() {
        val bundleConfig = configurations.create(DEPENDENCY_CONFIG_NAME, { it.isTransitive = false })
        val bundlesConfig = configurations.create(DEPENDENCIES_CONFIG_NAME, { it.isTransitive = false })
        val compileConfig = configurations.getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)

        gradle.projectsEvaluated {
            val bundleConfigs = mutableListOf(bundleConfig)

            bundleConfigs += configurations.getByName("bundle").resolve().flatMap { bundle ->
                val depConfigs = mutableListOf<Configuration>()

                if (ZipUtil.containsEntry(bundle, METADATA_FILE)) {
                    val input = ZipUtil.unpackEntry(bundle, METADATA_FILE).inputStream()
                    val descriptor = Klaxon().parse<BundleDescriptor>(input)
                    if (descriptor != null) {
                        // TODO instead of just adding dependency / notation we could add embedded bundle directly
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
        tasks.create(BundleTask.NAME, BundleTask::class.java)
    }

}