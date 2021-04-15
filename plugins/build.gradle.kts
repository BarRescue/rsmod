val rootPluginDir = projectDir
val rootPluginBuildDir = buildDir

val appDir = project(":all").projectDir
val pluginConfigDir = appDir.resolve("plugins").resolve("resources")

subprojects {
    val relative = projectDir.relativeTo(rootPluginDir)
    buildDir = rootPluginBuildDir.resolve(relative)
    group = "org.rsmod.plugins"

    sourceSets {
        main {
            output.resourcesDir = pluginConfigDir.resolve(relative)
        }
    }
}

allprojects {
    dependencies {
        implementation(kotlin("stdlib"))
        implementation(project(":game"))
        implementation("org.jetbrains.kotlin:kotlin-script-runtime:${Versions.KOTLIN}")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KOTLIN}")
        implementation("org.rsmod:pathfinder:${Versions.RS_MOD_PF}")
    }
}
