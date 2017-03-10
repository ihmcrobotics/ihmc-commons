IHMC Commons
============

Useful tools and utilities that extend Java, Apache Commons Lang, and Apache Commons IO to make them a little more accessible and non-intrusive.

### Getting Started

Artifacts are hosted on Bintray at https://bintray.com/ihmcrobotics/maven-release/ihmc-commons

In your `build.gradle`:
```groovy
compile group: 'us.ihmc', name: 'ihmc-commons', version: '0.9.1-alpha'
testCompile group: 'us.ihmc', name: 'ihmc-commons-testing', version: '0.9.1-alpha'
```

### What's Included

###### Main Distribution

- File and Path tools that interface using Java's NIO.2 API.
- Common paths to support IHMC conventions.
- Commonly needed conversions. (Data information units, etc.)
- Tool for printing log-level style messages that show class name and line number.

###### Testing Distribution

- Support for mutation testing and displaying the results in your browser.
- Tools for running parallel continuous integration tests in the cloud.
- Extra assertions. (assertSerializable, assertExceptionThrown, etc.)

### Contributing

This library aims to be small, lightweight, and stable with minimal dependencies. Pull requests will be heavily reviewed.

### License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
