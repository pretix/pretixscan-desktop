set MODULE_PATH=%1
set INPUT=%2
set OUTPUT=%3
set JAR=%4
set VERSION=%5
set APP_ICON=%6

call "%JAVA_HOME%\bin\java.exe" ^
    -Xmx512M ^
    --module-path "%JAVA_HOME%\jmods" ^
    --add-opens jdk.jlink/jdk.tools.jlink.internal.packager=jdk.packager ^
    -m jdk.packager/jdk.packager.Main ^
    create-image ^
    --module-path "%MODULE_PATH%" ^
    --verbose ^
    --echo-mode ^
    --add-modules "java.base,java.datatransfer,java.desktop,java.scripting,java.xml,jdk.jsobject,jdk.unsupported,jdk.unsupported.desktop,jdk.xml.dom,javafx.controls,javafx.fxml,javafx.graphics,java.naming,java.sql,jdk.charsets,jdk.crypto.ec" ^
    --input "%INPUT%" ^
    --output "%OUTPUT%" ^
    --name "pretixSCAN" ^
    --main-jar "%JAR%" ^
    --version "%VERSION%" ^
    --jvm-args "--add-opens javafx.base/com.sun.javafx.reflect=ALL-UNNAMED --add-exports=javafx.controls/com.sun.javafx.scene.control.inputmap=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED -Dfile.encoding=UTF8" ^
    --icon "%APP_ICON%" ^
    --class "eu.pretix.pretixscan.desktop.PretixScanMain"
