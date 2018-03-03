package com.neva.osgi.toolkit.commons.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object FileOperations {

    fun unzip(file: File, dir: File, path: String) {
        unzip(file, dir, { name ->
            val prefix = "$path/"
            if (!name.endsWith("/") && name.startsWith(prefix)) {
                name.substringAfter(prefix)
            } else {
                null
            }
        })
    }

    fun unzip(file: File, dir: File, nameMapper: (String) -> String?) {
        val buffer = ByteArray(1024)
        val zis = ZipInputStream(FileInputStream(file))
        var zipEntry: ZipEntry? = zis.nextEntry
        while (zipEntry != null) {
            val fileName = nameMapper(zipEntry.name)
            if (fileName != null) {
                val newFile = File(dir, fileName).apply { parentFile.mkdirs() }
                val fos = FileOutputStream(newFile)

                var len: Int
                while (true) {
                    len = zis.read(buffer)
                    if (len > 0) {
                        fos.write(buffer, 0, len)
                    } else {
                        break
                    }
                }

                fos.close()
            }

            zipEntry = zis.nextEntry
        }
        zis.closeEntry()
        zis.close()
    }

}
