===============================================
 Writing new Device classes in Jython and Java 
===============================================


Introduction
============

New devices can be written using core classes in GDA. These can be
written in either Jython or Java.


For both Jython and Java:

 1. Define new devices in code
 2. Load onto server (object server) 


For Jython:

 1. Define classes in Jython scripts that extend ScannableMotionBase
 2. Load them into the object server by importing the Jython module,
    and make instances of the Jython-defined devices

For Java:

 1. Write new devices in Java implementing different device
    interfaces. Here we illustrate by writing new Scannable devices
 2. Import instances of the classes defined in Spring beans
    configuration files


To illustrate the process of developing new devices in Java, and
incorporating them into GDA, we describe the process of developing
several new devices that implement the Scannable interface. These
devices are then included in the system by editing configuration files
which are read by the server at startup. The devices can then be
scanned and manipulated in GDA from the Jython terminal.


Developing software for new devices for GDA is a likely requirement at
each site using GDA, to accommodate specific beamline components into
the GDA software framework.


Users should first read "Chapter 5: Scanning" in the GDA Users manual
for an introduction to the basic data acquisition techniques used in
GDA. Below, we describe developing new classes which implement the
Scannable interface. This will likely be required development at each
site using GDA in order to accommodate specific beamline components
into the GDA software framework.


The Scannable interface and ScannableBase classes
=================================================

All Scannable classes implement the Scannable interface. A core base
class implementing the Scannable interface is available in GDA as the
class gda.devive.scannable.ScannableBase. New user-defined Scannable
implementations should extend ScannableBase. Instances of these will
then be visible in the GDA terminal after issuing the command 'ls
Scannable'.


The most important methods for a Scannable to implement are: 

 * getPosition() 
 * asynchronousMoveTo()
 * isBusy()

Other fields in the Scannable that must be defined are:

 * name
 * initial position
 * inputNames
 * extraNames
 * outputFormats
 * units


A full description of the parameters available in a Scannable
implementation is available in 'Chapter 5: Scanning' of the GDA Users
Manual.


A test class that has static methods for constructing instances of
several different types of 'dummy' or testable software Scannables is
available in the documentation configuration src directory:
org.myls.gda.device.scannable.ScannableClassGenerator. It has methods:

 * genarateScannableGaussian()
 * generateScannableGaussian(Gaussian)
 * generateScannableSine()
 * generateScannableSine(SineWave)


This generator constructs instances of the two Scannable classes
ScannableGaussian, and ScannableSine. These scannables classes differ
in the value returned by getPosition(). For ScannableGaussian, the
method returns the value of a Gaussian of the specified position,
width and height 1, with additional noise if defined, at the specified
x value:

