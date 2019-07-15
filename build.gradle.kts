plugins {
   id("us.ihmc.ihmc-build") version "0.16.3"
   id("us.ihmc.log-tools") version "0.3.1"
   id("us.ihmc.ihmc-ci") version "4.25"
   id("us.ihmc.ihmc-cd") version "0.1"
}

ihmc {
   group = "us.ihmc"
   version = "0.26.6"
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
   api("org.apache.commons:commons-lang3:3.8.1")
   api("commons-io:commons-io:2.6")
   api("us.ihmc:log-tools:0.3.1")
}

testingDependencies {
   api(ihmc.sourceSetProject("main"))
   api("org.junit.jupiter:junit-jupiter-api:5.4.0")
   api("org.pitest:pitest-command-line:1.4.5")
   api("org.pitest:pitest-junit5-plugin:0.8")
   api("com.google.code.java-allocation-instrumenter:java-allocation-instrumenter:3.2.0") // should be runtimeOnly?
}

testDependencies {
   api(ihmc.sourceSetProject("main"))
   api(ihmc.sourceSetProject("testing"))
}
