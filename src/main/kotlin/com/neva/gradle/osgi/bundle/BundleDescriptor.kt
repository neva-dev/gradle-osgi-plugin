package com.neva.gradle.osgi.bundle

import org.gradle.api.artifacts.Configuration

class BundleDescriptor {

    companion object {
        fun from(config: Configuration): BundleDescriptor {
            return BundleDescriptor().apply {
                dependencies = config.allDependencies.map { BundleDependency.from(it) }
            }
        }
    }

    lateinit var dependencies: List<BundleDependency>

}

