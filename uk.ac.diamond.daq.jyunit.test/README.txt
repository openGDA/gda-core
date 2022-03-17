JyUnit tests

This project provides a test running framework for JyUnit tests.

JyUnit tests are unit tests for Jython using the Python unittest module.

They live within a script project, for example:

uk.ac.gda.core
|
...
├── scripts
│   ├── all_tests.py
│   ├── beamlineParameters.py
│   ├── extendedSyntax
│   ├── finder.py
│   ├── gda_completer.py
│   ├── gdadevscripts
│   ├── gdascripts
│   ├── __init__.py
│   ├── jythonNamespaceMapping.py
│   ├── loghandling.py
│   ├── magicmodule.py
│   ├── mock.py
│   ├── testhelpers
│   └── testjy
...

Individually the tests may be run within Eclipse from their source directly.
To do this, choose a test and Run As a Jython unit-test.
This may fail if the dependencies are not expressed in the PyDev project configuration.
To modify this, right click on the project and go to Properties, PyDev - PYTHONPATH
and then add the dependency in the External libraries section. Also, can add dependent projects as project
references if those projects have Python nature (and their PyDev PYTHONPATH set in the Source Folders
tab to export the class files).

The purpose of this project is to provide a test running framework which can be used as part
of the Tycho build. An instance of a GDAJythonInterpreter is made and the tests executed within it.
It is executed within an Equinox runtime.

A single test class is created for each project containing JyUnit tests to be executed, these extend
the abstract class JyUnitTestRunner. Two methods must be implemented to provide a set of script
project paths and a test entry point script. The results are written in JUnit XML format to
<this project root>/target/surefire-reports. To the build/test framework, this will appear
as a single test but the CI report aggregation will read the report file written by Python.

These wrapper classes may also be run from Eclipse by running them as a Plugin test. Note that
this environment does differ slightly to that created by Tycho. Specifically the Plugin tests
in the IDE include all workspace bundles in the runtime, instead of just the project's
dependencies.

The reasoning for this approach is that it allows the tests to be run with limited additional build
configuration. Previously all tests were executed via Ant for GDA and Dawn but we have been keen
to avoid reintroducing this to the current build system.

Other options were considered such as locating each project's wrapper class within a corresponding
test plugin however this can be problematic due to plugin dependencies.
