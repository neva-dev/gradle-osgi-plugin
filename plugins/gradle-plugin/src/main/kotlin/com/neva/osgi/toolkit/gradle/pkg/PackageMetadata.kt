package com.neva.osgi.toolkit.gradle.pkg

import com.fasterxml.jackson.annotation.JsonIgnore
import org.gradle.api.Project
import java.io.Serializable

class PackageMetadata : Serializable {

    companion object {
        fun of(project: Project): PackageMetadata {
            return PackageMetadata().apply {
                artifact = PackageDependency.artifact(project)
                dependencies = PackageDependency.dependencies(project)
            }
        }
    }

    lateinit var artifact: PackageDependency

    var dependencies: List<PackageDependency> = listOf()

    @get:JsonIgnore
    val allDependencies: List<PackageDependency>
        get() = mutableListOf<PackageDependency>() + artifact + dependencies

}

