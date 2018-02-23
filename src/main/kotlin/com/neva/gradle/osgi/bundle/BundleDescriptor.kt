package com.neva.gradle.osgi.bundle

import org.gradle.api.artifacts.Configuration

class BundleDescriptor {

    companion object {
        fun from(config: Configuration) {
            config.allDependencies.map { BundleDependency.from(it) }
        }
    }

    lateinit var dependencies: List<BundleDependency>

}

