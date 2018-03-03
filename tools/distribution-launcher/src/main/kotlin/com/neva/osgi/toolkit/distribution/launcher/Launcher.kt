package com.neva.osgi.toolkit.distribution.launcher

import com.neva.osgi.toolkit.commons.domain.Instance
import com.neva.osgi.toolkit.commons.utils.FileOperations
import java.io.File

object Launcher {

    @JvmStatic
    fun main(args: Array<String>) {
        val cwd = File(".")
        val selfJar = File(javaClass.protectionDomain.codeSource.location.toURI())
        val selfDir = File(cwd, selfJar.nameWithoutExtension)

        FileOperations.unzip(selfJar, selfDir, Instance.DISTRIBUTION_PATH)
    }

}
