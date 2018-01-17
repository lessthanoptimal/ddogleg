Documentation
=====================


DDogleg Numerics is library for Java. Usage documentation is primarily provided in the form of examples and JavaDoc. To use DDogleg simply add its jars to your class path like you would with any other library. Ant and Maven scripts are provided for compiling the source code, alternatively you can use the command line or your favorite IDE.


JavaDoc
-----------

Full documentation of the API is contained in the `JavaDoc <http://ddogleg.org/javadoc>`_

Example Code
------------

Several examples are included with the source code and can be found in the "ddogleg/example/src" directory. For your convenience several examples have been added to this website, see links below. To view the latest examples, browse the GIT repository.

List of Example Code:

* :doc:`/Clustering`
* :doc:`/Combinatorics`
* :doc:`/Optimization`
* :doc:`/RobustModelFitting`
* :doc:`/PolynomialArithmetic`
* :doc:`/PolynomialRootFinding`
* :doc:`/NearestNeighbor`


Latest example code at Github:

https://github.com/lessthanoptimal/ddogleg/tree/v0.13/examples/src/org/ddogleg/example

Building the Library
--------------------

DDogleg is easy to compile and build its Jar files. Ant and Gradle scripts are provided, see below. It is also easy to create a new project and build it using your favorite IDE, e.g. IntelliJ and Eclipse.

Gradle::

  $ cd ddogleg/
  $ ./gradlew createLibraryDirectory
  :compileJava
  :processResources UP-TO-DATE
  :classes
  :jar
  :sourcesJar UP-TO-DATE
  :createLibraryDirectory

  BUILD SUCCESSFUL

  Total time: 7.238 secs

Then look in the "ddogleg/libraries" directory for compiled jar and source
