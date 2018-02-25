package com.neva.gradle.osgi.internal

import org.gradle.api.artifacts.Dependency

fun Dependency.displayName(): String {
    return "[group=$group,name=$name,version=$version]"
}