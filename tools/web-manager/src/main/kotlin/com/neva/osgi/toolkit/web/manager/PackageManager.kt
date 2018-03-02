package com.neva.osgi.toolkit.web.manager

import org.osgi.service.component.annotations.Activate
import org.osgi.service.component.annotations.Component
import org.osgi.service.component.annotations.Deactivate

@Component(
        immediate = true,
        service = [PackageManager::class]
)
class PackageManager {

    @Activate
    fun activate() {
        println("OSGi Toolkit - Package Manager started!")
    }

    @Deactivate
    fun deactivate() {
        println("OSGi Toolkit - Package Manager stopped!")
    }

}
