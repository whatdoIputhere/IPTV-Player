# IPTV Player

A simple Android IPTV player built with Jetpack Compose and ExoPlayer.

## Features
- Channel categories grid with search
- In-category channel search with instant filtering
- M3U playlist parsing with metadata
- Video playback via ExoPlayer (HLS supported)
- Saved playlists management entry point

## Tech stack
- Kotlin, Coroutines
- Jetpack Compose + Material 3
- Lifecycle ViewModel + Navigation Compose
- ExoPlayer 2.x (core/ui/hls)
- Retrofit 2 + Gson + OkHttp (with logging)
- Coil for images
- Room (runtime/ktx, kapt)

## Requirements
- JDK 21
- compileSdk 36, targetSdk 36, minSdk 33
- Gradle Wrapper 8.13

## License
This project is for educational purposes. Ensure you have rights to any IPTV content you stream.