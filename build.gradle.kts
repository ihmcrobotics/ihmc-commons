import info.solidsoft.gradle.pitest.PitestPlugin
import info.solidsoft.gradle.pitest.PitestPluginExtension

buildscript {
   repositories {
      mavenCentral()
   }
   configurations.maybeCreate("pitest")
   dependencies {
//      classpath("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.4.0")
      add("pitest", "org.pitest:pitest-junit5-plugin:0.8")
   }
}

plugins {
   id("us.ihmc.ihmc-build") version "0.15.5"
   id("us.ihmc.log-tools") version "0.3.1"
   id("us.ihmc.ihmc-ci") version "4.9"
   id("info.solidsoft.pitest") version "1.4.0"
}

ihmc.sourceSetProject("test").pluginManager.apply("info.solidsoft.pitest")

ihmc.sourceSetProject("test").tasks.register("mainClasses") {
   dependsOn(setOf(ihmc.sourceSetProject("main").tasks.getByName("classes")))
}

ihmc.sourceSetProject("test").pitest {
   pitestVersion = "1.4.5"
   testPlugin = "junit5"

   mainSourceSets = setOf(ihmc.sourceSet("main"))
   testSourceSets = setOf(ihmc.sourceSet("test"))
   additionalMutableCodePaths = setOf(
         ihmc.sourceSet("main").output.classesDirs.singleFile,
         ihmc.sourceSet("testing").output.classesDirs.singleFile)

   targetClasses = setOf("us.ihmc.commons.time.Stopwatch")
   targetTests = setOf("us.ihmc.commons.time.StopwatchTest")

   verbose = true
}

//extensions.withGroovyBuilder {  }

//ihmc.sourceSetProject("test").apply("../../pitest.groovy")

ihmc {
   group = "us.ihmc"
   version = "0.25.1"
   vcsUrl = "https://github.com/ihmcrobotics/ihmc-commons"
   openSource = true
   maintainer = "Duncan Calvert"

   configureDependencyResolution()
   configurePublications()
}

categories.configure("allocation") {
   junit5ParallelEnabled = true
}

dependencies {
   compile("org.apache.commons:commons-lang3:3.8.1")
   compile("commons-io:commons-io:2.6")
   compile("us.ihmc:log-tools:0.3.1")
}

ihmc.sourceSetProject("testing").dependencies {
   compile(ihmc.sourceSetProject("main"))
   compile("org.junit.jupiter:junit-jupiter-api:5.4.0")
   compile("org.pitest:pitest-command-line:1.4.5")
   compile("org.pitest:pitest-junit5-plugin:0.8")
   compile("com.google.code.java-allocation-instrumenter:java-allocation-instrumenter:3.2.0") // should be runtimeOnly?
}

ihmc.sourceSetProject("test").dependencies {
   compile(ihmc.sourceSetProject("main"))
   compile(ihmc.sourceSetProject("testing"))
}
