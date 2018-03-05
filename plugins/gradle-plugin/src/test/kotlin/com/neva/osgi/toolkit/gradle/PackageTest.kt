package com.neva.osgi.toolkit.gradle

import com.neva.osgi.toolkit.commons.domain.Package
import org.junit.Assert
import org.junit.Test
import org.zeroturnaround.zip.ZipUtil
import java.io.File

class PackageTest : BuildTest() {

    @Test
    fun shouldCreateValidPackage() {
        build("package", ":osgiPackage", { result ->
            val pkg = result.file("build/osgi/packages/example-1.0.0.jar")

            assertPackage(pkg)
            assertPackageFile(pkg, "${Package.ARTIFACT_PATH}/example-1.0.0.jar")
            //assertPackageFile(pkg, "bundle/dependencies/xxx.jar")
        })
    }

    fun assertPackage(pkg: File): File {
        assertFile(pkg)
        assertPackageFiles(pkg)

        return pkg
    }

    fun assertPackageFiles(file: File) {
        PACKAGE_FILES.onEach { assertPackageFile(file, it) }
    }

    fun assertPackageFile(file: File, entry: String) {
        assertPackageFile("Required file '$entry' is not included in OSGi bundle '$file'", file, entry)
    }

    fun assertPackageFile(message: String, file: File, entry: String) {
        Assert.assertTrue(message, ZipUtil.containsEntry(file, entry))
    }

    companion object {
        val PACKAGE_FILES = listOf(
                Package.METADATA_FILE
        )
    }

}
