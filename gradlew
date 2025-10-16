#!/usr/bin/env sh

# Simplified Gradle Wrapper startup script for POSIX (Linux/macOS)
# Launches org.gradle.wrapper.GradleWrapperMain using the wrapper JAR.

set -e

DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"

# Resolve APP_HOME to the absolute path of this script's directory
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' >/dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/." >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Locate Java
if [ -n "$JAVA_HOME" ] ; then
  if [ -x "$JAVA_HOME/bin/java" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
  else
    echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
    exit 1
  fi
else
  JAVACMD="java"
fi

# On macOS launched from Finder, JAVA_HOME may not be set; attempt discovery
if [ -z "$JAVA_HOME" ] && [ "`uname`" = "Darwin" ] && [ -x "/usr/libexec/java_home" ]; then
  JAVA_HOME=`/usr/libexec/java_home`
  JAVACMD="$JAVA_HOME/bin/java"
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS -Dorg.gradle.appname="$0" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"

