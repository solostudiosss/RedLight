// Top-level build file where you can add configuration options common to all sub-projects/modules.
@file:Suppress("UseTomlInstead")

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.11.1")
        classpath(kotlin("gradle-plugin", version = "1.9.23"))
    }

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

