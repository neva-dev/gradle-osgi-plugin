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

        val distributionTask = tasks.create(DistributionTask.NAME, DistributionTask::class.java)
        val createTask = tasks.create(CreateTask.NAME, CreateTask::class.java)
        val upTask = tasks.create(UpTask.NAME, UpTask::class.java)
        val haltTask = tasks.create(HaltTask.NAME, HaltTask::class.java)

        createTask.dependsOn(distributionTask)
    }

    companion object {

        const val TMP_PATH = "build/tmp/osgi/instance"

    }

}
