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
    //Korgelin
    maven("https://dl.bintray.com/toliner/Korgelin")
    //TOP
    maven("http://maven.tterrag.com/")
    //CraftTweaker
    //Immersive Engineering
    maven("http://maven.blamejared.com/")
    //Redstone Flux
    maven("http://maven.covers1624.net/")
    //JEI
    maven("http://dvs1.progwml6.com/files/maven")
    //HWYLA
    maven("http://tehnut.info/maven")
    
}

sourceSets.main.get().resources { srcDir("src/generated/resources") }

dependencies {
    minecraft("net.minecraftforge:forge:1.12.2-14.23.5.2854")
    implementation("net.toliner.korgelin:korgelin-1.12:1.1.0-1.3.31")
    implementation("mezz.jei:jei_1.12.2:4.+")
    implementation("cofh:RedstoneFlux:1.12-2.+:deobf")
    implementation("mcjty.theoneprobe:TheOneProbe-1.12:1.12-1.+")
    implementation("CraftTweaker2:CraftTweaker2-MC1120-Main:1.12-4.+")
    implementation("blusunrize:ImmersiveEngineering:0.12-+")
}

minecraft {
    mappings("snapshot", "20171003-1.12")
    buildDir.resolve("generatedATs").walk().filter {
        it.isFile && it.name == "accesstransformer.cfg"
    }.let {
        accessTransformers.addAll(it)
    }
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
    task("transformAccessTransformers") {
        group = "access_transformer"
        doLast {
            buildDir.resolve("generatedATs").deleteRecursively()
            project.configurations.runtimeClasspath.get().filter {
                !it.name.startsWith("forge")
            }.forEach {
                zipTree(it).filter { content ->
                    content.name.endsWith("_at.cfg")
                }.forEach { at ->
                    println("Found AccessTransFormer: ${at.name} in ${it.name}")
                    at.copyTo(buildDir.resolve("generatedATs/${at.nameWithoutExtension}/accesstransformer.cfg"))
                }
            }
        }
    }
    
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