pretixSCAN
==========

.. image:: https://ci.appveyor.com/api/projects/status/n3n5tp3wl3i2qg5p?svg=true
   :target: https://ci.appveyor.com/project/raphaelm/pretixdesk

Checking in your attendees, one ticket at a time.

What is this?
-------------

This is a cross-platform desktop application that handles attendee check-in for events managed
with `pretix`_, the open source ticket sales system of choice.

Project status
--------------

pretixSCAN has all features required to be useful but is still in it's early days. Feel free to
use it, but do have a backup plan.

Building and Running
--------------------

This project contains submodules, so the first command you execute should be::

    git submodule update --init

This project is built using Gradle. It requires a JDK in version 11.
You can then build and run the project using::

    cd pretixscan
    JAVA_HOME=/usr/lib/jvm/java-11-jdk ./gradlew :gui:run

To create packages, see PACKAGING.md.

FAQ
---

**Why Java?**

It's not Java, it's `Kotlin`_.

**Why Kotlin? You are a Python shop, after all!**

Yes, and we still love Python. However, all cross-platform GUI frameworks suck in some way,
and their Python bindings are rarely Pythonic or nice to use. Additionally, it is really hard
to create good standalone application packages for Windows users for Python. JavaFX isn't perfect
as well, but `tornadofx`_ has a nice Kotlin abstraction and
`JFoenix`_ has nice material-design elements.

Additionally, with the choice of a JVM-based language we can reuse lots of code from our
`pretixscan`_ Android application, making it easier to
bring new features to both platforms.

**Why not just a web app?**

Ever organized an event and right before the start your internet connection breaks down?
You don't want that to affect your processes too much. With a desktop application, we can
support slow and unreliable internet connections well.

**Why not build a fancy offline-first web app using local storage then?**

For the future, we plan adding more advanced features like box office sales to this application.
This requires access to a reliable and controlled data storage. Most browsers don't give strong
enough guarantees about retention of local storage data that we don't want to trust on.

Contributing
------------

If you like to contribute to this project, you are very welcome to do so. If you have any
questions in the process, please do not hesitate to ask us.

Please note that we have a `Code of Conduct`_
in place that applies to all project contributions, including issues, pull requests, etc.

License
-------
The code in this repository is published under the terms of the Apache License. 
See the LICENSE file for the complete license text.

This project is maintained by Raphael Michel <mail@raphaelmichel.de>. See the
AUTHORS file for a list of all the awesome folks who contributed to this project.

This project is 100 percent free and open source software. If you are interested in
commercial support, hosting services or supporting this project financially, please 
go to `pretix.eu`_ or contact Raphael directly.

.. _pretix: https://pretix.eu
.. _pretix.eu: https://pretix.eu
.. _Code of Conduct: https://docs.pretix.eu/en/latest/development/contribution/codeofconduct.html
.. _Nullsoft Install System: http://nsis.sourceforge.net/Download
.. _Kotlin: https://kotlinlang.org/
.. _tornadofx: https://github.com/edvin/tornadofx
.. _JFoenix: https://github.com/jfoenixadmin/JFoenix
.. _pretixscan: https://github.com/pretix/pretixscan-android
