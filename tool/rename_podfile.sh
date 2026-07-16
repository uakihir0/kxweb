#!/usr/bin/env bash
cd "$(dirname "$0")" || exit
BASE_PATH=$(pwd)
BUILD_PATH=../all/build

# Make Repository
cd "$BASE_PATH" || exit
mkdir -p $BUILD_PATH/cocoapods/repository/debug
mkdir -p $BUILD_PATH/cocoapods/repository/release

# Copy Podspec
cd "$BASE_PATH" || exit
cd $BUILD_PATH/cocoapods/publish/debug || exit
cp kxweb.podspec ../../repository/kxweb-debug.podspec
cd ../../repository/ || exit
sed -i -e "s|'kxweb'|'kxweb-debug'|g" kxweb-debug.podspec
sed -i -e "s|'kxweb.xcframework'|'debug/kxweb.xcframework'|g" kxweb-debug.podspec
rm *.podspec-e
cd "$BASE_PATH" || exit
cd $BUILD_PATH/cocoapods/publish/release || exit
cp kxweb.podspec ../../repository/kxweb-release.podspec
cd ../../repository/ || exit
sed -i -e "s|'kxweb'|'kxweb-release'|g" kxweb-release.podspec
sed -i -e "s|'kxweb.xcframework'|'release/kxweb.xcframework'|g" kxweb-release.podspec
rm *.podspec-e

# Copy Framework
cd "$BASE_PATH" || exit
cd $BUILD_PATH/cocoapods/publish/debug || exit
cp -r kxweb.xcframework ../../repository/debug/kxweb.xcframework
cd "$BASE_PATH" || exit
cd $BUILD_PATH/cocoapods/publish/release || exit
cp -r kxweb.xcframework ../../repository/release/kxweb.xcframework

# Copy README
cd "$BASE_PATH" || exit
cd ../ || exit
cp ./LICENSE ./all/build/cocoapods/repository/LICENSE
cp ./docs/pods/README.md ./all/build/cocoapods/repository/README.md
cp ./docs/pods/README_ja.md ./all/build/cocoapods/repository/README_ja.md
