plugins {
   id("us.ihmc.ihmc-build") version "0.22.0"
   id("us.ihmc.log-tools-plugin") version "0.5.0"
   id("us.ihmc.ihmc-ci") version "7.0"
   id("us.ihmc.ihmc-cd") version "1.16"
}

ihmc {
   group = "us.ihmc"
   version = "0.30.4"
   vcsUrl = "https://github.com/ihmcrobotics/ihmc-commons"
   openSource = true
   maintainer = "Duncan Calvert"

   configureDependencyResolution()
   configurePublications()
}

categories.configure("allocation") {
   junit5ParallelEnabled = true
}

mainDependencies {
   api("org.apache.commons:commons-lang3:3.11")
   api("commons-io:commons-io:2.8.0")
   api("us.ihmc:log-tools:0.5.0")
}

testingDependencies {
   api(ihmc.sourceSetProject("main"))
   api(junit.jupiterApi())
   api("org.pitest:pitest-command-line:1.5.2")
   api("org.pitest:pitest-junit5-plugin:0.12")
   api(allocation.instrumenter()) // should be runtimeOnly?
}

testDependencies {
   api(ihmc.sourceSetProject("main"))
   api(ihmc.sourceSetProject("testing"))
}
