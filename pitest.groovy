pitest {
   pitestVersion = "1.4.5"
   testPlugin = "junit5"

   testSourceSets = [sourceSets.test]
   mainSourceSets = [sourceSets.main, sourceSets.additionalMain]

   targetClasses = ['us.ihmc.commons.time.Stopwatch']
   targetTests = ['us.ihmc.commons.time.StopwatchTest']

   verbose = true
}