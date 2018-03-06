package com.neva.osgi.toolkit.gradle.instance

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

open class CreateTask : DefaultTask() {

    init {
        group = "osgi"
        description = "Create OSGi instance from distribution"
    }

    @Internal
    val handler = InstanceHandler(project)

    @get:OutputFile
    val lock = handler.lock("create")

    @TaskAction
    fun create() {
        handler.create()
        lock.lock()
    }

    companion object {

        const val NAME = "osgiCreate"

    }

}
