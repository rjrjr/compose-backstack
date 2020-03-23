# compose-backstack
[![](https://jitpack.io/v/zach-klippenstein/compose-backstack.svg)](https://jitpack.io/#zach-klippenstein/compose-backstack)

Simple library for [Jetpack Compose](https://developer.android.com/jetpack/compose) for rendering
backstacks of screens and animated transitions when the stack changes. It is _not_ a navigation
library, although it is meant to be easy to plug into your navigation library of choice
(e.g. [compose-router](https://github.com/zsoltk/compose-router)), or even just use on its own.

This library is compatible with Compose dev06.

## Usage

The entry point to the library is the `Backstack` composable.

## Example

```kotlin
 sealed class Screen {
   object ContactList: Screen()
   data class ContactDetails(val id: String): Screen()
   data class EditContact(val id: String): Screen()
 }

 data class Navigator(
   val push: (Screen) -> Unit,
   val pop: () -> Unit
 )

 @Composable fun App() {
   var backstack by state { listOf(Screen.ContactList) }
   val navigator = remember {
     Navigator(
       push = { backstack += it },
       pop = { backstack = backstack.dropLast(1) }
     )
   }

   Backstack(backstack) { screen ->
     when(screen) {
       Screen.ContactList -> ShowContactList(navigator)
       is Screen.ContactDetails -> ShowContact(screen.id, navigator)
       is Screen.EditContact -> ShowEditContact(screen.id, navigator)
     }
   }
 }
```

## Samples

There is a sample app in the `sample` module that demonstrates various transition animations and
the behavior with different backstacks.
## Gradle

`compose-backstack` is available from Jitpack:

```
allprojects {
    repositories {
        …
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.zach-klippenstein:compose-backstack:0.1.0'
}
```
