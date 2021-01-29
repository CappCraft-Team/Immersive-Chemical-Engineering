pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        maven("https://files.minecraftforge.net/maven")
        resolutionStrategy {
            eachPlugin {
                when (requested.id.id) {
                    "net.minecraftforge.gradle" -> {
                        useModule("net.minecraftforge.gradle:ForgeGradle:${requested.version}")
                    }
                }
            }
        }
    }
}
rootProject.name = "icheme"
