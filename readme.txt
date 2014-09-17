DDogleg Numerics is a high performance Java library for non-linear optimization, robust model fitting, polynomial root finding, sorting, and more.  The API is designed to be user to use, without excessive abstraction often found in other libraries.  The user is provided with the capability to have tight control over memory and CPU usage.  Source code is publicly available and has been released under and Apache 2.0 license.

                     http://ddogleg.org

------------ Directory Structure

src/        The source code
test/       Test source code
autocode/   Source code which is used to automatically generate other code
example/    Several example demonstrating how to use the library
lib/        Library dependencies
benchmark/  Internal benchmarks used to evaluate speed and stability    

------------ Building

Gradle is the recommended way to build DDogleg.  The Gradle script can be imported into Eclipse and IntelliJ IDEs

To build compiled jars using Gradle do the following:

--- BEGIN ----
cd ddogleg
gradle createLibraryDirectory
---  END ----

Then look inside the ddogleg/libraries directory created by the script.  It will include jars for this library and anything it depends on.

------------ Repository

The latest source code can be found on Github https://github.com/lessthanoptimal/ddogleg.  If using a commandline you can clone the repository by typing:

git clone git@github.com:lessthanoptimal/ddogleg.git

------------ License

Apache License, Version 2.0
http://www.apache.org/licenses/LICENSE-2.0.html

See LICENSE-2.0.txt

------------ Support

Support is provided through its message board on Google groups.  Please post your question/comment there first before contacting the author.

https://groups.google.com/forum/?fromgroups#!forum/ddogleg

------------ Author

Developed by Peter Abeles


