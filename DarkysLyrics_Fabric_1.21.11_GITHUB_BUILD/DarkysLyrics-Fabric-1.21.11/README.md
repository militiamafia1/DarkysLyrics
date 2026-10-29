# Darky's Lyrics — Fabric 1.21.11

This is a **source project**, not a prebuilt or verified Minecraft mod JAR.

## Build without running a local script (GitHub Actions)

1. Create a GitHub account if needed.
2. Create a new repository on GitHub.
3. Upload the contents of this folder to the repository (the `.github/workflows/build.yml` file must be included).
4. Open the repository's **Actions** tab and select **Build Darky's Lyrics**.
5. Click **Run workflow** (or wait for the push-triggered build).
6. If the build succeeds, open that workflow run and download the **darkys-lyrics-jar** artifact.
7. Extract the downloaded artifact ZIP. The `.jar` inside is what goes into the `mods` folder of a Minecraft 1.21.11 Fabric Modrinth profile.

If the build fails, the artifact will not be produced. Open the failed workflow run and inspect the build log; do not install a missing or failed artifact.

## Current feature status

- Press **J** to open the menu.
- Player, Lyrics, and Visuals tabs are included.
- Optional local Windows media metadata bridge source is in `helper/DarkysLyrics.MediaBridge/`.
- Local timestamped `.lrc` lyrics are supported in the prototype.
- Automatic online lyrics lookup, custom sky visuals, and a tested release build are **not complete**.
- The optional Windows helper is source-only and must be built separately on Windows with the .NET 8 SDK. It listens on `127.0.0.1` only and does not ask for music-account credentials.

## Requirements

Minecraft Java 1.21.11, Fabric Loader, Fabric API, and Java 21. This project has not been verified in a live Minecraft instance; successful compilation alone would not prove every feature works.
