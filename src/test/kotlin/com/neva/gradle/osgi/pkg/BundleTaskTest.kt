package com.neva.gradle.osgi.pkg

import com.neva.gradle.osgi.BuildTest
import org.junit.Test

class BundleTaskTest : BuildTest() {

    @Test
    fun shouldCreateValidBundle() {
        buildScript("package/minimal", { runner, projectDir ->
            val build = runner.withArguments("-i", "-S").build()
            assertTaskOutcome(build, ":osgiPackage")

            val pkg = assertBundle(projectDir, "build/distributions/example-1.0.0-SNAPSHOT.jar")
            //assertBundleFile(pkg, "bundle/dependencies/xxx.jar")
        })
    }

}
