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

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(false)

    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
    }
}
