package com.neva.osgi.toolkit.gradle.pkg

import org.gradle.api.Project
import java.io.Serializable

class PackageMetadata : Serializable {

    companion object {
        fun of(project: Project): PackageMetadata {
            return PackageMetadata().apply {
                artifact = PackageDependency.from(project)
                dependencies = PackageDependency.manyFrom(project)
            }
        }
    }

    lateinit var artifact: PackageDependency

    var dependencies: List<PackageDependency> = listOf()

    val allDependencies: List<PackageDependency>
        get() = mutableListOf<PackageDependency>() + artifact + dependencies

}

