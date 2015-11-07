Download 
========

Both the source code and precompiled jar files are available.  The latest stable code can be downloaded from Sourceforge while the bleeding edge can be checkout from Github. A list of dependencies and build instructions are also provided below.



Stable Release
--------------

The latest stable release is available on Source Forge, with direct links provided below for your convenience.

https://sourceforge.net/projects/ddogleg/

* `Source Code <https://sourceforge.net/projects/ddogleg/files/v0.8/ddogleg-v0.8-src.zip/download/>`_
* `Precompiled Jar <https://sourceforge.net/projects/ddogleg/files/v0.8/ddogleg-v0.8-libs.zip/download/>`_

Bleeding Edge
-------------

The latest source code is available from GitHub.  Most of the time it should compile without any problems.  If you get a compiler error and its missing a function/class in EJML you will need to checkout the latest source code from that project too.  Not familiar with GIT?  Learn about it here http://git-scm.com/

Git repository: https://github.com/lessthanoptimal/ddogleg

In Linux you can check out the code from git using the following commands:

::

  git clone git://github.com/lessthanoptimal/ddogleg.git ddogleg

Maven and Gradle
------------------------

To include the latest stable release in your Maven projects add the following dependency. Be sure to set VERSION to whatever the latest stable version is.

**Maven:**
::
    <dependency>
        <groupId>org.ddogleg</groupId>
        <artifactId>ddogleg</artifactId>
        <version>0.8</version>
    </dependency>
 
**Gradle:**
::
    compile group: 'org.ddogleg', name: 'ddogleg', version: '0.8'


Dependencies
------------

DDogleg depends on `EJML <http://ejml.org>`_ for matrix operations and also uses `JUnit <http://junit.org>`_ for unit testing.
