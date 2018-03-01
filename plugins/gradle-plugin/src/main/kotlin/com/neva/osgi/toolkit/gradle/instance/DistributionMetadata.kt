package com.neva.osgi.toolkit.gradle.instance

import org.gradle.api.Project
import java.io.Serializable

class DistributionMetadata : Serializable {

    companion object {
        fun of(project: Project): DistributionMetadata {
            return DistributionMetadata().apply {
                // TODO ..
            }
        }
    }

}

