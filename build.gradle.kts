plugins {
   id("us.ihmc.ihmc-build") version "0.15.1"
   id("us.ihmc.log-tools") version "0.2.2"
   id("us.ihmc.ihmc-ci") version "1.0.2"
}

ihmc {
   group = "us.ihmc"
   version = "0.24.0"
   vcsUrl = "https://github.com/ihmcrobotics/ihmc-commons"
   openSource = true
   maintainer = "Duncan Calvert"

   configureDependencyResolution()
   configurePublications()
}

dependencies {
   compile("org.apache.commons:commons-lang3:3.8.1")
   compile("commons-io:commons-io:2.6")
   compile("us.ihmc:log-tools:0.2.2")
}

ihmc.sourceSetProject("testing").dependencies {
   compile(ihmc.sourceSetProject("main"))
   compile("junit:junit:4.12")
   compile("org.pitest:pitest-command-line:1.4.3")
   compile("com.google.code.java-allocation-instrumenter:java-allocation-instrumenter:3.1.0") // should be runtimeOnly?
}

ihmc.sourceSetProject("test").dependencies {
   compile(ihmc.sourceSetProject("main"))
   compile(ihmc.sourceSetProject("testing"))
}
