# Mustang Player — Android music and video

Native Android 8+ music/video app with a custom running-horse logo. Install the single **Mustang-Player.apk** from [Releases](https://github.com/dogra1212k/Music-player/releases/latest). Releases appear only after unit tests, lint, APK builds and Android 15 emulator playback checks pass.

## In the app

- **Files** imports multiple audio/video files through Android's file picker, without broad storage permissions. MP3, WAV, AAC, FLAC, MP4 and other formats play when supported by the phone's codecs.
- **Stream** adds direct HTTPS media URLs. Enter one URL or quality variants such as `360p=https://…` and `720p=https://…`. Web page URLs, DRM services and YouTube/Spotify links are not direct media.
- Music/video filters, search and language filters include Hindi, Punjabi, English, Bhojpuri, Tamil, Telugu, Bengali, Marathi, Gujarati, Kannada, Malayalam and Urdu. Long-press a track to edit its title, artist and language or remove it from the library. Any script/language can appear in names and lyrics; files with no metadata start as Other.
- Foreground playback keeps music and a video's **audio** running when the app is backgrounded or the screen is off. Notification and headset controls support play/pause, previous, next and stop. Removing the app task does not intentionally stop playback; force-stop always does.
- **Mini video** opens picture-in-picture where supported. PiP shows video while other apps are open; screen-off playback is audio only.
- **Auto-play next** advances through the full library queue until its end, including music and video. Filters affect browsing, not queue order. **Auto-scroll** follows the active queue item and highlighted timestamped lyrics; it does not continuously move a feed on a timer.
- **Lyrics → Find** searches LRCLIB after an explicit user action. The title/artist are sent to LRCLIB, and the user chooses the matching recording. Availability varies by language/song. Saved lyrics can be read offline.
- **Import LRC** accepts UTF-8 LRC or TXT lyrics. LRC follows playback, supports repeated timestamps and offsets, and allows tapping a line to seek. Plain lyrics display without timing.
- **Quality** switches only between real imported files or added URLs for the same video, preserving playback position and play/pause state. It does not invent HD quality or upscale a source. Add matching versions of the same recording for accurate switching.
- Three original 20-second instrumental demos with original English, Hindi and Punjabi reading lyrics are bundled; these have **no sung vocals**. One original video demo includes real 180p and 360p encodes. Demo music/lyrics/video are CC0.

## Content and limits

This app is a personal media player, not a licensed all-songs streaming catalog. It does not include commercial music or scrape subscription platforms. Add media you can access or your provider's direct playable streams. Network streaming and online lyrics need internet. Local media, bundled demos and cached lyrics work offline. Missing/moved files must be re-added. Persisted access depends on the selected document provider.

The Mustang horse mark is an original app icon; this project is not affiliated with Ford. File titles and media stay locally stored. Only explicit lyric searches contact LRCLIB; playback URLs contact their hosts. No analytics or account required.

## Development

JDK 17, Gradle 8.9, Android SDK/build tools 35, AGP 8.7.3. No external playback SDK is used.

```sh
gradle testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
```

GitHub Actions tests the LRC parser, renders the home screen, actually plays bundled audio and video, seeks/pauses/resumes/stops playback and switches video quality while preserving time. Target-device checks remain needed for Bluetooth, manufacturer battery restrictions, PiP behavior, network sources and lyrics-service availability. APKs are debug-signed sideload builds, not Play Store releases. Pin a production signing key before distributing updates broadly.
