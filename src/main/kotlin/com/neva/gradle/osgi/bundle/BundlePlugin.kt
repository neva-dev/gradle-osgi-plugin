package com.neva.gradle.osgi.bundle

import aQute.bnd.gradle.BundleTaskConvention
import com.neva.gradle.osgi.internal.Formats
import com.neva.gradle.osgi.internal.Patterns
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.util.GFileUtils
import org.zeroturnaround.zip.ZipUtil
import java.io.FileOutputStream

open class BundlePlugin : Plugin<Project> {

    companion object {
        // TODO tmp should not be under build dir so that clean task cannot work
        // TODO place where to extract metadata in config phase should be different
        const val TMP_PATH = "build/tmp/bundle"

        const val DEPENDENCY_CONFIG_NAME = "bundle"

        const val DEPENDENCIES_CONFIG_NAME = "bundles"

        const val BND_FILE = "bnd.bnd"

        const val BND_CONVENTION_PLUGIN = "bundle"

        const val OSGI_PATH = "osgi"

        const val METADATA_FILE = "$OSGI_PATH/metadata.json"

        const val ARTIFACT_PATH = "$OSGI_PATH/artifact"

        const val DEPENDENCIES_PATH = "$OSGI_PATH/dependencies"

        const val PUBLICATION_MAVEN = "mavenBundle"
    }

    override fun apply(project: Project) {
        project.run({ configure() })
    }

    private fun Project.configure() {
        logger.info("Applying bundle plugin")

        setupDependentPlugins()
        setupConfigurations()
        setupDependentTasks()
        setupNewTasks()
    }

    private fun Project.setupDependentPlugins() {
        plugins.apply(BasePlugin::class.java)
        plugins.apply(JavaPlugin::class.java)
    }

    private fun Project.setupDependentTasks() {
        val jar = tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar

        setupJarDefaults(jar)
        setupJarBndTool(jar)
    }

    private fun Project.setupJarDefaults(jar: Jar) {
        // Do not generate empty JAR with there is no 'src' folder
        jar.enabled = file("src").exists()
    }

    private fun Project.setupJarBndTool(jar: Jar) {
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

            bundleConfigs += configurations.getByName(DEPENDENCY_CONFIG_NAME).resolve().flatMap { bundle ->
                val depConfigs = mutableListOf<Configuration>()

                if (ZipUtil.containsEntry(bundle, "osgi/")) {
                    ZipUtil.iterate(bundle, { input, entry ->
                        if (!entry.name.endsWith("/") && Patterns.wildcard(entry.name, "$OSGI_PATH/*")) {
                            val file = project.file("$TMP_PATH/${entry.name}")
                            GFileUtils.mkdirs(file.parentFile)
                            IOUtils.copy(input, FileOutputStream(file))
                        }
                    })

                    val metadataJson = project.file("$TMP_PATH/$METADATA_FILE")
                            .bufferedReader().use { it.readText() }
                    val metadata = Formats.fromJson(metadataJson, BundleMetadata::class.java)
                    if (metadata != null) {
                        depConfigs += metadata.allDependencies.map { dependency ->
                            val depConfigName = "bundle_${dependency.notation}"
                            val depConfig = configurations.create(depConfigName)

                            dependencies.add(depConfigName, BundleSelfResolvingDependency(project, dependency))

                            depConfig
                        }
                    } else {
                        throw BundleException("Cannot parse bundle metadata from file: $bundle")
                    }
                }

                depConfigs
            }

            bundlesConfig.setExtendsFrom(bundleConfigs)
            compileConfig.extendsFrom(bundlesConfig)
        }
    }

    private fun Project.setupNewTasks() {
        val build = tasks.getByName(LifecycleBasePlugin.BUILD_TASK_NAME)
        val bundle = tasks.create(BundleTask.NAME, BundleTask::class.java)
        val jar = tasks.getByName(JavaPlugin.JAR_TASK_NAME)

        bundle.dependsOn(jar)
        build.dependsOn(bundle)

        afterEvaluate {
            if (plugins.hasPlugin(MavenPublishPlugin::class.java)) {
                val publish = tasks.getByName(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME)
                publish.dependsOn(bundle)

                val publications = extensions.getByType(PublishingExtension::class.java).publications
                if (publications.findByName(PUBLICATION_MAVEN) == null) {
                    publications.create(PUBLICATION_MAVEN, MavenPublication::class.java, {
                        it.artifact(bundle.archivePath)
                    })
                }
            }
        }
    }
}