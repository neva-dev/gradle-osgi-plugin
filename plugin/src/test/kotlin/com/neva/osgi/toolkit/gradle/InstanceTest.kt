package com.neva.osgi.toolkit.gradle

import org.junit.Test

class InstanceTest : BuildTest() {

    @Test
    fun shouldCreateValidDistribution() {
        build("instance", ":osgiDistribution", { result ->
            val distro = result.file("build/libs/example-1.0.0.distribution.jar")
            assertFile(distro)
        })
    }

}