.. code-block:: java

   @Override
   public Object getPosition() throws DeviceException {

       // we assume the position is a double - it is only for testing
       double x = (Double) super.getPosition();
       double x2 = x - centre;
       double sigma = 0.425 * width // FWHM -> sd 
       double noiseVal = height * (Math.random() * noise;
       double y = Math.exp(-(x2 * x2) / (sigma * sigma)) + noiseVal;
       return new Double[] { x, y };
   }


Description of the Scannable properties and relations between them
==================================================================

(This material is derived from 'Chapter 5: Scanning' in the GDA Users'
manual; it is repeated here for convenience)


It is obligatory to set the values of several fields in the
constructor of all Scannables. These obligatory fields are:

 * name
 * inputNames
 * extraNames
 * outputFormat
 * currentPosition


The fields 'inputNames', 'outputNames', and 'outputFormat' together
define what numbers this Scannable represents, what they are called,
and the format for printing their values out to file or console.


The '''inputNames''' array defines the size of the array that this
Scannable's rawAsynchronousMoveTo expects. Each element of the
inputNames array is a label for that element which is used in file
headers etc. Note that this array can be empty (size 0) if required.


The '''extraNames''' array is used in a similar manner to the
inputNames array, but lists additional elements in the array returned
by the Scannable's rawGetPosition() method, i.e. the array returned by
getRawPosition() may be larger than the array required by
rawAsynchronousMoveTo(). This allows for the possibility that a
Scannable may hold and return more information than it needs in order
to move pr perform whatever operation it does inside its
rawAsynchronousMoveTo() method. This array is normally empty (size 0).


The '''outputFormat''' array lists the formatting strings for the
elements of both the inputNames and extraNames arrays. It is used when
printing the output from the rawGetPosition() method to the console
and logfiles.

.. note::

   It is an absolute requirement that the length of the
   outputFormat array is the sum of the lengths of the inputNames and
   outputNames arrays for the Scannable to work properly.'''


Add a new device to the server
==============================

The new device is added to the server by defining it as a bean in a
Spring beans configuration file. In the distribution, this file is
'server_beans.xml' in the 'xml' directory. This file can be consulted
for the syntax used to define new object instances as beans in the
Spring beans configuration file. The beans defined in this file are
loaded into the object server at server startup, and can be accessed
and manipulated by the GDA client.


Both getter and constructor dependency injection can be used. Each
object on the server must have a 'name' property, which is its unique
identifier in the server object namespace. As an example, we define
several instances of the ScannableGaussian class using different bean
definitions:

 * scannableGaussian0 --- all properties set in the bean definition
 * scannableGaussian1 --- only the properties of the Gaussian are set in
   the bean. Other properties such as input and extra names, and
   output formats are set to defaults in the Java constructor
 * scannableGaussian2 --- the scannable is defined using a constructor
   argument which is a test Gaussian bean defijned in the Spring
   configuration file. This demonstrates constructor dependency
   injection by Spring
 * scannableGaussian3 --- no properties or constructor arguments are
   defined in the bean. The scannable is constructed using the default
   no argument constructor. All necessary properties are set to
   defaults in the Java class.


Similar examples are provided by several instances of the
scannableSine class in the Spring configuration file:

 * scannableSine0 --- the name and properties of the sine are set in the
   bean definition. Default values for other properties, such as input
   and extra names, and output formats, are defined in the Java class.
 * scannableSine1 --- the properties of the sine are assigned to the
   object by a test sine bean defined in the bean configuration file
   ('testSineWave' bean)
 * scannableSine2 --- no properties other than the name are defined in
   the bean definition. All other properties are set in the
   zero-argument constructor in the Java class.


Example: ScannableGaussian with setter injection
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Fields of the ScannableGaussian are set as properties in the Spring
beans configuration file, and default values defined. Atomic fields
are defined with 'name' and 'value' attributes fields; array fields
are defined using the 'list' tag:

.. code-block:: xml

   <bean id='scannableGaussian1' class='org.myls.gda.device.scannable.ScannableGaussian'>
      <property name='name' valuew='simpleScannable1'/>
      <property name='position' value='0.0'/>
      <property name='inputNames'>
         <list>
             <value>x</value>
         </list>
      </property>
      <property name='extraNames'>
         <list>
             <value>y</value>
         </list>
      </property>
      <property name='level' value='3'/> 
      <property name='outputFormat'>
         <list>
             <value>%5.5G</value>
             <value>%5.5G</value>
         </list>
      </property>
      <property name='units'>
         <list>
             <value>mm</value>
             <value>counts</value>
         </list>
      </property>
   </bean>


Now instantiate a ScannableGaussian using a predefined Gaussian
Spring bean. Spring beans definition of a test Gaussian object:

.. code-block:: xml

   <bean id='testGaussian' class='org.myls.gda.device.scannable.Gaussian'>
      <property name='testGaussian' value='testGaussian'/>
      <property name='centre' value='0.0'/>
      <property name='width' value='1.0'/>
      <property name='height' value='1.0'/>
      <property name='noise' value='0.1'/>
   </bean>


This test Gaussian bean can be used to create an instance of a
ScannableGaussian using constructor injection with the test Gaussian
as a constructor argument:

.. code-block:: xml

   <bean id='scannableGaussian2'>
      <property name='name' value='scannableGassian2'/>
      <constructor-arg ref='testGaussian'/>
   </bean>

Exercise
~~~~~~~~

Start with an empty server_beans.xml file, add Scannable components
one by one, and test them in the GDA Jython console (requires server
restart to incorporate the new components).

Examples of other Scannable classes and tests in GDA
====================================================

 * DummyMotor: from core: gda.device.motor.DummyMotor
 * ScannableMotorTest: from core/test: gda.device.scannable.ScannableMotorTest
 * TotalDummyMotor from core (used by test): gda.device.motor.TotalDummyMotor 


Demonstrate use of Scannable in terminal
========================================

The new components are now available to be controlled from the GDA client. 

Scan 1D
~~~~~~~

The example scanabbles can be scanned and manipulated from the Jython terminal in the GDA GUI. 

Scan the example scannable scannableGaussian0 from -2 to 2 in steps of 0.01::

   >>> scan scannableGaussian0 -2.0 2.0 0.1

Change the width of scannableGaussian0 from 1 to 2, and rescan::

   >>> scannableGaussian0.setWidth(2)
   >>> scan scannableGaussian0 -2.0 2.0 0.1


Change the centre of scannableGaussian0 to -1.0 and rescan::

   >>> scannableGaussian0.setCentre(-1)
   >>> scan scannableGaussian0 -2.0 2.0 0.1


Nested scan
~~~~~~~~~~~

Import the demo scannable classes defined in the user`q demonstration
module scannableClasses.py (located in 'documentation/users/scripts',
and viewable from from the JythonEditor view)::

   >>> import scannableClasses
   >>> from scannableClasses import *
   >>> sgw = ScannableGaussianWidth('sgw', scannableGaussian0)
   >>> scan sgw 0.2 2.0 0.2 scannableGaussian0 -1.0 1.0 0.02

This nested scan has an outer scan which sets the width of the
contained scannable Gaussian to different values from 0.2 to 2.0 in
steps of 0.2. The inner scannable is then plotted for each width from
-1.0 to 1.0 in steps of 0.02



