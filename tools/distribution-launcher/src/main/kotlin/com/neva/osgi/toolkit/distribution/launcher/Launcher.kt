package com.neva.osgi.toolkit.distribution.launcher

import java.io.File

object Launcher {

    @JvmStatic
    fun main(args: Array<String>) {
        System.out.println("Distribution Launcher - Hello World!")

        val cwd = File(".")
        val distroDir = File(cwd, "distribution")

        FileOperations.copyResources("OSGI-INF/distribution", distroDir)
    }

}
