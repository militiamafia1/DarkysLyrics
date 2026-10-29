# Darky's Lyrics Media Bridge (source)

This small Windows helper reads the **current Windows media session** (for example, a player that publishes Now Playing metadata) and exposes only title, artist, album, play state, and playback position to a local Minecraft client.

## Safety design

- Source code is included for inspection.
- Listens only on `127.0.0.1` (loopback), not on the LAN or public internet.
- Does not ask for Spotify credentials, cookies, passwords, or API tokens.
- Does not launch, install, or modify other programs.
- Does not download or execute code.
- Does not send media metadata to a remote server. The helper itself makes no outbound web requests.
- Stop it at any time with Ctrl+C.

No software can be guaranteed malware-free just from a description. Review the source and build it yourself if you want the strongest assurance. This is source code, not a compiled executable.

## Build

Requires Windows 10/11, .NET 8 SDK, and internet access to restore the Windows SDK NuGet package on first build.

From this folder in PowerShell:

```powershell
dotnet build -c Release
dotnet run
```

The helper should print that it is listening on `127.0.0.1:38471`.

Test in a browser:
- `http://127.0.0.1:38471/health`
- `http://127.0.0.1:38471/nowplaying`

It reports the current Windows media session. The player must expose metadata through Windows' media-session API. If it doesn't, the helper may show no session.

## Important

This helper currently provides media metadata and playback position only. The Minecraft mod still needs to poll this endpoint and match the song to available timestamped lyrics. Automatic lyrics fetching and in-game bridge integration are not included in this helper-only milestone.
