package com.neva.osgi.toolkit.gradle.pkg

import com.neva.osgi.toolkit.commons.domain.Package
import com.neva.osgi.toolkit.gradle.bundle.BundlePlugin
import com.neva.osgi.toolkit.gradle.internal.Formats
import com.neva.osgi.toolkit.gradle.internal.Patterns
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.util.GFileUtils
import org.zeroturnaround.zip.ZipUtil
import java.io.File
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

        // Do not generate empty JAR if there is no 'src' folder
        jar.enabled = file("src").exists()
    }

    private fun Project.setupConfigurations() {
        val packageConfig = configurations.create(PACKAGE_CONFIG_NAME, { it.isTransitive = false })
        val implementationConfig = configurations.create(IMPLEMENTATION_CONFIG_NAME, { it.isTransitive = false })
        val compileOnlyConfig = configurations.create(COMPILE_ONLY_CONFIG_NAME, { it.isTransitive = false })
        val runtimeClasspathConfig = configurations.create(RUNTIME_CLASSPATH_CONFIG_NAME, { it.isTransitive = false })
        val allConfig = configurations.create(ALL_CONFIG_NAME, { it.isTransitive = false })

        afterEvaluate {
            val implementationConfigs = mutableListOf(packageConfig)
            val compileOnlyConfigs = mutableListOf<Configuration>()
            val runtimeClasspathConfigs = mutableListOf<Configuration>()

            configurations.getByName(PACKAGE_CONFIG_NAME).resolve().forEach { bundle ->
                splitMetadataDependenciesIntoConfigurations(
                        bundle, compileOnlyConfigs, runtimeClasspathConfigs, implementationConfigs
                )
            }

            implementationConfig.setExtendsFrom(implementationConfigs)
            configurations.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME).extendsFrom(implementationConfig)

            compileOnlyConfig.setExtendsFrom(compileOnlyConfigs)
            configurations.getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME).extendsFrom(compileOnlyConfig)

            runtimeClasspathConfig.setExtendsFrom(runtimeClasspathConfigs)
            configurations.getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME).extendsFrom(runtimeClasspathConfig)

            allConfig.extendsFrom(compileOnlyConfig, runtimeClasspathConfig, implementationConfig)
        }
    }

    /**
     * Place each OSGi dependency in a separate configuration to be able to resolve
     * multiple versions of same bundle.
     */
    private fun Project.splitMetadataDependenciesIntoConfigurations(bundle: File, compileOnlyConfigs: MutableList<Configuration>, runtimeClasspathConfigs: MutableList<Configuration>, implementationConfigs: MutableList<Configuration>) {
        val tmpPath = rootProject.file("$PACKAGE_CACHE_PATH/${bundle.nameWithoutExtension}-${FileUtils.checksumCRC32(bundle)}")
        if (!tmpPath.exists()) {
            if (!ZipUtil.containsEntry(bundle, Package.METADATA_FILE)) {
                throw PackageException("Dependency is not a valid OSGi package: $bundle")
            }
            ZipUtil.iterate(bundle, { input, entry ->
                if (!entry.name.endsWith("/") && Patterns.wildcard(entry.name, "${Package.OSGI_PATH}/*")) {
                    val file = project.file("$tmpPath/${entry.name}")
                    GFileUtils.mkdirs(file.parentFile)
                    IOUtils.copy(input, FileOutputStream(file))
                }
            })
        }

        val metadataFile = project.file("$tmpPath/${Package.METADATA_FILE}")
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
            val source = project.files("$tmpPath/${Package.DEPENDENCIES_PATH}/${dependency.path}")

            dependencies.add(depConfigName, PackageSelfResolvingDependency(source, dependency))

            when (dependency.configuration) {
                PackageDependency.Configuration.COMPILE_ONLY -> compileOnlyConfigs += depConfig
                PackageDependency.Configuration.RUNTIME -> runtimeClasspathConfigs += depConfig
                PackageDependency.Configuration.COMPILE -> implementationConfigs += depConfig
            }
        }
    }

    private fun Project.setupNewTasks() {
        val build = tasks.getByName(LifecycleBasePlugin.BUILD_TASK_NAME)
        val pkg = tasks.create(PackageTask.NAME, PackageTask::class.java)
        val jar = tasks.getByName(JavaPlugin.JAR_TASK_NAME)

        pkg.dependsOn(jar)
        build.dependsOn(pkg)
    }

    companion object {

        const val TMP_PATH = "build/tmp/osgi/package"

        const val IMPLEMENTATION_CONFIG_NAME = "osgiCompile"

        const val COMPILE_ONLY_CONFIG_NAME = "osgiProvided"

        const val RUNTIME_CLASSPATH_CONFIG_NAME = "osgiRuntime"

        const val PACKAGE_CONFIG_NAME = "osgiPackage"

        const val ALL_CONFIG_NAME = "osgiAll"

        const val PACKAGE_CACHE_PATH = ".gradle/osgiPackages"

    }
}
