// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.maven.publish) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    dependencies {
        add("ktlintRuleset", "io.nlopez.compose.rules:ktlint:0.4.28")
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.8.0")
        debug.set(true)
        verbose.set(true)
        android.set(true)
        outputToConsole.set(true)
        outputColorName.set("RED")
        ignoreFailures.set(false)
        enableExperimentalRules.set(false)
        filter {
            exclude("**/generated/**")
            include("**/kotlin/**")
        }
    }

    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
            signAllPublications()
        }
    }
}
