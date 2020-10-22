plugins {
   id("us.ihmc.ihmc-build") version "0.22.0"
   id("us.ihmc.log-tools-plugin") version "0.5.0"
   id("us.ihmc.ihmc-ci") version "6.8"
   id("us.ihmc.ihmc-cd") version "1.14"
}

ihmc {
   group = "us.ihmc"
   version = "0.30.3"
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
   api("org.apache.commons:commons-lang3:3.9")
   api("commons-io:commons-io:2.6")
   api("us.ihmc:log-tools:0.5.0")
}

testingDependencies {
   api(ihmc.sourceSetProject("main"))
   api(junit.jupiterApi())
   api("org.pitest:pitest-command-line:1.4.10")
   api("org.pitest:pitest-junit5-plugin:0.9")
   api(allocation.instrumenter()) // should be runtimeOnly?
}

testDependencies {
   api(ihmc.sourceSetProject("main"))
   api(ihmc.sourceSetProject("testing"))
}
