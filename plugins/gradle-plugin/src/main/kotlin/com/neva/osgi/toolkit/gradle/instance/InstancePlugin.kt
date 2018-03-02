package com.neva.osgi.toolkit.gradle.instance

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

open class InstancePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.configure()
    }

    private fun Project.configure() {
        logger.info("Applying OSGi instance plugin")

        plugins.apply(BasePlugin::class.java)
        tasks.create(DistributionTask.NAME, DistributionTask::class.java)
    }

    companion object {

        const val TMP_PATH = "build/tmp/osgi/instance"

    }

}
