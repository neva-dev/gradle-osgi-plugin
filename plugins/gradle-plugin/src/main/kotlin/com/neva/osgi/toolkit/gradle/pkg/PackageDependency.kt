package com.neva.osgi.toolkit.gradle.pkg

import com.fasterxml.jackson.annotation.JsonIgnore
import com.neva.osgi.toolkit.gradle.internal.displayName
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import java.io.File
import java.io.Serializable

class PackageDependency : Serializable {

    companion object {

        fun artifact(project: Project): PackageDependency {
            val bundleTask = project.tasks.getByName(PackageTask.NAME) as PackageTask
            val file = bundleTask.artifactFile

            if (project.group !is String || (project.group as String).isBlank()) {
                throw PackageException("Project group cannot be blank: $project")
            }

            if (project.group !is String || (project.group as String).isBlank()) {
                throw PackageException("Project version cannot be blank: $project")
            }

            return PackageDependency().apply {
                group = project.group as String
                name = project.name as String
                version = project.version as String
                path = "$group/${file.name}"
            }
        }

        fun dependencies(project: Project): List<PackageDependency> {
            val task = project.tasks.getByName(PackageTask.NAME) as PackageTask

            return task.dependencies.map { from(project, it.key, it.value) }
        }

        fun from(project: Project, dependency: Dependency, file: File): PackageDependency {
            if (dependency.group.isNullOrBlank()) {
                throw PackageException("Dependency group cannot be blank: ${dependency.displayName()}")
            }

            if (dependency.version.isNullOrBlank()) {
                throw PackageException("Dependency version cannot be blank: ${dependency.displayName()}")
            }

            return PackageDependency().apply {
                group = dependency.group!!
                name = dependency.name
                version = dependency.version!!
                path = "$group/${file.name}"
                configuration = Configuration.of(project, dependency)
            }
        }

    }

    lateinit var path: String

    lateinit var group: String

    lateinit var name: String

    lateinit var version: String

    var configuration = Configuration.COMPILE

    @get:JsonIgnore
    val notation: String
        get() = "$group:$name:$version"



    enum class Configuration {
        COMPILE_ONLY,
        COMPILE,
        RUNTIME;

        companion object {
            fun of(project: Project, dependency: Dependency): Configuration {
                return when {
                    check(project, dependency, PackagePlugin.COMPILE_ONLY_CONFIG_NAME) -> COMPILE_ONLY
                    check(project, dependency, PackagePlugin.RUNTIME_CLASSPATH_CONFIG_NAME) -> RUNTIME
                    else -> COMPILE
                }
            }

            private fun check(project: Project, dependency: Dependency, config: String): Boolean {
                return project.configurations.getByName(config).allDependencies.contains(dependency)
            }
        }
    }

}
