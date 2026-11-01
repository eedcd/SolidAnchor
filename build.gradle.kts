/*
 * This file is part of SolidAnchor
 *
 * Copyright (c) 2015 - 2025 CCBlueX
 *
 * SolidAnchor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SolidAnchor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SolidAnchor. If not, see <https://www.gnu.org/licenses/>.
 */

import dev.detekt.gradle.DetektCreateBaselineTask
import java.time.Duration
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.workers.ProcessIsolation
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradleGitProperties)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
}

base {
    archivesName = project.property("archives_base_name") as String
    version = project.property("mod_version") as String
    group = project.property("maven_group") as String
}

dokka {
    // Dokka Generator runs in its own worker process, so org.gradle.jvmargs does not apply here.
    (dokkaGeneratorIsolation.get() as ProcessIsolation).maxHeapSize = "4g"
}

/** Includes dependency recursively in the JAR file */
val jij = configurations.create("jij")

jij.excludeProvidedLibs()

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            name = "CCBlueX Releases"
            url = uri("https://maven.ccbluex.net/releases")
        }
        maven {
            name = "CCBlueX Snapshots"
            url = uri("https://maven.ccbluex.net/snapshots")
        }
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "Jitpack"
            url = uri("https://jitpack.io")
        }
        maven {
            name = "ViaVersion"
            url = uri("https://repo.viaversion.com/")
        }
        maven {
            name = "modrinth"
            url = uri("https://api.modrinth.com/maven")
        }
        maven {
            name = "OpenCollab Snapshots"
            url = uri("https://repo.opencollab.dev/maven-snapshots/")
        }
        maven {
            name = "Lenni0451"
            url = uri("https://maven.lenni0451.net/everything")
        }
        maven {
            url = uri("https://maven.shedaniel.me/")
        }
    }
}

loom {
    accessWidenerPath = file("src/main/resources/solidanchor.accesswidener")
}

gitProperties {
    // The distributed source archive ships without a .git directory; GitInfo falls back to its defaults.
    failOnNoGitDirectory = false
}

// Client game tests: `src/gametest` is a separate source set/mod, never part of the main jar.
// Run with `./gradlew runClientGameTest`. Headless, SDL needs EGL, since Xvfb has no sRGB GLX visual:
// `SDL_VIDEO_FORCE_EGL=1 xvfb-run -a -s "-screen 0 1280x720x24" ./gradlew runClientGameTest`
fabricApi {
    configureTests {
        createSourceSet = true
        modId = "solidanchor-gametest"
        // SolidAnchor is client-only; server game tests would otherwise be wired into `check`.
        enableGameTests = false
        eula = true
    }
}

loom.runs.named("clientGameTest") {
    // Keeps the vanilla screens the test API waits for.
    systemProperties.put("net.solidanchor.ui.basicMode", "true")
}

tasks.named("runClientGameTest") {
    // A game that cannot start may wait on an error dialog forever
    timeout = Duration.ofMinutes(10)
}

dependencies {
    // Minecraft
    minecraft(libs.minecraft)

    // Fabric
    api(libs.fabric.loader)
    api(libs.fabric.api)
    api(libs.fabric.kotlin)

    // Mods the client only needs while developing. They must not reach the published POM - no add-on
    // declares the Modrinth or ViaVersion repository - so they are compileOnly + localRuntime rather
    // than api + runtimeOnly.

    // Mod menu
    compileOnly(libs.modmenu)
    localRuntime(libs.modmenu)

    // Recommended mods (on IDE)
    compileOnly(libs.sodium)
    compileOnly(libs.lithium)
    localRuntime(libs.sodium)
    localRuntime(libs.lithium)
    localRuntime(libs.immediatelyFast)
    localRuntime(libs.iris)

    // ViaFabricPlus
    compileOnly(libs.vfp.api)
    localRuntime(libs.vfp)

    // Exploit Preventer
    compileOnly(libs.exploitPreventer.api)
    localRuntime(libs.exploitPreventer)

    // Minecraft account authentication (Microsoft/Xbox Live/XSTS token chain)
    jij(libs.minecraftauth)

    // TheAltening alt service
    jij(libs.thealtening)

    // Mojang REST APIs
    jij(libs.bundles.retrofit)

    // LWJGL EGL
    jij(libs.lwjgl.egl)

    // HTTP library
    jij(libs.bundles.okhttp)

    // Update Checker
    jij(libs.semver4j)

    // Name Protect
    jij(libs.ahocorasick)

    // External utils
    compileOnlyApi(libs.fastutil4k.extensionsOnly)
    jij(libs.fastutil4k.moreCollections)

    // Test libraries
    testImplementation(kotlin("test"))
    testImplementation(libs.fabric.loader.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

addResolvedDependencies(jij, "compileOnly", "include", "api")

tasks.processResources {
    val modVersion = providers.gradleProperty("mod_version")
    val minecraftVersion = providers.gradleProperty("mod_mc_version")
    val fabricVersion = libs.versions.fabric.api
    val loaderVersion = libs.versions.fabric.loader
    val minLoaderVersion = libs.versions.fabric.loaderMin
    val fabricKotlinVersion = libs.versions.fabric.kotlin
    val viafabricplusVersion = libs.versions.viafabricplus
    val contributors = provider { "[]" }

    inputs.property("version", modVersion)
    inputs.property("minecraft_version", minecraftVersion)
    inputs.property("fabric_version", fabricVersion)
    inputs.property("loader_version", loaderVersion)
    inputs.property("min_loader_version", minLoaderVersion)
    inputs.property("fabric_kotlin_version", fabricKotlinVersion)
    inputs.property("viafabricplus_version", viafabricplusVersion)
    inputs.property("contributors", contributors)

    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "version" to modVersion.get(),
                "minecraft_version" to minecraftVersion.get(),
                "fabric_version" to fabricVersion.get(),
                "loader_version" to loaderVersion.get(),
                "min_loader_version" to minLoaderVersion.get(),
                "contributors" to contributors.get(),
                "fabric_kotlin_version" to fabricKotlinVersion.get(),
                "viafabricplus_version" to viafabricplusVersion.get()
            )
        )
    }
}

