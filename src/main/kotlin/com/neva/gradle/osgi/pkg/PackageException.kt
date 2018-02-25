package com.neva.gradle.osgi.pkg

class PackageException : Exception {

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(message: String) : super(message)

}