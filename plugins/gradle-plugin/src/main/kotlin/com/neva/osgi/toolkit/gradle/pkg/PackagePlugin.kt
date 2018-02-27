package com.neva.osgi.toolkit.gradle.pkg

import com.neva.osgi.toolkit.gradle.bundle.BundlePlugin
import com.neva.osgi.toolkit.gradle.internal.Formats
import com.neva.osgi.toolkit.gradle.internal.Patterns
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
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

open class PackagePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.configure()
    }

    private fun Project.configure() {
        logger.info("Applying OSGi package plugin")

        applyDependentPlugins()
        setupConfigurations()
        setupDependentTasks()
        setupNewTasks()
    }

    private fun Project.applyDependentPlugins() {
        plugins.apply(BundlePlugin::class.java)
    }

    private fun Project.setupDependentTasks() {
        val jar = tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar

        // Do not generate empty JAR with there is no 'src' folder
        jar.enabled = file("src").exists()
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
                val tmpPath = rootProject.file("${PACKAGE_CACHE_PATH}/${bundle.nameWithoutExtension}-${FileUtils.checksumCRC32(bundle)}")
                if (!tmpPath.exists()) {
                    if (!ZipUtil.containsEntry(bundle, PACKAGE_FILE)) {
                        throw PackageException("Dependency is not a valid OSGi package: $bundle")
                    }
                    ZipUtil.iterate(bundle, { input, entry ->
                        if (!entry.name.endsWith("/") && Patterns.wildcard(entry.name, "${OSGI_PATH}/*")) {
                            val file = project.file("$tmpPath/${entry.name}")
                            GFileUtils.mkdirs(file.parentFile)
                            IOUtils.copy(input, FileOutputStream(file))
                        }
                    })
                }

                val metadataFile = project.file("$tmpPath/${PACKAGE_FILE}")
                if (!metadataFile.exists()) {
                    throw PackageException("OSGi package cache has been corrupted, because no metadata file found for: $bundle")
                }

                val metadataJson = metadataFile
                        .bufferedReader().use { it.readText() }
                val metadata = Formats.fromJson(metadataJson, PackageMetadata::class.java)
                        ?: throw PackageException("Cannot parse bundle metadata from file: $bundle")

                metadata.allDependencies.map { dependency ->
                    val depConfigName = "bundle_${dependency.notation}"
                    val depConfig = configurations.create(depConfigName)
                    val source = project.files("$tmpPath/${DEPENDENCIES_PATH}/${dependency.path}")

                    dependencies.add(depConfigName, PackageSelfResolvingDependency(source, dependency))

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
        val bundle = tasks.create(PackageTask.NAME, PackageTask::class.java)
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

    companion object {

        const val TMP_PATH = "build/tmp/osgiPackage"

        const val COMPILE_CONFIG_NAME = "osgiCompile"

        const val RUNTIME_CONFIG_NAME = "osgiRuntime"

        const val ALL_CONFIG_NAME = "osgiAll"

        const val PACKAGE_CONFIG_NAME = "osgiPackage"

        const val PACKAGE_CACHE_PATH = ".gradle/osgiPackages"

        const val OSGI_PATH = "OSGI-INF"

        const val PACKAGE_FILE = "$OSGI_PATH/package.json"

        const val DISTRIBUTION_FILE = "$OSGI_PATH/distribution.json"

        const val ARTIFACT_PATH = "$OSGI_PATH/artifact"

        const val DEPENDENCIES_PATH = "$OSGI_PATH/dependencies"

        const val PUBLICATION_MAVEN = "mavenOsgiPackage"

    }
}
