package com.neva.osgi.toolkit.gradle.instance

import com.neva.osgi.toolkit.commons.domain.Instance
import com.neva.osgi.toolkit.gradle.internal.Formats
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.gradle.util.GFileUtils
import org.zeroturnaround.zip.ZipUtil
import java.io.File

class InstanceHandler(val project: Project) {

    val logger = project.logger

    val distributionJar: File
        get() = project.file((project.tasks.getByName(DistributionTask.NAME) as DistributionTask).archivePath)

    val instanceDir: File
        get() = File("${System.getProperty("user.home")}/.osgi/${project.rootProject.name}")

    val startScript: Script
        get() = script("start")

    val stopScript: Script
        get() = script("stop")

    val frameworkLauncherLock: File
        get() = File(instanceDir, "pid.lock")

    fun create() {
        logger.info("Creating OSGi instance from distribution '$distributionJar' at path '$instanceDir'")

        if (instanceDir.exists()) {
            logger.info("Deleting previously created OSGi instance")

            instanceDir.deleteRecursively()
            GFileUtils.mkdirs(instanceDir)
        }

        logger.info("Unpacking distribution files")

        ZipUtil.unpack(distributionJar, instanceDir, { name ->
            val prefix = Instance.DISTRIBUTION_PATH + "/"
            if (name.startsWith(prefix)) {
                name.substringAfter(prefix)
            } else {
                null
            }
        })
    }

    fun up() {
        if (frameworkLauncherLock.exists()) {
            logger.info("OSGi instance is already running.")
            return
        }

        logger.info("Turning on OSGi instance.")
        executeScript(startScript)
    }

    fun halt() {
        if (!frameworkLauncherLock.exists()) {
            logger.info("OSGi instance is not running.")
            return
        }

        logger.info("Turning off OSGi instance.")
        executeScript(stopScript)
    }

    fun script(name: String, os: OperatingSystem = OperatingSystem.current()): Script {
        return if (os.isWindows) {
            Script(File(instanceDir, "$name.bat"), listOf("cmd", "/C"))
        } else {
            Script(File(instanceDir, name), listOf("sh"))
        }
    }

    fun executeScript(script: Script) {
        ProcessBuilder(*script.commandLine.toTypedArray())
                .directory(instanceDir)
                .start()
    }

    fun lock(name: String) = Lock(instanceDir, "$name.lock")

    class Lock(parent: File, child: String) : File(parent, child) {
        fun lock() {
            printWriter().use {
                it.print(Formats.toJson(mapOf(
                        "locked" to System.currentTimeMillis()))
                )
            }
        }
    }

    class Script(val bin: File, val command: List<String>) {
        val commandLine: List<String>
            get() = command + listOf(bin.absolutePath)

        override fun toString(): String {
            return "Script(commandLine=$commandLine)"
        }
    }

}
