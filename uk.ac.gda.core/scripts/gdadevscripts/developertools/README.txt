Checking XML item names
=======================

Checks deviceNames, motorNames, etc. in motor systems to determine whether
there are missing links, either forward or backward. This helps to decrease the
number of null pointer exceptions caused when starting up GDA.

Note, the problems caught by this script are not schema errors, so will not be
caught by Castor!

Usage: checkNamesGDA.py server_epics.xml phase2interface.xml

Generating files from new style Epics interface files
=====================================================

The following script will generate EpicsMonitors from simplePvs and generate
the whole chain of EpicsMotors, OEs including Positioners and DOFs from
simpleMotors. Assumes linear items for now.

Usage: GDA_generator.py phase2interface.xml output.xml

Generating a text file containing Epics items paired up with their GDA names
============================================================================

This script is very useful for figuring out what items have been left out of
the current server_epics.xml, inadvertently or not. Reading the output into a
spreadsheet will also allow useful displays to be made, such as a Epics PV/GDA
item name spreadsheet that I have made for I24. Only works for newer interface
files.

Usage: matchEpicsGda.py phase2interface.xml gda_server.xml

Using the CSV generated from matchEpicsGda to make a testing script
===================================================================

The items in the matched EPICS to GDA tab-delimited text file can be used to
generate a basic testing script. Based on the integrationTest.py scripts used
on phase I MX beamlines.

Usage: generateTester2.py spreadsheet.txt tester.py

Creates a tester.py script containing a set of tests that can be run to do
basic checking of items on the beamline.
