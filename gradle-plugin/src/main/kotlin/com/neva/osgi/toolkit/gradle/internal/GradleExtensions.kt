package com.neva.osgi.toolkit.gradle.internal

import org.gradle.api.artifacts.Dependency

fun Dependency.displayName(): String {
    return "[group=$group,name=$name,version=$version]"
}
