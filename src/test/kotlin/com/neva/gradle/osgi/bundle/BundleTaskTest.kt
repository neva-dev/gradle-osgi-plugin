package com.neva.gradle.osgi.bundle

import com.neva.gradle.osgi.BuildTest
import org.junit.Test

class BundleTaskTest : BuildTest() {

    @Test
    fun shouldCreateValidBundle() {
        buildScript("bundle/minimal", { runner, projectDir ->
            val build = runner.withArguments("osgiBundle", "-i", "-S").build()
            assertTaskOutcome(build, ":osgiBundle")

            val bundle = assertBundle(projectDir, "build/distributions/example-1.0.0-SNAPSHOT.bundle")
            //assertBundleFile(pkg, "bundle/dependencies/xxx.jar")
        })
    }

}