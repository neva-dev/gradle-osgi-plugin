package com.neva.gradle.osgi.bundle

import com.fasterxml.jackson.annotation.JsonIgnore
import com.neva.gradle.osgi.internal.displayName
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import java.io.File
import java.io.Serializable

class BundleDependency : Serializable {

    companion object {

        fun from(project: Project): BundleDependency {
            val bundleTask = project.tasks.getByName(BundleTask.NAME) as BundleTask
            val file = bundleTask.artifactFile

            if (project.group !is String || (project.group as String).isBlank()) {
                throw BundleException("Project group cannot be blank: $project")
            }

            if (project.group !is String || (project.group as String).isBlank()) {
                throw BundleException("Project version cannot be blank: $project")
            }

            return BundleDependency().apply {
                group = project.group as String
                name = project.name as String
                version = project.version as String
                path = "$group/${file.name}"
            }
        }

        fun manyFrom(project: Project): List<BundleDependency> {
            val config = project.configurations.getByName(BundlePlugin.ALL_CONFIG_NAME)
            return config.allDependencies.map {
                val file = config.files(it).single()
                from(project, it, file)
            }
        }

        fun from(project: Project, dependency: Dependency, file: File): BundleDependency {
            if (dependency.group.isNullOrBlank()) {
                throw BundleException("Dependency group cannot be blank: ${dependency.displayName()}")
            }

            if (dependency.version.isNullOrBlank()) {
                throw BundleException("Dependency version cannot be blank: ${dependency.displayName()}")
            }

            return BundleDependency().apply {
                group = dependency.group!!
                name = dependency.name
                version = dependency.version!!
                path = "$group/${file.name}"
                configurations = Configurations.of(project, dependency)
            }
        }

    }

    lateinit var path: String

    lateinit var group: String

    lateinit var name: String

    lateinit var version: String

    var configurations: Configurations = Configurations()

    @get:JsonIgnore
    val notation: String
        get() = "$group:$name:$version"


    class Configurations(val compile: Boolean = true, var runtime: Boolean = true) : Serializable {

        companion object {
            fun of(project: Project, dependency: Dependency): Configurations {
                return Configurations(
                        check(project, dependency, BundlePlugin.COMPILE_CONFIG_NAME),
                        check(project, dependency, BundlePlugin.RUNTIME_CONFIG_NAME)
                )
            }

            private fun check(project: Project, dependency: Dependency, config: String): Boolean {
                return project.configurations.getByName(config).allDependencies.contains(dependency)
            }
        }
    }

}