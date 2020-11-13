# Changelog

## [unreleased]

### Added

- Added a `clientDetails` property to the `testLab` extension and both `instrumentation` and `robo` tests. This is a map 
  of arbitrary keys and values passed along with each test, which can be retrieved via Firebase Cloud Functions after
  the test has completed.

## [0.5.0] - 2020-11-13

### Added

- Create additional test tasks for app bundles. These are named 'testLab${test}BundleTest'. Existing tests for APKs are
  not affected.
- Support for split APKs, which are passed via the `additionalApks` property.
- Support for additional Robo test options:
  - `script`: File containing scripted actions taken by the Robo test runner.
  - `startingIntents { }`: Intents used to start the application for the Robo crawl.
- Support for additional common test options:
  - `dontAutograntPermissions`: Prevent all runtime permissions from being granted at install time.
  - `systrace { }`: Enables collection of systrace data.
- Support for Instrumentation test sharding.
  - The following options have migrated to a `targets { }` closure and are deprecated:
    - `testTargets`
    - `targetPackage`
    - `targetClass`
    - `targetMethod`
  - Shards are configured within the `targets { }` closure:
    - `shardCount`: Uniformly distribute test targets among this number of shards.
    - `shard { }`: Add an explicit shard of test targets.

### Changed

- Require Android Gradle Plugin 4.1 in order to support app bundles using the new AGP DSL.
- Split upload tasks into finer-grained units. Out-of-date checks for these tasks are now scoped to the task outputs
  which produce the files. For example, the "upload APK" task will not be considered out-of-date if the APKs have not
  changed since being uploaded.

### Fixed

- Infer `projectId` from the provided service credentials for the ToolResults API.
- Do not generate instrumentation test tasks for non-tested app variants such as release builds.

## [0.4.1] - 2020-06-30

### Changed

- Fixed crashes due to improperly-serialized task inputs.

## [0.4.0] - 2020-06-25

### Added

- Initial public release.

[unreleased]: https://github.com/SimpleFinance/gradle-test-lab-plugin/compare/v0.5.0...HEAD
[0.5.0]: https://github.com/SimpleFinance/gradle-test-lab-plugin/compare/v0.4.1...v0.5.0
[0.4.1]: https://github.com/SimpleFinance/gradle-test-lab-plugin/compare/v0.4.0...v0.4.1
[0.4.0]: https://github.com/SimpleFinance/gradle-test-lab-plugin/releases/tag/v0.4.0
