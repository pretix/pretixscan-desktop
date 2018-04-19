#!/bin/bash
VERSION=0.3.3

mkdir -p dist
mkdir deb.tmp
pushd deb.tmp

mkdir -p debian
mkdir -p debian/DEBIAN

# Write control files
cat <<END > debian/DEBIAN/control
Package: pretixdesk
Version: $VERSION
Section: web
Priority: optional
Architecture: amd64
Maintainer: Raphael Michel
License: GPL-3
Depends: openjdk-8-jre, openjfk
Description: pretix ticket check-in tool
 .
 pretixdesk distribution
 .
END

mkdir -p debian/usr/lib/pretixdesk
mkdir -p debian/usr/share/applications
mkdir -p debian/usr/bin

cp ../build/libs/pretixdesk.jar debian/usr/lib/pretixdesk

cat <<'END' > debian/usr/bin/pretixdesk
#!/bin/sh
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/jre/
$JAVA_HOME/bin/java -jar /usr/lib/pretixdesk/pretixdesk.jar $*
END
chmod +x debian/usr/bin/pretixdesk

cat <<END > debian/usr/share/applications/pretixdesk.desktop
[Desktop Entry]
Name=pretixdesk
Exec=pretixdesk %u
Type=Application
Terminal=false
MimeType=x-scheme-handler/pretixdesk;
END

DPKG=dpkg
DEBDIR=$(pwd)
if ! hash $DPKG 2>/dev/null
then
    DPKG="docker run --rm --entrypoint /usr/bin/dpkg -v $(pwd):/tmp/deb -it raphaelm/ci-pretixdesk-apt"
    DEBDIR=/tmp/deb
fi
$DPKG --build $DEBDIR/debian

mv debian.deb ../dist/pretixdesk.deb

popd
#rm -rf deb.tmp
