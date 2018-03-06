package com.neva.osgi.toolkit.gradle.instance

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

open class UpTask : DefaultTask() {

    init {
        group = "osgi"
        description = "Turn on OSGi instance"
    }

    @Internal
    val handler = InstanceHandler(project)

    @TaskAction
    fun up() {
        handler.up()
    }

    companion object {

        const val NAME = "osgiUp"

    }

}
