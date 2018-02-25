package com.neva.osgi.toolkit.gradle.instance

import org.gradle.api.tasks.Input
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

    @Input
    var manager: String = project.project(":manager-bundle").run { "$group:$name:$version" }

    @Input
    var launcher: String = project.project(":framework-launcher").run { "$group:$name:$version" }

    // TODO group: 'org.apache.felix', name: 'org.apache.felix.main.distribution', version: '5.6.10', ext: 'zip
    @Input
    var distribution: String = ""

    @TaskAction
    override fun copy() {
        logger.info("Creating OSGi instance")
    }

}
