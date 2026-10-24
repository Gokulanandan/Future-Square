# Future Square

An Android game based on the 2000s school notebook game for predicting your future.
Fill the sides of a square with possible futures (rank, ambition, kind of house, best friend,
and any extra fields you like), say "start" and "stop" while lines are drawn, and a pencil
strikes values out one by one until one value is left in every field. The result is a
sentence and a picture of your future.

## Install on a phone

Every push to `main` builds the app on GitHub. Open the **latest** release on your Android
phone, download `future-square.apk` and open it. Android will ask you to allow installs
from your browser or files app the first time.

## How the game counts

- Start at the right end of the bottom field and go round: bottom right to left, left side
  bottom to top, top left to right, right side top to bottom.
- Count the number of lines drawn and strike the value you land on. Carry on from the next value.
- A field with one value left is closed and skipped.
- Up to eight fields; each side holds two (inner one counted first).

The rules live in `core/` as plain Kotlin with unit tests. The Jetpack Compose app is in `app/`.

## Build

```
./gradlew :core:test :app:assembleDebug
```

Needs JDK 17 and the Android SDK (API 34).
