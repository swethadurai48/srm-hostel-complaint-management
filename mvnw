#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper Script for Unix
# ----------------------------------------------------------------------------

set -e

[ -z "$MAVEN_HOME" ] || export MAVEN_HOME=""

PRG="$0"
while [ -h "$PRG" ]; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=$(dirname "$PRG")"/$link"
  fi
done

PRGDIR=$(dirname "$PRG")
BASE_DIR=$(cd "$PRGDIR" >/dev/null; pwd)

WRAPPER_JAR="$BASE_DIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"

# Determine JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
  if [ -d "/opt/homebrew/opt/openjdk@17" ]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
    export PATH="$JAVA_HOME/bin:$PATH"
    JAVACMD="$JAVA_HOME/bin/java"
  elif [ -d "/opt/homebrew/opt/openjdk" ]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk"
    export PATH="$JAVA_HOME/bin:$PATH"
    JAVACMD="$JAVA_HOME/bin/java"
  elif which java >/dev/null 2>&1 && java -version >/dev/null 2>&1; then
    JAVACMD="java"
  else
    echo "Error: JAVA_HOME is not set and no working 'java' command could be found." >&2
    exit 1
  fi
else
  JAVACMD="$JAVA_HOME/bin/java"
fi

# Locate or Download Maven
MAVEN_VERSION="3.9.6"
USER_HOME="${HOME:-~}"
MAVEN_DIR="$USER_HOME/.m2/wrapper/dists/apache-maven-$MAVEN_VERSION"

if [ ! -d "$MAVEN_DIR" ]; then
  echo "Downloading Apache Maven $MAVEN_VERSION..."
  mkdir -p "$MAVEN_DIR"
  ARCHIVE="$MAVEN_DIR/apache-maven-$MAVEN_VERSION-bin.tar.gz"
  curl -fsSL "https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz" -o "$ARCHIVE"
  tar -xzf "$ARCHIVE" -C "$MAVEN_DIR" --strip-components=1
  rm -f "$ARCHIVE"
fi

exec "$MAVEN_DIR/bin/mvn" "$@"
