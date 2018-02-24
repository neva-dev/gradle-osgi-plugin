package com.neva.gradle.osgi.bundle

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

class BundleDependency {

    companion object {

        fun from(project: Project): BundleDependency {
            if (project.group !is String || (project.group as String).isBlank()) {
                throw BundleException("Project group cannot be blank in $project")
            }

            if (project.group !is String || (project.group as String).isBlank()) {
                throw BundleException("Project version cannot be blank in $project")
            }

            return BundleDependency().apply {
                group = project.group as String
                name = project.name as String
                version = project.version as String
            }
        }

        fun from(dependency: Dependency): BundleDependency {
            if (dependency.group.isNullOrBlank()) {
                throw BundleException("Dependency group cannot be blank in $dependency")
            }

            if (dependency.version.isNullOrBlank()) {
                throw BundleException("Dependency version cannot be blank in $dependency")
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

    val notation: String
        get() = "$group:$name:$version"

}