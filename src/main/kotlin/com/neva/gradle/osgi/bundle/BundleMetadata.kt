package com.neva.gradle.osgi.bundle

import org.gradle.api.Project

class BundleMetadata {

    companion object {
        fun of(project: Project): BundleMetadata {
            val dependenciesConfig = project.configurations.getByName(BundlePlugin.DEPENDENCIES_CONFIG_NAME)

            return BundleMetadata().apply {
                artifact = BundleDependency.from(project)
                dependencies = dependenciesConfig.allDependencies.map { BundleDependency.from(it) }
            }
        }
    }

    lateinit var artifact: BundleDependency

    var dependencies: List<BundleDependency> = listOf()

    val allDependencies: List<BundleDependency>
        get() = mutableListOf<BundleDependency>() + artifact + dependencies

}

