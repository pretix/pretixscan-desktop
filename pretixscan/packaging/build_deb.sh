#!/bin/bash
VERSION=0.3.3

mkdir -p dist
mkdir deb.tmp
pushd deb.tmp

mkdir -p debian
mkdir -p debian/DEBIAN

# Write control files
cat <<END > debian/DEBIAN/control
Package: pretixscan
Version: $VERSION
Section: web
Priority: optional
Architecture: amd64
Maintainer: Raphael Michel
License: GPL-3
Depends: openjdk-8-jre, openjfx
Description: pretix ticket check-in tool
 .
 pretixscan distribution
 .
END

mkdir -p debian/usr/lib/pretixscan
mkdir -p debian/usr/share/applications
mkdir -p debian/usr/bin

cp ../build/libs/pretixscan.jar debian/usr/lib/pretixscan

cat <<'END' > debian/usr/bin/pretixscan
#!/bin/sh
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/jre/
$JAVA_HOME/bin/java -jar /usr/lib/pretixscan/pretixscan.jar $*
END
chmod +x debian/usr/bin/pretixscan

cat <<END > debian/usr/share/applications/pretixscan.desktop
[Desktop Entry]
Name=pretixscan
Exec=pretixscan %u
Type=Application
Terminal=false
MimeType=x-scheme-handler/pretixscan;
END

DPKG=dpkg
DEBDIR=$(pwd)
if ! hash $DPKG 2>/dev/null
then
    DPKG="docker run --rm --entrypoint /usr/bin/dpkg -v $(pwd):/tmp/deb -it raphaelm/ci-pretixdesk-apt"
    DEBDIR=/tmp/deb
fi
$DPKG --build $DEBDIR/debian

mv -f debian.deb ../dist/pretixscan.deb

popd
#rm -rf deb.tmp
