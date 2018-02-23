package com.neva.gradle.osgi.bundle

class BundleException : Exception {

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(message: String) : super(message)

}