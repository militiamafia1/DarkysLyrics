using System.Net;
using System.Text.Json;
using Windows.Media.Control;

const string Prefix = "http://127.0.0.1:38471/";
using var listener = new HttpListener();
listener.Prefixes.Add(Prefix);

try
{
    listener.Start();
    Console.WriteLine("Darky's Lyrics Media Bridge");
    Console.WriteLine("Listening only on 127.0.0.1:38471 (this PC only).");
    Console.WriteLine("Press Ctrl+C to quit. No Spotify password or token is requested.");
}
catch (Exception ex)
{
    Console.Error.WriteLine($"Could not start local listener: {ex.Message}");
    return;
}

GlobalSystemMediaTransportControlsSessionManager? manager = null;
try
{
    manager = await GlobalSystemMediaTransportControlsSessionManager.RequestAsync();
}
catch (Exception ex)
{
    Console.Error.WriteLine($"Windows media session access unavailable: {ex.Message}");
}

while (listener.IsListening)
{
    HttpListenerContext context;
    try { context = await listener.GetContextAsync(); }
    catch (HttpListenerException) { break; }

    // Reject anything that is not a simple local GET endpoint.
    if (context.Request.HttpMethod != "GET" ||
        (context.Request.Url?.AbsolutePath != "/nowplaying" &&
         context.Request.Url?.AbsolutePath != "/health"))
    {
        context.Response.StatusCode = 404;
        context.Response.Close();
        continue;
    }

    context.Response.Headers["Access-Control-Allow-Origin"] = "http://127.0.0.1";
    context.Response.ContentType = "application/json; charset=utf-8";

    object response;
    if (context.Request.Url!.AbsolutePath == "/health")
    {
        response = new { ok = true, app = "DarkysLyrics.MediaBridge" };
    }
    else
    {
        response = await GetNowPlayingAsync(manager);
    }

    byte[] bytes = JsonSerializer.SerializeToUtf8Bytes(response);
    context.Response.ContentLength64 = bytes.Length;
    try
    {
        await context.Response.OutputStream.WriteAsync(bytes);
    }
    finally { context.Response.Close(); }
}

static async Task<object> GetNowPlayingAsync(GlobalSystemMediaTransportControlsSessionManager? manager)
{
    if (manager is null)
        return new { playing = false, title = "", artist = "", album = "", positionMs = 0L, status = "unavailable" };

    try
    {
        var session = manager.GetCurrentSession();
        if (session is null)
            return new { playing = false, title = "", artist = "", album = "", positionMs = 0L, status = "no-session" };

        var props = await session.TryGetMediaPropertiesAsync();
        var playback = session.GetPlaybackInfo();
        var timeline = session.GetTimelineProperties();

        return new
        {
            playing = playback.PlaybackStatus == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing,
            title = props?.Title ?? "",
            artist = props?.Artist ?? "",
            album = props?.AlbumTitle ?? "",
            positionMs = Math.Max(0L, (long)timeline.Position.TotalMilliseconds),
            status = playback.PlaybackStatus.ToString()
        };
    }
    catch
    {
        return new { playing = false, title = "", artist = "", album = "", positionMs = 0L, status = "read-error" };
    }
}
