package com.neva.osgi.toolkit.distribution.launcher

import com.neva.osgi.toolkit.commons.domain.Instance
import com.neva.osgi.toolkit.commons.utils.FileOperations
import java.io.File

object Launcher {

    @JvmStatic
    fun main(args: Array<String>) {
        System.out.println("Distribution Launcher - Hello World!")

        val cwd = File(".")
        val distroDir = File(cwd, "distribution")

        FileOperations.copyResources(Instance.DISTRIBUTION_PATH, distroDir)
    }

}
