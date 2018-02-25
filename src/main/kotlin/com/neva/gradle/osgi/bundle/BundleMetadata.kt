package com.neva.gradle.osgi.bundle

import org.gradle.api.Project
import java.io.Serializable

class BundleMetadata : Serializable {

    companion object {
        fun of(project: Project): BundleMetadata {
            return BundleMetadata().apply {
                artifact = BundleDependency.from(project)
                dependencies = BundleDependency.manyFrom(project)
            }
        }
    }

    lateinit var artifact: BundleDependency

    var dependencies: List<BundleDependency> = listOf()

    val allDependencies: List<BundleDependency>
        get() = mutableListOf<BundleDependency>() + artifact + dependencies

}

