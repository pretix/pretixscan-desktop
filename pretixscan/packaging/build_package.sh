
#!/bin/bash
set -e

PACKAGER=${1}
INSTALLER_TYPE=${2}
MODULE_PATH=${3}
INPUT=${4}
OUTPUT=${5}
JAR=${6}
VERSION=${7}
APP_ICON=${8}
EXTRA_BUNDLER_ARGUMENTS=${9}
echo $INPUT > /tmp/debug

${PACKAGER} \
  create-installer ${INSTALLER_TYPE} \
  --module-path ${MODULE_PATH} \
  --verbose \
  --echo-mode \
  --license-file ../../../../LICENSE \
  --copyright "pretix.eu, Raphael Michel" \
  --vendor "pretix" \
  --linux-deb-maintainer "pretix team <support@pretix.eu>" \
  --add-modules java.base,java.datatransfer,java.desktop,java.scripting,java.xml,jdk.jsobject,jdk.unsupported,jdk.unsupported.desktop,jdk.xml.dom,javafx.controls,javafx.fxml,javafx.graphics,java.naming,java.sql,jdk.charsets \
  --input "${INPUT}" \
  --output "${OUTPUT}" \
  --name pretixSCAN \
  --main-jar ${JAR} \
  --version ${VERSION} \
  --jvm-args '--add-opens javafx.base/com.sun.javafx.reflect=ALL-UNNAMED --add-exports=javafx.controls/com.sun.javafx.scene.control.inputmap=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED' \
  --icon $APP_ICON \
  $EXTRA_BUNDLER_ARGUMENTS \
  --class eu.pretix.pretixscan.desktop.PretixScanMain
