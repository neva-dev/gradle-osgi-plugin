package com.neva.osgi.toolkit.framework.launcher

import org.apache.felix.main.Main
import java.io.File
import java.lang.management.ManagementFactory

object Launcher {

    private val pid = ManagementFactory.getRuntimeMXBean().name.substringBefore("@").toInt()

    private val pidFile = File("pid.lock")

    @JvmStatic
    fun main(args: Array<String>) {
        start()
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                Launcher.stop()
            }
        })
        Main.main(args)
    }

    fun start() {
        createPidFile()
        printLogo()
    }

    fun stop() {
        deletePidFile()
    }

    private fun printLogo() {
        println("----------------------")
        println("| OSGi Toolkit 1.0.0 |")
        println("----------------------")
    }

    private fun createPidFile() {
        pidFile.printWriter().use { it.print(pid) }
    }

    private fun deletePidFile() {
        if (pidFile.exists()) {
            pidFile.delete()
        }
    }

}
