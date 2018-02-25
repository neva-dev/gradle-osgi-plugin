package com.neva.gradle.osgi.pkg

import com.neva.gradle.osgi.BuildTest
import org.junit.Test

class PackageTaskTest : BuildTest() {

    @Test
    fun shouldCreateValidBundle() {
        buildScript("pkg/minimal", { runner, projectDir ->
            val build = runner.withArguments("-i", "-S").build()
            assertTaskOutcome(build, ":osgiPackage")

            val pkg = assertPackage(projectDir, "build/libs/example-1.0.0-SNAPSHOT.jar")
            //assertBundleFile(pkg, "bundle/dependencies/xxx.jar")
        })
    }

}
