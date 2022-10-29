plugins {
   id("us.ihmc.ihmc-build")
   id("us.ihmc.log-tools-plugin") version "0.6.3"
   id("us.ihmc.ihmc-ci") version "7.6"
   id("us.ihmc.ihmc-cd") version "1.23"
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

categories.configure("allocation") {
   junit5ParallelEnabled = true
   jvmArguments += "allocationAgent"
}

mainDependencies {
   api("org.apache.commons:commons-lang3:3.12.0")
   api("commons-io:commons-io:2.11.0")
   api("us.ihmc:log-tools:0.6.3")
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
   api(ihmc.sourceSetProject("testing"))
}