// ensure that the encoding is set to UTF-8, no matter what the system default is
// this fixes some edge cases with special characters not displaying correctly
// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
tasks.withType<JavaCompile>().configureEach {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"

    options.release = libs.versions.jdk.get().toInt()
}

tasks.test {
    useJUnitPlatform()
    // Prevent macOS AWT from starting a native window session during font tests.
    systemProperty("java.awt.headless", "true")
    systemProperty(
        "fabric.debug.disableModIds",
        arrayOf(
            // ImmediatelyFast's platform service requires a fully initialized Fabric game process.
            "immediatelyfast",
            // ViaFabricPlus mixins call its API, which only exists once the mod entrypoint ran.
            "viafabricplus",
            "viafabricplus-api",
            // Avoid loading Fabric Language Kotlin's nested Kotlin runtime alongside Gradle's test runtime.
            "org_jetbrains_kotlin_kotlin-reflect",
            "org_jetbrains_kotlin_kotlin-stdlib",
            "org_jetbrains_kotlin_kotlin-stdlib-jdk7",
            "org_jetbrains_kotlin_kotlin-stdlib-jdk8",
        ).joinToString(","),
    )
    // Let Knot delegate Kotlin Test and the Kotlin runtime to JUnit's parent class loader.
    jvmArgumentProviders.add(
        objects.newInstance<FabricSystemLibrariesArgumentProvider>().apply {
            runtimeClasspath.from(configurations.testRuntimeClasspath)
        }
    )
}

// Detekt check

detekt {
    config.setFrom(file("${rootProject.projectDir}/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    baseline = file("${rootProject.projectDir}/config/detekt/baseline.xml")
    // Defaults only cover src/{main,test}/{java,kotlin}.
    source.from("src/gametest/kotlin")
}

tasks.register<DetektCreateBaselineTask>("detektProjectBaseline") {
    description = "Overrides current baseline."
    ignoreFailures.set(true)
    parallel.set(true)
    buildUponDefaultConfig.set(true)
    setSource(files(rootDir))
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline.set(file("$rootDir/config/detekt/baseline.xml"))
    include("**/*.kt", "**/*.kts")
    exclude("**/resources/**", "**/build/**")
}

tasks.register<JavaExec>("liquidInstruction") {
    group = "other"
    description = "Run SolidInstruction class."

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("net.solidanchor.SolidInstruction")
}

tasks.runClient {
    jvmArgs("-XX:+UseZGC")
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get().toInt()))
    }
}

kotlin {
    compilerOptions {
        suppressWarnings = true
        jvmToolchain(libs.versions.jdk.get().toInt())
        freeCompilerArgs.add("-Xcollection-literals")
        freeCompilerArgs.add("-Xcompanion-blocks-and-extensions")
    }

    // Add-ons are compiled against these; `./gradlew updateKotlinAbi` records a deliberate change.
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        filters {
            include {
                annotatedWith.add("net.solidanchor.features.addon.AddonApi")
            }
        }
    }
}

tasks.jar {
    val archivesBaseName = providers.gradleProperty("archives_base_name")
    val modVersion = providers.gradleProperty("mod_version")
    val mavenGroup = providers.gradleProperty("maven_group")

    inputs.property("archives_base_name", archivesBaseName)
    inputs.property("mod_version", modVersion)
    inputs.property("maven_group", mavenGroup)

    manifest {
        attributes["Main-Class"] = "net.solidanchor.SolidInstruction"
        attributes["Implementation-Title"] = archivesBaseName.get()
        attributes["Implementation-Version"] = modVersion.get()
        attributes["Implementation-Vendor"] = mavenGroup.get()
    }

    // Rename the project's license file to LICENSE_<project_name> to avoid conflicts
    from("LICENSE") {
        rename {
            "${it}_${archivesBaseName.get()}"
        }
    }
}

tasks.register<Copy>("copyZipInclude") {
    from("zip_include/")
    into("build/libs/zip")
}

tasks.named<Jar>("sourcesJar") {
    dependsOn("generateGitProperties")
}

tasks.named("build") {
    dependsOn("copyZipInclude")
}
