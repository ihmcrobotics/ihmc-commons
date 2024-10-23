IHMC Commons
============

Useful tools and utilities that extend Java, Apache Commons Lang, and Apache Commons IO to make them a little more accessible and non-intrusive.

[![Automated Tests](https://github.com/ihmcrobotics/ihmc-commons/actions/workflows/gradle-test.yml/badge.svg?branch=develop)](https://github.com/ihmcrobotics/ihmc-commons/actions/workflows/gradle-test.yml)

### Download

In your build.gradle:

`compile group: "us.ihmc", name: "ihmc-commons", version: `
[ ![ihmc-commons](https://maven-badges.herokuapp.com/maven-central/us.ihmc/ihmc-commons/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/us.ihmc/ihmc-commons)

`compile group: "us.ihmc", name: "ihmc-commons-robotics", version: `
[ ![ihmc-commons](https://maven-badges.herokuapp.com/maven-central/us.ihmc/ihmc-commons/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/us.ihmc/ihmc-commons-robotics)

`testCompile group: "us.ihmc", name: "ihmc-commons-testing", version: `
[ ![ihmc-commons-testing](https://maven-badges.herokuapp.com/maven-central/us.ihmc/ihmc-commons-testing/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/us.ihmc/ihmc-commons-testing)

### What's Included

##### Main Dependencies

- Apache Commons Lang 3
- Apache Commons I/O
- IHMC's Log Tools logging library providing setting log level from CLI

##### Robotics Dependencies

- Apache Commons Lang 3
- Apache Commons I/O
- IHMC's Log Tools logging library providing setting log level from CLI
- IHMC's Euclid Geometry Library
- Trove4j for garbage free lists of primitives
- Google Guava for String printing

##### Main Distribution

- File and Path tools that interface using Java's NIO.2 API.
- Minimal Stopwatch with friendly API.
- Commonly needed conversions. (Data information units, etc.)
- Epsilons for explicitness and convenience.
- Functional exception handling including one-liner option.
- Recycling and preallocated lists, deque.
- Ring buffers and array sorters
- Tools to increase threading safety.
- Tools for defining time intervals.
- Tools for working with Angles and arrays
- Tools for working with deadbands.

##### Robotics Distribution

- RobotSide, RobotQuadrant, and RobotSextant, which extend RobotSegment definitions for conveniently defining sides for robots.
- SegmentDependentLists, which provide conveniences for containing sided values.
- Common enums for defining robot structure, such as ArmJointName and LegJointName.
- Recycling lists for DenseMatrix from EJML and FrameTuple2D and 3D objects.
- Generic holders of data, such as contactable bodies and center of mass state.
- Holders of output data that is useful to pass between controller instances.
- Tools for deadband

##### Testing Distribution

- Support for mutation testing and displaying the results in your browser.
- Tools for running parallel continuous integration tests in the cloud.
- Extra assertions. (assertSerializable, assertExceptionThrown, etc.)
- Tools for allocation testing. (Filtering on top of google/java-allocation-instrumenter)

### Contributing

This build requires Gradle 5.0+.

This library aims to be small, lightweight, and stable with minimal dependencies. Pull requests will be heavily reviewed.

### License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
