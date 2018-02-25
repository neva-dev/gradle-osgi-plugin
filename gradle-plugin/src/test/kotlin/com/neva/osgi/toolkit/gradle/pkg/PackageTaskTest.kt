package com.neva.osgi.toolkit.gradle.pkg

import com.neva.osgi.toolkit.gradle.BuildTest
import org.junit.Test

// TODO rename to more generic
class PackageTaskTest : BuildTest() {

    @Test
    fun shouldCreateValidPackage() {
        buildScript("pkg/minimal", { runner, projectDir ->
            val build = runner.withArguments("-i", "-S").build()
            assertTaskOutcome(build, ":osgiPackage")

            val pkg = assertPackage(projectDir, "build/libs/example-1.0.0-SNAPSHOT.jar")
            //assertBundleFile(pkg, "bundle/dependencies/xxx.jar")
        })
    }

    @Test
    fun shouldCreateValidDistribution() {
        buildScript("pkg/minimal", { runner, projectDir ->
            val build = runner.withArguments("-i", "-S").build()
            assertTaskOutcome(build, ":osgiDistribution")

            val pkg = assertPackage(projectDir, "build/libs/example-1.0.0-SNAPSHOT.distribution.jar")
            //assertBundleFile(pkg, "bundle/dependencies/xxx.jar")
        })
    }

}
