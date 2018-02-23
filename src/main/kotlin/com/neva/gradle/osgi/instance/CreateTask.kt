package com.neva.gradle.osgi.instance

import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar

open class CreateTask : Jar() {

    companion object {
        const val NAME = "osgiCreate"
    }

    init {
        group = "OSGi"
        description = "Creates OSGi instance"

    }

    @TaskAction
    override fun copy() {
        logger.info("Creating OSGi instance")
    }

}