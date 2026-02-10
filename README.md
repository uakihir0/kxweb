> [日本語](./docs/README_ja.md)

# kxweb

![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.repsy.io%2Fmvn%2Fuakihir0%2Fpublic%2Fwork%2Fsocialhub%2Fkxweb%2Fcore%2Fmaven-metadata.xml)

![badge][badge-jvm]
![badge][badge-ios]
![badge][badge-mac]
![badge][badge-windows]
![badge][badge-linux]

**This library is an X (Twitter) web client library compatible with [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html).**
It depends on [khttpclient] and internally uses Ktor Client.
Therefore, this library is available on Kotlin Multiplatform and platforms supported by Ktor Client.
The behavior on each platform depends on [khttpclient].

This project is strongly inspired by [Nitter](https://github.com/zedeus/nitter) and [bird](https://github.com/steipete/bird), which provide valuable insights into Twitter/X web API interactions.

**Note:** This library uses X (Twitter)'s unofficial/undocumented web APIs, which may change without notice. The API stability cannot be guaranteed, and functionality may break due to platform updates.

## Usage

Below is how to use it in Kotlin with Gradle on supported platforms.
**If you want to use it on Apple platforms, please refer to kxweb-spm or kxweb-cocoapods (coming soon).**
**Also, for usage in JavaScript, please refer to kxweb.js (coming soon).**
Please refer to the test code for how to use each API once available.

### Stable (Maven Central)

Once the library is published to Maven Central, you can use it as follows:

```kotlin:build.gradle.kts
repositories {
    mavenCentral()
}

dependencies {
    implementation("work.socialhub.kxweb:core:[VERSION]")
}
```

### Snapshot

```kotlin:build.gradle.kts
repositories {
    maven { url = uri("https://repo.repsy.io/mvn/uakihir0/public") }
}

dependencies {
    implementation("work.socialhub.kxweb:core:0.1.0-SNAPSHOT")
}
```

### Using as part of a regular Java project

All of the above can be added to and used in regular Java projects, too. All you have to do is to use the suffix `-jvm` when listing the dependency.

Here is a sample Maven configuration:

```xml
<dependency>
    <groupId>work.socialhub.kxweb</groupId>
    <artifactId>core-jvm</artifactId>
    <version>[VERSION]</version>
</dependency>
```

### Authentication

This library supports two authentication methods.

#### Cookie-Based Authentication (Recommended)

Extract `auth_token` and `ct0` cookies from your browser while logged into x.com.

**Option A: Browser DevTools Console**

Open DevTools (F12) on x.com and run the following in the Console:

```javascript
console.log("auth_token:", document.cookie.split('; ').find(c => c.startsWith('auth_token='))?.split('=')[1]);
console.log("ct0:", document.cookie.split('; ').find(c => c.startsWith('ct0='))?.split('=')[1]);
```

**Option B: DevTools Application Tab**

1. Open DevTools (F12) on x.com
2. Go to **Application** > **Cookies** > `https://x.com`
3. Copy the values of `auth_token` and `ct0`

Then create an instance:

```kotlin
val xweb = XWebFactory.instance(
    authToken = "your_auth_token",
    csrfToken = "your_ct0_value"
)
```

#### OAuth1 Authentication (JVM Only)

Uses OAuth 1.0a with HMAC-SHA1 signature (same approach as Nitter). The library includes public consumer credentials internally, so you only need to provide user-level tokens.

```kotlin
val xweb = XWebFactory.instanceOAuth(
    oauthToken = "your_oauth_token",
    oauthSecret = "your_oauth_secret"
)
```

> **Note:** OAuth1 authentication is currently only supported on JVM. On JS and Native platforms, use cookie-based authentication instead.

### Features

This library is currently under active development. The following features are planned:

- **Authentication**: Support for various authentication methods to access X (Twitter) APIs
- **Timeline Operations**: Read tweets, user timelines, and related content
- **Post Operations**: Create tweets and interact with posts
- **Search**: Search for tweets, users, and hashtags
- **User Operations**: Access user profiles and related information

Detailed API usage examples will be available through test code as the implementation progresses.

### Important Notes

- **API Stability**: As this library relies on unofficial/undocumented X (Twitter) APIs, breaking changes may occur without prior notice
- **Rate Limiting**: Be mindful of X (Twitter)'s rate limiting policies when making requests
- **Privacy**: The design is influenced by privacy-focused approaches, handling requests server-side where possible

## License

MIT License

## Author

[Akihiro Urushihara](https://github.com/uakihir0)

[khttpclient]: https://github.com/uakihir0/khttpclient
[badge-android]: http://img.shields.io/badge/-android-6EDB8D.svg
[badge-android-native]: http://img.shields.io/badge/support-[AndroidNative]-6EDB8D.svg
[badge-wearos]: http://img.shields.io/badge/-wearos-8ECDA0.svg
[badge-jvm]: http://img.shields.io/badge/-jvm-DB413D.svg
[badge-js]: http://img.shields.io/badge/-js-F8DB5D.svg
[badge-js-ir]: https://img.shields.io/badge/support-[IR]-AAC4E0.svg
[badge-nodejs]: https://img.shields.io/badge/-nodejs-68a063.svg
[badge-linux]: http://img.shields.io/badge/-linux-2D3F6C.svg
[badge-windows]: http://img.shields.io/badge/-windows-4D76CD.svg
[badge-wasm]: https://img.shields.io/badge/-wasm-624FE8.svg
[badge-apple-silicon]: http://img.shields.io/badge/support-[AppleSilicon]-43BBFF.svg
[badge-ios]: http://img.shields.io/badge/-ios-CDCDCD.svg
[badge-mac]: http://img.shields.io/badge/-macos-111111.svg
[badge-watchos]: http://img.shields.io/badge/-watchos-C0C0C0.svg
[badge-tvos]: http://img.shields.io/badge/-tvos-808080.svg
