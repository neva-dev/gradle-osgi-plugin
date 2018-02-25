package com.neva.osgi.toolkit.gradle.base

import org.gradle.api.Plugin
import org.gradle.api.Project

class BasePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.run { configure() }
    }

    private fun Project.configure() {
        logger.info("Applying base plugin")
    }

}
