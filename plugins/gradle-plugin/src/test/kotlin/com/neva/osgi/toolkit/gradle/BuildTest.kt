package com.neva.osgi.toolkit.gradle

import com.neva.osgi.toolkit.gradle.internal.ResourceOperations
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GFileUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File

abstract class BuildTest {

    class Result(val build: BuildResult, val projectDir: File) {

        fun file(path: String): File {
            return File(projectDir, path)
        }

    }

    @Rule
    @JvmField
    var tmpDir = TemporaryFolder()

    fun build(scriptDir: String, taskName: String) {
        build(scriptDir, listOf(taskName), {})
    }

    fun build(scriptDir: String, taskName: String, checker: (Result) -> Unit) {
        build(scriptDir, listOf(taskName), { result ->
            assertTaskOutcome(result.build, taskName)
            checker(result)
        })
    }

    fun build(scriptDir: String, args: List<String>, checker: (Result) -> Unit) {
        val projectDir = File(tmpDir.newFolder(), scriptDir)

        GFileUtils.mkdirs(projectDir)
        ResourceOperations.copyDir("OSGI-INF/toolkit/$scriptDir", projectDir)

        val result = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(projectDir)
                .withArguments(listOf("-i", "-S") + args)
                .forwardOutput()
                .build()

        checker(Result(result, projectDir))
    }

    fun assertTaskOutcomes(build: BuildResult, taskName: String, outcome: TaskOutcome = TaskOutcome.SUCCESS) {
        build.tasks.filter { it.path.endsWith(taskName) }.forEach { assertTaskOutcome(build, it.path, outcome) }
    }

    fun assertTaskOutcome(build: BuildResult, taskName: String, outcome: TaskOutcome = TaskOutcome.SUCCESS) {
        assertEquals(outcome, build.task(taskName)?.outcome)
    }

    fun assertFile(file: File) {
        assertTrue("File does not exist: $file", file.exists())
    }


}
