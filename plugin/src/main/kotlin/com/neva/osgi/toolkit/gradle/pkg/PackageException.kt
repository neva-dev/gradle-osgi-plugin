package com.neva.osgi.toolkit.gradle.pkg

class PackageException : Exception {

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(message: String) : super(message)

}
