package com.neva.osgi.toolkit.gradle.instance

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

open class HaltTask : DefaultTask() {

    init {
        group = "osgi"
        description = "Turn off OSGi instance"
    }

    @Internal
    val handler = InstanceHandler(project)

    @TaskAction
    fun halt() {
        handler.halt()
    }

    companion object {

        const val NAME = "osgiHalt"

    }

}
