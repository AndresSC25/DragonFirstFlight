import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktlint)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

sourceSets {
    main {
        resources.srcDir(
            project(":shared")
                .layout
                .projectDirectory
                .dir("src/commonMain/resources"),
        )
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.korge)
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.compose.uiToolingPreview)

    // JavaFX decodes MP4 frames; KorGE renders them using its own OpenGL texture.
    val platform =
        when {
            System.getProperty("os.name").startsWith("Windows") -> "win"
            System.getProperty("os.name").startsWith("Mac") ->
                if (System.getProperty("os.arch") == "aarch64") "mac-aarch64" else "mac"
            else ->
                if (System.getProperty("os.arch") == "aarch64") "linux-aarch64" else "linux"
        }
    val javafxVersion = libs.versions.javafx.get()
    for (module in listOf("base", "graphics", "media", "swing")) {
        implementation("org.openjfx:javafx-$module:$javafxVersion:$platform") {
            isTransitive = false
        }
    }
    testImplementation(libs.kotlin.testJunit)
}

val korGeJvmArgs =
    listOf(
        "--add-opens=java.desktop/sun.java2d.opengl=ALL-UNNAMED",
        "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
        "--add-opens=java.desktop/com.apple.eawt=ALL-UNNAMED",
        "--add-opens=java.desktop/com.apple.eawt.event=ALL-UNNAMED",
        "--add-exports=java.desktop/com.apple.eawt=ALL-UNNAMED",
        "--add-exports=java.desktop/com.apple.eawt.event=ALL-UNNAMED",
    )

compose.desktop {
    application {
        mainClass = "com.redcrow.dragonsfirstflight.MainKt"

        jvmArgs.addAll(korGeJvmArgs)

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb,
            )

            packageName = "com.redcrow.dragonsfirstflight"
            packageVersion = "1.0.0"
        }
    }
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs(korGeJvmArgs)
}

tasks.test {
    // Native media checks need a graphical session; keep ordinary unit tests headless.
    exclude("**/DesktopIntroDecoderTest*", "**/DesktopIntroFailureTest*")
}

tasks.register<Test>("verifyIntroPlayback") {
    group = "verification"
    description = "Verifies real MP4 playback, pause/resume and decoder failures in a graphical session."
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    include("**/DesktopIntroDecoderTest*", "**/DesktopIntroFailureTest*")
    // JavaFX exits after disposal, so each native test class owns a fresh toolkit/JVM.
    forkEvery = 1
    maxParallelForks = 1
    shouldRunAfter(tasks.test)
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(false)

    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
    }
}
