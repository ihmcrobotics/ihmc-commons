# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.30.5] - 2021-12-20

- Upgrade dependencies and remove log4j vulnerabilities.

### Features

- Add `DaemonThreadFactory` class
- Return Thread handle from `ThreadTools#startAThread` and `TheadTools#startAsDaemon`
- Add `ThreadTools#join`
- Add `ThreadTools` executor creator methods
- Upgrade to use Gradle Java Library plugin via the new ihmc-build plugin release

## [0.26.6] - 2019-07-15

### Features

- Add `DaemonThreadFactory` class
- Return Thread handle from `ThreadTools#startAThread` and `TheadTools#startAsDaemon`
- Add `ThreadTools#join`
- Add `ThreadTools` executor creator methods
- Upgrade to use Gradle Java Library plugin via the new ihmc-build plugin release

## [0.26.3] - 2019-05-01
### Features
Adding Time Interval Tools and bumping build plug in version
- Moving the classes for time intervals into ihmc-commons
- includes `TimeIntervalReadOnly`, which is an interface for a read only time interval, `TimeIntervalBasics`, which defines an interface for a modifiable time interval, and `TimeInterval`, which defines the actual time interval class.
- also includes `TimeIntervalTools`, which provides tools for pruning and sorting lists of time intervals.
- includes an interface `TimeIntervalProvider`, which is intended to define a class that provides a time interval.
- ups the version of the build plug in
- improves the ReadMe to indicate the required version of Gradle.

## [0.26.2] - 2019-03-05
- Add `ExceptionTools` for one-liner exception handling.
- Add `Notification` for the simplest case of class-local threading inter operation

## [0.25.4] - 2019-02-19
- Add `facilitateMutationTestForClasses(Class<?>[] applicationClasses, Class<?>[] testClasses)` that allows for mutating multiple classes while running multiple tests.
- Added `--excludedClasses *Test*` to pitest arguments

## [0.25.2] - 2019-02-20
- Fix mutation testing with JUnit 5

## [0.25.1] - 2019-02-14
- Fix MutationTestFacilitator for long file names
- ihmc-ci 4.8
- ihmc-commons 0.15.5
- log-tools 0.3.1
- junit 5.4.0
- pitest 1.4.5
- allocation-instrumenter 3.2.0

## [0.25.0] - 2018-10-23
### Features

This release switches completely from JUnit 4 to JUnit 5.

## [0.24.0] - 2018-10-11
### Deprecated API

- PrintTools is now deprecated in favor of LogTools

### Dependencies

- IHMC's Log Tools logging library has been added to replace PrintTools. See more at https://github.com/ihmcrobotics/log-tools
- Commons Lang and PiTest upgraded to latest versions

## [0.23.2] - 2018-10-11
### New Data Structure

**RecyclingLinkedList** provides a data structure that can be used similar to a deque. Elements can
be added and removed efficiently from the front and the end of the linked list. However, element access is
not possible. The linked list provides a forward and backward ***RecyclingIterator** that can be used to
move through the list.

## [0.23.1] - 2018-10-02
### New Feature

- Adding a convenience method to `SupplierBuilder` for making an index-based `Supplier`

## [0.23.0] - 2018-08-24
### New Features

- BoundedRecyclingArrayList - throws OutOfMemoryException when max capacity violated

## [0.22.0] - 2018-07-02
### API Changes

- Rewrite `AllocationProfiler` API to be more configurable, easy to understand, and 70% mutation test coverage

## [0.21.1] - 2018-06-26
### Bug fixes

- Fix bug where blacklisted methods got added to the whitelist accidentally

## [0.21.0] - 2018-06-26
### API Changes

- Rewrite allocation testing API for user friendliness. See `AllocationRecordingDemo.java`

## [0.20.1] - 2018-06-21
### API Changes

- `PreallocatedList` now implements `java.util.List`

## [0.20.0] - 2018-06-21
### New Features

This release mostly features some new utilities that help when programming for real-time systems.

###### New list classes geared towards memory management for real-time safety
- `PreallocatedList`, `PreallocatedEnumList`
- `RecyclingArrayList`, `RecyclingArrayDeque`

###### Allocation testing tools
- Extend `AllocationTest` to test code allocations


## [0.19.1] - 2018-05-03
# Bug Fixes

- MutationTestFacilitator got lost when run from src/test. It now correctly identifies it's parent project root, 
and places complete results in your-project/pit-reports no matter where you run it from (project root or src/xxx)

## [0.19.0] - 2018-04-26
# New Features

- New ExceptionHandler functional interface
- DefaultExceptionHandler is now default cases of ExceptionHandler

# Breaking Changes

DefaultExceptionHandler no longer returns `null` as an Object. Please return null separately.

## [0.18.0] - 2018-06-21
### Code cleanup

- Format the whole repository

### Dependency cleanup

- Remove `ihmc-ci` dependency

## [0.11.2] - 2017-06-27

## [0.11.0-alpha] - 2017-06-27
### Release Notes

- Add Assertions to assert exceptions thrown, serializable
- Add RunnableThatThrows to support lambdas and asserting exceptions thrown
- Refactor Conversions API
- Complete Conversions documentation
- Remove array signatures
- Fix Issue #1: Add double precision signature to milliseconds method
- Fix Issue #2: Remove radians-degrees conversions (Use Math.toRadians and Math.toDegrees)
- Add Epsilons

## [0.9.1-alpha] - 2017-03-10
### Release Notes

- Add new Stopwatch class with friendly API

[Unreleased]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.30.5...HEAD
[0.30.5]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.26.6...0.30.5
[0.26.6]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.26.3...0.26.6
[0.26.3]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.26.2...0.26.3
[0.26.2]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.25.4...0.26.2
[0.25.4]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.25.2...0.25.4
[0.25.2]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.25.1...0.25.2
[0.25.1]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.25.0...0.25.1
[0.25.0]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.24.0...0.25.0
[0.24.0]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.23.2...0.24.0
[0.23.2]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.23.1...0.23.2
[0.23.1]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.23.0...0.23.1
[0.23.0]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.22.0...0.23.0
[0.22.0]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.21.1...0.22.0
[0.21.1]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.21.0...0.21.1
[0.21.0]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.20.1...0.21.0
[0.20.1]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.20.0...0.20.1
[0.20.0]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.19.1...0.20.0
[0.19.1]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.19.0...0.19.1
[0.19.0]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.18.0...0.19.0
[0.18.0]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.11.2...0.18.0
[0.11.2]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.11.0-alpha...0.11.2
[0.11.0-alpha]: https://github.com/ihmcrobotics/ihmc-commons/compare/0.9.1-alpha...0.11.0-alpha
[0.9.1-alpha]: https://github.com/ihmcrobotics/ihmc-commons/releases/tag/0.9.1-alpha