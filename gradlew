#!/usr/bin/env bash
set -euo pipefail
GRADLE_VERSION="8.9"
GRADLE_HOME="$HOME/.gradle/custom-gradle-$GRADLE_VERSION"
if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  mkdir -p "$HOME/.gradle"
  TMP_ZIP="$HOME/.gradle/gradle-$GRADLE_VERSION-bin.zip"
  curl -L --retry 3 -o "$TMP_ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  rm -rf "$GRADLE_HOME" "$HOME/.gradle/gradle-$GRADLE_VERSION"
  unzip -q "$TMP_ZIP" -d "$HOME/.gradle"
  mv "$HOME/.gradle/gradle-$GRADLE_VERSION" "$GRADLE_HOME"
fi
exec "$GRADLE_HOME/bin/gradle" "$@"
