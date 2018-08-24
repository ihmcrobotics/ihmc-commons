IHMC Commons
============

Useful tools and utilities that extend Java, Apache Commons Lang, and Apache Commons IO to make them a little more accessible and non-intrusive.

### Download

In your build.gradle:

`compile group: "us.ihmc", name: "ihmc-commons", version: `
[ ![ihmc-commons](https://api.bintray.com/packages/ihmcrobotics/maven-release/ihmc-commons/images/download.svg) ](https://bintray.com/ihmcrobotics/maven-release/ihmc-commons/_latestVersion)

`testCompile group: "us.ihmc", name: "ihmc-commons-testing", version: `
[ ![ihmc-commons](https://api.bintray.com/packages/ihmcrobotics/maven-release/ihmc-commons/images/download.svg) ](https://bintray.com/ihmcrobotics/maven-release/ihmc-commons/_latestVersion)

### What's Included

##### Main Distribution

- File and Path tools that interface using Java's NIO.2 API.
- Minimal Stopwatch with friendly API.
- Commonly needed conversions. (Data information units, etc.)
- Tool for printing log-level style messages that show class name and line number.
- Epsilons for explicitness and convenience.
- Functional exception handler with defaults.
- Recycling and preallocated lists, deque.

##### Testing Distribution

- Support for mutation testing and displaying the results in your browser.
- Tools for running parallel continuous integration tests in the cloud.
- Extra assertions. (assertSerializable, assertExceptionThrown, etc.)
- Tools for allocation testing. (Filtering on top of google/java-allocation-instrumenter)

### Contributing

This library aims to be small, lightweight, and stable with minimal dependencies. Pull requests will be heavily reviewed.

### License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
