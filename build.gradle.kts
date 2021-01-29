import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.Instant
import java.time.format.DateTimeFormatter

plugins {
    kotlin("jvm") version "1.3.31"
    id("net.minecraftforge.gradle") version "4.0.13"
    id("com.matthewprenger.cursegradle") version "1.4.0"
}

group = "team.cappcraft"
version = "1.0-SNAPSHOT"
val mcVersion = "1.12.2"
base.archivesBaseName = "icheme"

repositories {
    jcenter()
    mavenCentral()
    maven("https://files.minecraftforge.net/maven")
    maven("https://dl.bintray.com/toliner/Korgelin")
}

sourceSets.main.get().resources { srcDir("src/generated/resources") }

dependencies {
    minecraft("net.minecraftforge:forge:1.12.2-14.23.5.2854")
    implementation("net.toliner.korgelin:korgelin-1.12:1.1.0-1.3.31")
}

minecraft {
    mappings("snapshot", "20171003-1.12")
    runs {
        create("client") {
            workingDirectory = project.file("run").toString()
            
            // Recommended logging data for a userdev environment
            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            
            // Recommended logging level for the console
            property("forge.logging.console.level", "debug")
            
            mods {
                create(base.archivesBaseName) {
                    sources(sourceSets.main.get())
                }
            }
        }
        
        create("server") {
            workingDirectory = project.file("run").toString()
            
            // Recommended logging data for a userdev environment
            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            
            // Recommended logging level for the console
            property("forge.logging.console.level", "debug")
            
            mods {
                create(base.archivesBaseName) {
                    source(sourceSets.main.get())
                }
            }
        }
    }
}

tasks {
    processResources {
        inputs.properties(
            "version" to version,
            "mcversion" to mcVersion
        )
        filesMatching("mcmod.info") {
            expand("version" to version, "mcversion" to mcVersion)
        }
    }
    
    withType<Jar> {
        manifest {
            attributes(
                "Specification-Title" to "Immersive Chemical Engineering",
                "Specification-Vendor" to "CappCraft Team",
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to "$archiveVersion",
                "Implementation-Vendor" to "CappCraft Team",
                "Implementation-Timestamp" to DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            )
        }
    }
    
    withType<KotlinCompile> {
        doFirst {
            copy {
                from(this@withType.source)
                with(buildDir.resolve("generatedKotlin")) {
                    into(this)
                    this@withType.source = fileTree(this)
                }
                
                filter<org.apache.tools.ant.filters.ReplaceTokens>(
                    "tokens" to mapOf("version" to version)
                )
            }
        }
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    
    withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }
}