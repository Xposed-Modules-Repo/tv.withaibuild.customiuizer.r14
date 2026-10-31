package tv.withaibuild.customiuizer.testsupport

import java.io.File

/** Reads repository inputs from either the root or Gradle's app working directory. */
internal fun repositorySource(relativePath: String): String {
    var directory = File(System.getProperty("user.dir").orEmpty()).absoluteFile
    while (true) {
        val candidate = File(directory, relativePath)
        if (candidate.isFile) return candidate.readText()
        directory = directory.parentFile
            ?: error("Repository source not found: $relativePath")
    }
}
