package com.neva.gradle.osgi.bundle

import org.gradle.api.Project

class BundleDescriptor {

    companion object {
        fun of(project: Project): BundleDescriptor {
            val dependenciesConfig = project.configurations.getByName(BundlePlugin.DEPENDENCIES_CONFIG_NAME)

            return BundleDescriptor().apply {
                artifact = BundleDependency.from(project)
                dependencies = dependenciesConfig.allDependencies.map { BundleDependency.from(it) }
            }
        }
    }

    lateinit var artifact: BundleDependency

    lateinit var dependencies: List<BundleDependency>

}

