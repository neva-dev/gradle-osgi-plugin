package com.neva.gradle.osgi.bundle

import org.gradle.api.artifacts.Dependency

class BundleDependency {

    companion object {
        fun from(dependency: Dependency): BundleDependency {
            return BundleDependency().apply {
                group = dependency.group
                name = dependency.name
                version = dependency.version
            }
        }
    }

    var group: String? = null

    lateinit var name : String

    var version: String? = null

    val notation: String
        get() = "$group:$name:$version"

}