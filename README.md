IHMC Commons
============

Useful tools and utilities that extend Java, Apache Commons Lang, and Apache Commons IO to make them a little more accessible and non-intrusive.

### Getting Started

Artifacts are hosted on Bintray at the following URLs.

https://bintray.com/ihmcrobotics/maven-release/ihmc-commons

https://bintray.com/ihmcrobotics/maven-release/ihmc-commons-testing

https://bintray.com/ihmcrobotics/maven-release/ihmc-commons-test

In your `build.gradle`:
```groovy
compile group: "us.ihmc", name: "ihmc-commons", version: "0.21.0"
testCompile group: "us.ihmc", name: "ihmc-commons-testing", version: "0.21.0"
```

### What's Included

###### Main Distribution

- File and Path tools that interface using Java's NIO.2 API.
- Minimal Stopwatch with friendly API.
- Commonly needed conversions. (Data information units, etc.)
- Tool for printing log-level style messages that show class name and line number.
- Epsilons for explicitness and convenience.
- Functional exception handler with defaults.
- Recycling and preallocated lists, deque.

###### Testing Distribution

- Support for mutation testing and displaying the results in your browser.
- Tools for running parallel continuous integration tests in the cloud.
- Extra assertions. (assertSerializable, assertExceptionThrown, etc.)
- Tools for allocation testing. (light wrapper for Google's java-allocation-instrumenter)

### Contributing

This library aims to be small, lightweight, and stable with minimal dependencies. Pull requests will be heavily reviewed.

### License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
