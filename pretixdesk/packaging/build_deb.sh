#!/bin/bash
JAVA_RELEASE=9.0.1
JAVA_PATCH=11
VERSION=0.1.0

mkdir -p dist
mkdir deb.tmp
pushd deb.tmp

# Download java
wget -nc --header "Cookie: gpw_e24=http://www.oracle.com; oraclelicense=accept-securebackup-cookie" \
    http://download.oracle.com/otn-pub/java/jdk/$JAVA_RELEASE+$JAVA_PATCH/jre-"$JAVA_RELEASE"_linux-x64_bin.tar.gz
tar xzf jre-"$JAVA_RELEASE"_linux-x64_bin.tar.gz
mv jre-$JAVA_RELEASE jre

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
License: GPL-3 and Oracle Binary Code License Agreement for Java SE
Description: pretix ticket check-in tool
 .
 pretixdesk distribution with bundled JRE
 .
END

mkdir -p debian/usr/lib/pretixdesk
mkdir -p debian/usr/share/applications
mkdir -p debian/usr/bin

cp -r jre debian/usr/lib/pretixdesk/jre
cp ../build/libs/pretixdesk.jar debian/usr/lib/pretixdesk

cat <<'END' > debian/usr/bin/pretixdesk
#!/bin/sh
export JAVA_HOME=/usr/lib/pretixdesk/jre
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