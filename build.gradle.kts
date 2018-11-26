plugins {
   id("us.ihmc.ihmc-build") version "0.15.4"
   id("us.ihmc.log-tools") version "0.2.2"
   id("us.ihmc.ihmc-ci") version "2.3"
}

ihmc {
   group = "us.ihmc"
   version = "0.25.0"
   vcsUrl = "https://github.com/ihmcrobotics/ihmc-commons"
   openSource = true
   maintainer = "Duncan Calvert"

   configureDependencyResolution()
   configurePublications()
}

categories.create("fast") {
   maxJVMs = 1
}

categories.create("allocation") {
   maxJVMs = 1
   includeTags += "allocation"
   jvmArguments += "allocationAgent"
}

dependencies {
   compile("org.apache.commons:commons-lang3:3.8.1")
   compile("commons-io:commons-io:2.6")
   compile("us.ihmc:log-tools:0.2.2")
}

ihmc.sourceSetProject("testing").dependencies {
   compile(ihmc.sourceSetProject("main"))
   compile("org.junit.jupiter:junit-jupiter-api:5.3.1")
   compile("org.pitest:pitest-command-line:1.4.3")
   compile("com.google.code.java-allocation-instrumenter:java-allocation-instrumenter:3.1.0") // should be runtimeOnly?
}

ihmc.sourceSetProject("test").dependencies {
   compile(ihmc.sourceSetProject("main"))
   compile(ihmc.sourceSetProject("testing"))
}
