package com.neva.gradle.osgi.bundle

import aQute.bnd.gradle.BundleTaskConvention
import com.neva.gradle.osgi.internal.Formats
import com.neva.gradle.osgi.internal.Patterns
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
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

        const val TMP_PATH = "build/tmp/bundle"

        const val COMPILE_CONFIG_NAME = "bundleCompile"

        const val RUNTIME_CONFIG_NAME = "bundleRuntime"

        const val ALL_CONFIG_NAME = "bundleAll"

        const val PACKAGE_CONFIG_NAME = "bundle"

        const val PACKAGE_CACHE_PATH = ".gradle/bundle"

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
        val packageConfig = configurations.create(PACKAGE_CONFIG_NAME, { it.isTransitive = false })
        val compileConfig = configurations.create(COMPILE_CONFIG_NAME, { it.isTransitive = false })
        val runtimeConfig = configurations.create(RUNTIME_CONFIG_NAME, { it.isTransitive = false })
        val allConfig = configurations.create(ALL_CONFIG_NAME, { it.isTransitive = false })

        afterEvaluate {
            val compileConfigs = mutableListOf(packageConfig)
            val runtimeConfigs = mutableListOf(packageConfig)

            configurations.getByName(PACKAGE_CONFIG_NAME).resolve().forEach { bundle ->
                val tmpPath = rootProject.file("$PACKAGE_CACHE_PATH/${FileUtils.checksumCRC32(bundle)}")
                if (!tmpPath.exists()) {
                    if (!ZipUtil.containsEntry(bundle, "$OSGI_PATH/")) {
                        throw BundleException("Dependency is not a valid OSGi bundle: $bundle")
                    }
                    ZipUtil.iterate(bundle, { input, entry ->
                        if (!entry.name.endsWith("/") && Patterns.wildcard(entry.name, "$OSGI_PATH/*")) {
                            val file = project.file("$tmpPath/${entry.name}")
                            GFileUtils.mkdirs(file.parentFile)
                            IOUtils.copy(input, FileOutputStream(file))
                        }
                    })
                }

                val metadataJson = project.file("$tmpPath/$METADATA_FILE")
                        .bufferedReader().use { it.readText() }
                val metadata = Formats.fromJson(metadataJson, BundleMetadata::class.java)
                        ?: throw BundleException("Cannot parse bundle metadata from file: $bundle")

                metadata.allDependencies.map { dependency ->
                    val depConfigName = "bundle_${dependency.notation}"
                    val depConfig = configurations.create(depConfigName)
                    val source = project.files("$tmpPath/${BundlePlugin.DEPENDENCIES_PATH}/${dependency.path}")

                    dependencies.add(depConfigName, BundleSelfResolvingDependency(source, dependency))

                    if (dependency.configurations.compile) {
                        compileConfigs += depConfig
                    }
                    if (dependency.configurations.runtime) {
                        runtimeConfigs += depConfig
                    }
                }
            }

            compileConfig.setExtendsFrom(compileConfigs)
            configurations.getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME).extendsFrom(compileConfig)

            runtimeConfig.setExtendsFrom(runtimeConfigs)
            configurations.getByName(JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME).extendsFrom(runtimeConfig)

            allConfig.extendsFrom(compileConfig, runtimeConfig)
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