# WebPanels Standalone — 0.2.0 development source

Target: Minecraft 1.21.4, Fabric, Java 21.

## Current milestone
- `O` opens an embedded Chromium browser screen.
- URL navigation, mouse movement/clicks/wheel, and keyboard input are forwarded to MCEF.
- MCEF is a separate LGPL-2.1 library dependency; install the matching Fabric MCEF 2.1.6-1.21.4 jar in `mods`.
- YouTube playback and audio depend on the user's MCEF/CEF runtime, platform codecs, and the site's current compatibility. This project has not yet been runtime-tested.

## Build
Use JDK 21 and Gradle 8.10+ (or import the project into IntelliJ IDEA with the Fabric Loom plugin). Run:

```bash
gradle build
```

The resulting mod jar is in `build/libs/`.

## Next milestones (not implemented in this build)
- 3D world-space panels with placement, movement, rotation, resize and curved geometry.
- Multiple independent panels/tabs and per-panel persistence.
- Spatial audio routing and multiplayer panel/frame synchronization.

This project is a separate client mod and does not bundle EnoughVisuals classes or assets. The EnoughVisuals repository is GPL-3.0; MCEF is LGPL-2.1 and remains a separate dependency. Keep the relevant notices when distributing derivative components.
