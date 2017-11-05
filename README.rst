pretixdesk
==========

Checking in your attendees, one ticket at a time.

What is this?
-------------

This is a cross-platform desktop application that handles attendee check-in for events managed
with [pretix](https://pretix.eu/), the open source ticket sales system of choice.

Project status
--------------

This is really early stage. Please do not use it yet.

Building and Running
--------------------

This project is built using Gradle. It requires a JDK in version 9 with JavaFX. As this is
currently hard to achieve using OpenJDK, we advise using Oracle's Java distribution. You
can then build the project using::

    JAVA_HOME=/usr/lib/jvm/java-9-jdk ./gradlew build

You of course need to replace ``JAVA_HOME`` with the location of your Java 9 SDK.
We recommend using IntelliJ IDEA as an development environment.
To run the application from the command line, use::

    JAVA_HOME=/usr/lib/jvm/java-9-jdk ./gradlew build

To build a stand-alone JAR file for distributing::
    
    JAVA_HOME=/usr/lib/jvm/java-9-jdk ./gradlew fatJar

The JAR file will be created at ``build/jfx/app/pretixdesk-all-VERSION.jar``.

Windows package
^^^^^^^^^^^^^^^

To build the windows executable, you can run::
    
    JAVA_HOME=/usr/lib/jvm/java-9-jdk ./gradlew createExe

To then create the installer, you will need to download the .tar.gz file of the
[Java 9 JRE](http://www.oracle.com/technetwork/java/javase/downloads/jre9-downloads-3848532.html) and
unpack it to a folger called ``jre`` within the source directory. You also need to 
install [Nullsoft Install System](http://nsis.sourceforge.net/Download). Then, you can create
the installer with::

    makensis windows.nsi

FAQ
---

**Why Java?**

It's not Java, it's [Kotlin](https://kotlinlang.org/).

**Why Kotlin? You are a Python shop, after all!**

Yes, and we still love Python. However, all cross-platform GUI frameworks suck in some way,
and their Python bindings are rarely Pythonic or nice to use. Additionally, it is really hard
to create good standalone application packages for Windows users for Python. JavaFX isn't perfect
as well, but [tornadofx](https://github.com/edvin/tornadofx) has a nice Kotlin abstraction and
[JFoenix](https://github.com/jfoenixadmin/JFoenix) has nice material-design elements.

Additionally, with the choice of a JVM-based language we can reuse lots of code from our
[pretixdroid](https://github.com/pretix/pretixdroid) Android application, making it easier to
bring new features to both platforms.

**Why not just a web app?**

Ever organized an event and right before the start your internet connection breaks down?
You don't want that to affect your processes too much. With a desktop application, we can
support slow and unreliable internet connections well.

**Why not build a fancy offline-first web app using local storage then?**

For the future, we plan adding more advanced features like box office sales to this application.
This requires access to a reliable and controlled data storage. Most browsers don't give strong
enough guarantees about retention of local storage data that we don't want to trust on.

License
-------
The code in this repository is published under the terms of the GPLv3 License. 
See the LICENSE file for the complete license text.

This project is maintained by Raphael Michel <mail@raphaelmichel.de>. See the
AUTHORS file for a list of all the awesome folks who contributed to this project.

This project is 100 percent free and open source software. If you are interested in
commercial support, hosting services or supporting this project financially, please 
go to [pretix.eu](https://pretix.eu) or contact Raphael directly.
