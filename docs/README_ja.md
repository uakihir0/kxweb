# kxweb

![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.repsy.io%2Fmvn%2Fuakihir0%2Fpublic%2Fwork%2Fsocialhub%2Fkxweb%2Fcore%2Fmaven-metadata.xml)

![badge][badge-jvm]
![badge][badge-ios]
![badge][badge-mac]
![badge][badge-windows]
![badge][badge-linux]

**このライブラリは [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) に対応した X (Twitter) web クライアントライブラリです。**
[khttpclient] を依存関係に持っており、 内部で Ktor Client を使用しています。
そのため、本ライブラリは、Kotlin Multiplatform かつ Ktor Client がサポートしているプラットフォームであれば利用可能です。
各プラットフォームでどのような挙動をするのかについては、[khttpclient] に依存します。

このプロジェクトは [Nitter](https://github.com/zedeus/nitter) と [bird](https://github.com/steipete/bird) から強い影響を受けており、Twitter/X web API との連携に関する貴重な知見を提供しています。

**注意:** このライブラリは X (Twitter) の非公式/非公開の web API を使用しています。API は予告なく変更される可能性があり、安定性は保証されません。プラットフォームの更新により機能が動作しなくなる可能性があります。

## 使い方

以下は対応するプラットフォームにおいて Gradle を用いて Kotlin で使用する際の使い方になります。
**Apple プラットフォームで使用する場合は、kxweb-spm または kxweb-cocoapods を参照してください（準備中）。**
**また、JavaScript で使用する場合は、kxweb.js を参照してください（準備中）。**
各 API の使用方法については、実装が進むにつれてテストコードを参照してください。

### Stable (Maven Central)

ライブラリが Maven Central に公開された際は、以下のように使用できます。

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

### 通常の Java プロジェクトの一部として使用する

上記のすべては、通常の Java プロジェクトにも追加して使用できます。依存関係をリストする際に、サフィックス `-jvm` を使用するだけです。

Maven の設定例は以下の通りです。

```xml
<dependency>
    <groupId>work.socialhub.kxweb</groupId>
    <artifactId>core-jvm</artifactId>
    <version>[VERSION]</version>
</dependency>
```

### 機能

このライブラリは現在積極的に開発中です。以下の機能を予定しています。

- **認証**: X (Twitter) API にアクセスするための各種認証方法のサポート
- **タイムライン操作**: ツイート、ユーザータイムライン、および関連コンテンツの読み取り
- **投稿操作**: ツイートの作成および投稿とのインタラクション
- **検索**: ツイート、ユーザー、ハッシュタグの検索
- **ユーザー操作**: ユーザープロフィールおよび関連情報へのアクセス

実装が進むにつれて、詳細な API 使用例はテストコードを通じて提供される予定です。

### 重要な注意事項

- **API の安定性**: このライブラリは非公式/非公開の X (Twitter) API に依存しているため、予告なく破壊的な変更が発生する可能性があります
- **レート制限**: リクエストを行う際は、X (Twitter) のレート制限ポリシーに注意してください
- **プライバシー**: 設計はプライバシーに配慮したアプローチの影響を受けており、可能な限りサーバーサイドでリクエストを処理します

## ライセンス

MIT License

## 作者

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
