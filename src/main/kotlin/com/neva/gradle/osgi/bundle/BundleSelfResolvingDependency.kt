package com.neva.gradle.osgi.bundle

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.artifacts.dependencies.AbstractDependency
import org.gradle.api.internal.artifacts.dependencies.SelfResolvingDependencyInternal
import org.gradle.api.internal.tasks.DefaultTaskDependency
import org.gradle.api.tasks.TaskDependency
import java.io.File

// TODO replace with dependency.fileTree that matches single file (or not)
class BundleSelfResolvingDependency(
        private val project: Project,
        private val dependency: BundleDependency
) : AbstractDependency(), FileCollectionDependency, SelfResolvingDependencyInternal {

    override fun getTargetComponentId(): ComponentIdentifier? {
        return ComponentIdentifier { dependency.jarPath }
    }

    override fun getFiles(): FileCollection {
        return project.files(source)
    }

    override fun resolve(): MutableSet<File> {
        return mutableSetOf(source)
    }

    private val source: File
        get() {
            return project.file("${BundlePlugin.TMP_PATH}/${BundlePlugin.DEPENDENCIES_PATH}/${dependency.jarPath}")
        }

    override fun resolve(transitive: Boolean): MutableSet<File> {
        return resolve()
    }

    override fun getGroup(): String? {
        return dependency.group
    }

    override fun getName(): String {
        return dependency.name
    }

    override fun getVersion(): String? {
        return dependency.version
    }

    override fun contentEquals(dependency: Dependency?): Boolean {
        if (dependency is BundleSelfResolvingDependency) {
            return source == dependency.source
        }

        return false
    }

    override fun copy(): Dependency {
        return BundleSelfResolvingDependency(project, dependency)
    }

    override fun getBuildDependencies(): TaskDependency {
        return DefaultTaskDependency()
    }
}