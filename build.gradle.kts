plugins {
   id("us.ihmc.ihmc-build") version "0.15.1"
   id("us.ihmc.log-tools") version "0.2.2"
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
   implementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
   runtimeOnly("org.junit.vintage:junit-vintage-engine:5.3.1")
   runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

val allocationTests = (property("allocationTests") as String) == "true"

ihmc.sourceSetProject("test").tasks.withType<Test> {
   useJUnitPlatform {
      if (allocationTests)
      {
         includeTags("allocation")
      }
      else
      {
         excludeTags("allocation")
      }
   }

   setForkEvery(1)
   maxParallelForks = 2
   systemProperties["junit.jupiter.execution.parallel.enabled"] = "true"
   systemProperties["junit.jupiter.execution.parallel.config.strategy"] = "dynamic"
   systemProperties["junit.jupiter.execution.parallel.config.fixed.parallelism"] = "4"

   if (allocationTests)
   {
      ihmc.sourceSetProject("test").configurations.compile.files.forEach {
         if (it.name.contains("java-allocation-instrumenter"))
         {
            val jvmArg = "-javaagent:" + it.getAbsolutePath()
            println("[ihmc-commons] Passing JVM arg: " + jvmArg)
            val tmpArgs = allJvmArgs
            tmpArgs.add(jvmArg)
            allJvmArgs = tmpArgs

         }
      }
   }
}