plugins {
   id("us.ihmc.ihmc-build") version "0.15.7"
   id("us.ihmc.log-tools") version "0.3.1"
   id("us.ihmc.ihmc-ci") version "4.25"
}

ihmc {
   group = "us.ihmc"
   version = "0.26.3"
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
