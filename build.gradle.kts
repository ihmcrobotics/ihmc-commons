plugins {
   id("us.ihmc.ihmc-build") version "0.20.1"
   id("us.ihmc.log-tools") version "0.3.1"
   id("us.ihmc.ihmc-ci") version "5.3"
   id("us.ihmc.ihmc-cd") version "1.8"
}

ihmc {
   group = "us.ihmc"
   version = "0.28.1"
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
   api("us.ihmc:log-tools:0.3.1")
}

testingDependencies {
   api(ihmc.sourceSetProject("main"))
   api("org.junit.jupiter:junit-jupiter-api:${junitVersion.jupiter}")
   api("org.pitest:pitest-command-line:1.4.10")
   api("org.pitest:pitest-junit5-plugin:0.9")
   api("com.google.code.java-allocation-instrumenter:java-allocation-instrumenter:3.2.0") // should be runtimeOnly?
}

testDependencies {
   api(ihmc.sourceSetProject("main"))
   api(ihmc.sourceSetProject("testing"))
}
