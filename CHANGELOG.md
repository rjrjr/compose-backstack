Changelog
=========

## v0.8.0+beta02

_2021-03-15_

 * Upgrade: Compose to beta02. (#39, #47)
 * Completely rewrote most of the internals and APIs: (#50)
   * All the actual transition logic is moved out of the `Backstack` composable and into `TransitionController`.
   * `BackstackInspector` and `InspectionGestureDetector` are replaced with a single API in a new, dedicated module: `xrayed()`.
   * `Backstack` now just handles the container and managing saveable state.
   * Introduces `FrameController`, an abstraction which can be used to implement both transitions, the inspector functionality, and things like #17.
   * Removes the spring animations from the inspector. They added unnecessary lag to the gestures.
   * Adds more tests for state handling.
   * Fixed some races that caused navigation animation glitches.
 * Use Material DropdownMenu for spinner. (#51)

## v0.6.0+alpha04

_2020-10-13_

 * Upgrade: Compose to 0.1.0-alpha04. (#37)

## v0.5.0+dev16

_2020-08-09_

 * Upgrade: Compose to dev16. (#32)

## v0.4.0

_2020-07-13_

 * New: Start using `UiSavedStateRegistry` to preserve screen "view" state. (#25)
 * New: Automatically save/restore the state of a `BackstackViewerApp` using the new saved state tools. (#21)
   * Buggy implementation subsequently fixed by @grandstaish in #29, thanks!
 * Fix: Rename `master` branch to `main`.
 * Upgrade: Kotlin to 1.3.71. (#15)
 * Upgrade: Compose to dev14. (#31)

## v0.3.0

_2020-03-22_

 * Introduce inspection mode for peering into the past. (#12)
 * Add RTL support to the `Slide` transition.
 * Add system back support to the viewer app. (#14)
 * Replace custom opacity modifier with the new built-in one.
 * Add more API documentation to the README.
 * Tweak the sample custom transition. (#10)

## v0.2.0

_2020-03-22_

 * Update Compose to dev07.

## v0.1.0

_2020-03-22_

 * Initial release. Supports Compose dev06.
