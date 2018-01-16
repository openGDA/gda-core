==========
 Scanning
==========

This section discusses scanning which is the main method of collecting
data in the GDA.

The definition of a scan
========================

In the GDA there are two types of scan available: step-based scanning
and continuous scanning. Continuous, or fast, scanning is when
detectors are collecting data throughout the time that other hardware
such as motors are operated. Such scanning can be very convenient
because the scan can take less time. However continuous scans offer
less flexibility to users as wiring is required between specific
motors involved in the scan and the detector electronics.

Steps scans require no such specific wiring. They have a distinct
separation between movement of hardware and then detector collection
and readout. Such a scan has two lists of objects inside it: Detectors
and Scannables. At each node in a step scan, the Scannables are
operated (these operations are grouped by the level attribute which
each Scannable has. So from the lowest level upwards all Scannables at
a particular level are operated concurrently and once they have
finished their movement the next level is operated and so on.). Once
the Scannables have been operated then data is collected from all
Detectors concurrently and once they have all completed then the data
from that round of collection plus the new locations of all Scannables
are recorded to file.

Scannables are a generic type of object representing anything which is
not a detector which needs to be operated between each round of
collecting data. They could be as simple as a motor, but could be a
calculation based on several motor positions, or a Scannable could
even represent a script which is run at every point in the scan.

Scannables can be defined as representing a number, or array of
numbers. Their operations analogous to motors i.e. they have methods
to 'move' them, a method to requested their status to see if they are
moving and a method to get their current position. They are the main
type of object that users will interact with in the GDA

The structure of a step scan
----------------------------

To explain in detail how a scan works, below is listed the structure
of a step scan. An explanation of the parts of the scan is explained
in the rest of this section. This structure should be helpful for
those writing their own Scannable classes and need to understand scans
in detail.

Prepare for the scan:

1. call the atScanStart method on all Scannables which are part of the
   scan
2. if the flag has been set to return all Scannables to their original
   positions after the scan, record all Scannables current positions
3. create a new data file

Iterate through all dimensions of the scan, at each level:

1. call the atScanLineStart method on all Scannables
2. move the Scannable of that dimension to its next step
3. if at the inner-most dimension, loop through all points in that
   scan. At each point:

   a. call the atPointStart on every Scannable in the scan
   b. move the Scannable of that dimension and every other Scannable that
      is not a separate dimension in the scan (if the parameters from the
      command-line said that the Scannable should be operated). All the
      Scannables to be operated at this point are operated in groups defined
      by their level attribute. Once all Scannables in a level have finished
      moving (this is known by polling their isBusy method), then the
      Scannables from the next level are operated
   c. collect data from all Detectors which are part of the scan
   d. record the data and Scannable positions in the data file. The data
      is also broadcast to the GDA client for visualisation when required.
      This is the only point in the loop that the getPosition method of
      Scannables is called
   e. call the atPointEnd on every Scannable in the scan

4. call the atScanLineEnd method on every Scannable in the scan

Finish the scan:

1. call the atScanEnd method on every Scannable in the scan
2. close the data file
3. if the flag has been set to return all Scannables to their original
   positions after the scan, then move all Scannables back to position
   noted at the start


Scan commands
=============

The scan commands have the format::

   scan <scannable> <start> <stop> <step> [<scannable> [<start>] [<stop>] [<step>]] ...
        ... [<detector1>] [<detector2>]

Only the first Scannable with its start, stop and step values is
required. Other Scannables are optional and they be operated during
the scan or merely have their position recorded at each step in the
scan. If subsequent Scannable objects have three arguments after their
name then this counts as an extra dimension to the scan. There is no
limit to the number of dimensions which can be included in a scan.

The detectors are also optional in the command: in the GDA Jython
environment there is a list of default Scannables and Detectors which
will be included in every scan without having to include the object in
the scan command.

To explain the various possible options this format gives, here are
some more explicit explanations followed by examples:

Other types of step scans
-------------------------
There are other step scans available which work in a similar manner
but take alternative input parameters. These scans and their commands
are:

TO BE RE-ANOUNCED!


Scan options
============

A scan command is comprised of a list of scannable to be moved or
observed and a list of detectors to be triggered. At each point in the
scan, after the scannable and detectors have completed their tasks,
the positions of the scannable and the data from the detectors is read
and recorded. By default unless scannables are given different
priorities they will move concurrently (see section below on
priorities). There are a number of different types of scan each called
with a different syntax:


+  **One parameter scan**::
   
      >>> scan scannable start stop step [detector [exposure time]]

   The scannable is swept from the start value to the stop value in step
   size steps. At each point, if specified, the detector is triggered. An
   optional exposure time can be given for a detector.

