plugins {
   id("us.ihmc.ihmc-build")
   id("us.ihmc.log-tools-plugin") version "0.6.3"
   id("us.ihmc.ihmc-ci") version "8.3"
   id("us.ihmc.ihmc-cd") version "1.26"
}

ihmc {
   group = "us.ihmc"
   version = "0.32.0"
   vcsUrl = "https://github.com/ihmcrobotics/ihmc-commons"
   openSource = true
   maintainer = "Duncan Calvert"

   configureDependencyResolution()
   configurePublications()
}

categories.configure("fast") {
   junit5ParallelEnabled = true
   jvmArguments += "-Dlog4j2.configurationFile=log4j2NoColor.yml"
}

categories.configure("allocation") {
   junit5ParallelEnabled = true
   jvmArguments += "allocationAgent"
}

mainDependencies {
   api("org.apache.commons:commons-lang3:3.12.0")
   api("commons-io:commons-io:2.11.0")
   api("us.ihmc:log-tools:0.6.3")
}

roboticsDependencies {
   api(ihmc.sourceSetProject("main"))
   api("us.ihmc:euclid-geometry:0.22.0")
   api("us.ihmc:mecano:17-0.18.1")
   api("net.sf.trove4j:trove4j:3.0.3")
}

testingDependencies {
   api(ihmc.sourceSetProject("main"))
   api(junit.jupiterApi())
   api("org.pitest:pitest-command-line:1.7.3")
   api("org.pitest:pitest-junit5-plugin:0.15")
   api(allocation.instrumenter()) // should be runtimeOnly?
}

testDependencies {
   api(ihmc.sourceSetProject("main"))
   api(ihmc.sourceSetProject("robotics"))
   api(ihmc.sourceSetProject("testing"))
   api("com.google.guava:guava:18.0")
}

tasks.register("printJUnitXMLs")
{
   doLast {
      val buildDir = ihmc.sourceSetProject("test").layout.buildDirectory

      buildDir.asFile.get().walk()
         .filter { it.isFile && it.path.matches(Regex(".*TEST-.*.xml"))}
         .forEach { file -> logger.quiet(file.readText()) }
   }
}
