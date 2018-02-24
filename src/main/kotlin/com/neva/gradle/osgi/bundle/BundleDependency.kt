package com.neva.gradle.osgi.bundle

import com.fasterxml.jackson.annotation.JsonIgnore
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

class BundleDependency {

    companion object {

        fun from(project: Project): BundleDependency {
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
            }
        }

        private fun Dependency.displayName(): String {
            return "[group=$group,name=$name,version=$version]"
        }

        fun from(dependency: Dependency): BundleDependency {
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
            }
        }
    }

    lateinit var group: String

    lateinit var name: String

    lateinit var version: String

    @get:JsonIgnore
    val notation: String
        get() = "$group:$name:$version"

    // TODO $group/$name-$version.jar // to avoid collisions
    @get:JsonIgnore
    val jarPath: String
        get() = "$name-$version.jar"

}