+  **Concurrently moved parameters scan**::

       >>> scan scannable start stop step scannable2 start step (detector)

   A second Scannable is varied concurrently with the first. This second
   device does not need a stop value, as the number of points visited is
   determined by the values for the first device.

+  **Multidimensional scan**::

      >>> scan scannable start stop step scannable2 start stop step (detector)

   This example is a two dimensional scan in which an entire scan over
   scannable2 is performed after each move of the first Scannable.

+  **Move-to-keep-still scan**::

      >>> scan scannable start stop step scannable2 start (detector)

   In this example scannable2 is moved to the start position and then
   this position is maintained as scannable is scanned. An example below
   shows why this may be useful.

   At each point in the scan along with the data from the detector, a
   line is stored in the data file recording the value of every Scannable
   listed in the command. This means Scannables can be used like
   detectors to record values. For example.

+  **Monitoring a Scannable**::

      >>> scan scannable start stop step scannable2 scannable3 (detector)

   For each point in the scan the value of the 2nd and 3rd Scannables
   (along with the first of course) are recorded.


There is no limit to the number of Scannables or detectors which can
be included in any scan. The syntax of these commands can be mixed to
build up complex scans.



Example scan commands
=====================

Following are some useful example scans used on beamlines at Diamond.

Energy scan. The scan command might be used to sweep the energy, or
wavelength, of photons illuminating a sample. For example:

   >>> scan pgmenergy 500 2000 0.1 uv 2
    				
This scans the x-ray energy from 500eV to 2000eV in 0.1eV step. At
each scan point, an image is taken from a camera called uv. The camera
exposure time is set to 2 second. This command is the same::

   >>> scan pgmenergy 500 2000 0.1 ca43s 0.5

except at each point a drain current is measured by reading from a
scalar card channel ca43s. The scaler card counting time is set to 0.5
second. (These commands are from I06 and require pgmenergy, uv and
ca43s to be defined on your beamline.)

Time scan. The scan command may be used to perform a scan with respect
to time::

   >>> scan x 1 100 1 ct4 1 detector

This scans over a dummy Scannable x that acts simply as a counter. For
each step of x, a timer scannable ct4 waits for one second and then
the detector is triggered. This is a useful scan for tracking the
stability of a measurement or to see what happens if another device is
moved. (This is from I16. To use this command the Scannable for x and
ct4 must be defined in your beamline.)

Move-to-keep-still scan. The scan command may sweep some degrees of
freedom, while keeping a second constant, even while the second is
influenced by the first. For example::

   >>> scan en 7.0 7.1 0.001 hkl 100 detector
    				
This sweeps a scannable en that controls the energy or wavelength
selected by a monochromator. As the wavelength is varied, the angle
that a scattered ray of interest leaving the sample varies. To detect
this ray the sample must be rotated as the wavelength varies to keep
the ray directed at a fixed detector. The scannable hkl 100 orients
the sample so that the particular ray described by the Miller indices
(1,0,0) is detected. As this orientation is a function of wavelength
the hkl scannable will cause the sample to rotate appropriately as the
wavelength is swept.



Default devices and detectors
=============================

There may be some Scannables or detectors which you might want to
operate in every scan. To avoid having to type the object's name in
every scan, there is a list of "defaults" which are included in every
scan:

**list_defaults**
   returns the list of default objects operated in every scan

**add_default <object name>**
   add the given object to the list of defaults
**remove_default <object name>**
  remove the given object to the list of defaults.

You may find that some detectors are added to the list of defaults
automatically when the GDA is started.


Configuring Scannable movement priority (levels)
================================================

Normally Scannables are moved between points concurrently; that is
they are all started moving at the same time and once (one by one)
they have all finished moving the data is read and recorded. However
it is also possible to explicitly define numerical priorities to
Scannables. Devices are moved to the next scan point in sequence with
the highest priority (lowest number) devices moving first. Starting
with highest priority, all devices with that priority are moved
concurrently. Once these have completed their move all the devices at
the next highest priority are moved and so on. Once this sequence is
complete and all the devices have reached final positions the values
of all the detectors and positions of all the devices are sampled and
recorded.

The default level for a Scannable is 5. This can be read or modified
using the level command.

To return the level of the Scannable (higher priority = lower number)::

   >>> level <Scannable>

and to set the level of the Scannable to a given value::

   >>> level <Scannable> <integer>
    						
For example to move x and y where y's actual position depends on the
position of x, the priority of x should be set higher than that of y.
y will now not be moved until x has completed its move; if y reads the
value of x to use in a calculation it will use the new value of x.

As another example a wait device might simply wait n seconds if asked
to move to n. To use this to add a settling delay after all devices
have moved assign it a low priority. Once the rest of the devices have
moved the wait device will then 'move' for n seconds before the
positions of all devices are read and the detectors triggered.

