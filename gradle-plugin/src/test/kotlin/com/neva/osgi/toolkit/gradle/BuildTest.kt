package com.neva.osgi.toolkit.gradle

import com.neva.osgi.toolkit.gradle.internal.FileOperations
import com.neva.osgi.toolkit.gradle.pkg.PackagePlugin
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GFileUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.zeroturnaround.zip.ZipUtil
import java.io.File

abstract class BuildTest {

    @Rule
    @JvmField
    var tmpDir = TemporaryFolder()

    fun buildScript(scriptDir: String, configurer: (runner: GradleRunner, projectDir: File) -> Unit) {
        val projectDir = File(tmpDir.newFolder(), scriptDir)

        GFileUtils.mkdirs(projectDir)
        FileOperations.copyResources(scriptDir, projectDir)

        val runner = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(projectDir)

        configurer(runner, projectDir)
    }

    fun assertTaskOutcomes(build: BuildResult, taskName: String, outcome: TaskOutcome = TaskOutcome.SUCCESS) {
        build.tasks.filter { it.path.endsWith(taskName) }.forEach { assertTaskOutcome(build, it.path, outcome) }
    }

    fun assertTaskOutcome(build: BuildResult, taskName: String, outcome: TaskOutcome = TaskOutcome.SUCCESS) {
        assertEquals(outcome, build.task(taskName)?.outcome)
    }

    fun assertPackage(projectDir: File, path: String): File {
        val pkg = File(projectDir, path)

        assertTrue("Composed OSGi package does not exist: $pkg", pkg.exists())
        assertPackageFiles(pkg)

        return pkg
    }

    fun assertBundleFile(file: File, entry: String) {
        assertPackageFile("Required file '$entry' is not included in OSGi bundle '$file'", file, entry)
    }

    fun assertPackageFile(message: String, file: File, entry: String) {
        assertTrue(message, ZipUtil.containsEntry(file, entry))
    }

    fun assertPackageFiles(file: File) {
        PACKAGE_FILES.onEach { assertBundleFile(file, it) }
    }

    companion object {
        val PACKAGE_FILES = listOf(
                PackagePlugin.METADATA_FILE
        )
    }

}
