==================
 NeXus Data Files
==================



The DASC group are in the process of implementing the writing of NeXus
files across all beamlines. For MX beamlines this will be as an
archive format.



Overview
========

The structure of NeXus files is extremely flexible, allowing the
storage both of simple data sets, e.g., a single data array and its
axes, and also of highly complex data, e.g., the simulation results of
an entire multi-component instrument. This flexibility is achieved
through a hierarchical structure, with related data items collected
together into groups, making NeXus files easy to navigate, even
without any documentation. NeXus files are self-describing, and should
be easy to understand, at least by those familiar with the
experimental technique.



Using NeXus at Diamond.
=======================



Configuring the GDA to produce NeXus.
-------------------------------------

In order to configure the GDA to produce NeXus files there are a
couple of configuration settings that will need to be set. Firstly, in
the java.properties file ensure that the data writer's format is set
to NexusDataWriter, i.e::

   gda.data.scan.datawriter.dataFormat=NexusDataWriter

Next, we need to make sure that the GDA can find the relevant NeXus
libraries that are required. So for any process that need to access
NeXus files (this will normally only be the Command Server) we need to
add the following argument to the startup command::

   -Djava.library.path=$GDA_ROOT/lib/Linux-i386

where `GDA_ROOT` is defined to be wherever the GDA is installed. We
are also assuming that we are running on a 32-bit Linux machine.

Finally, we need to make sure that the `LD_LIBRARY_PATH` also contains
the library path (e.g. `$GDA_ROOT/lib/Linux-i386` ). For the case of
Microsoft Windows, we just need to make sure that the library
directory (e.g. `%GDA_ROOT%/lib/win32` )is in the system path.

.. IMPORTANT::

   If you use the standard GDA startup scripts then these modifications
   are already in place and all you will need to set is the java
   property.



relevant Java Properties
========================

Below is a summary of the java properties that can be defined in order
to configure some aspects of the DataWriter's behaviour.

.. table:: Nexus Java properties

   ========================  =======  ===========  ================================================
   Java Property name        default  possible     Description
   ========================  =======  ===========  ================================================
   gda.nexus.backend         HDF5     HDF5, XML    Which on disk format to use.
   gda.nexus.beamlinePrefix  true     true, false  Whether to use the "iXX-" prefix for data files.
   gda.nexus.createSRS       true     true, false  Whether to create an ASCII data file as well.
   ========================  =======  ===========  ================================================


Useful Java Interfaces
======================

If a scannable implements the ``INeXusInfoWriteable`` interface then it
will have the ability to write additional information into the NeXus
file within that scannables NeXus group which will usually be either a
NXpositioner (scannables) or a NXdetector (detectors).

As an example here is the code from the ``ScannableMotionBase`` device:

.. code-block:: java

   public void writeNeXusInformation(GdaNexusFile file) {                      
       try {
	   NeXusUtils.writeNexusDoubleArray(file, "soft_limit_min", getUpperGdaLimits());
	   NeXusUtils.writeNexusDoubleArray(file, "soft_limit_max", getLowerGdaLimits());
	   NeXusUtils.writeNexusDoubleArray(file, "tolerance", getTolerance());
	   NeXusUtils.writeNexusInteger(file, "gda_level", getLevel());  // Non-official field
       } catch (NexusException e) {
	   logger.debug("ScannableMotionBase: Problem writing additional info to NeXus file.");
       }
   }
    					


Also, here is an example from ``CounterTimerBase``:

.. code-block:: java

   public void writeNeXusInformation(GdaNexusFile file) {                      
       try {
	   NeXusUtils.writeNexusString(file, "description", getDescription());
	   NeXusUtils.writeNexusString(file, "type", getDetectorType());
	   NeXusUtils.writeNexusString(file, "id", getDetectorID());
       } catch (NexusException e) {
	   logger.debug("CounterTimerBase: Problem writing additional info to NeXus file.");
       } catch (DeviceException e) {
	   logger.debug("CounterTimerBase: Problem writing additional info to NeXus file.");
       }
   }



You can see by implementing this interface, you are able to write also
any information you want into the NeXus file.



Using NeXus Outside a Scan.
===========================

At the moment, the NeXus writing is mainly tied into the scanning
mechanism. This is were most of the testing has taken place. It is
possible to use the NexusDataWriter within a Jython script but this is
considered 'experimental' at the moment.




Capturing Additional Information
================================

Metadata
--------

There are a number of items that will be captured by default on all
beamlines. These are mainly items relating to the machine (source).
All the metadata is read out using the GdaMetadata Class.

Basically, in order to record a particular value then you need to
define a MetadataEntry with a specific name inside your server xml
file. These can use any of the supported sources to actually get the
data, e.g. scannable, EPICS pv, java property, ICAT value, etc...

At the moment the values are only captured at the start of a scan,
this will be extended to allow for capture at various 'trigger'
points, such as:

+ Start of a scan.
+ End of a scan.
+ At both the start and end of a scan.
+ At every scan point.
+ At a specified time period (long term development)


Metadata items to NeXus Class Mapping
-------------------------------------

This section lists the mapping between various items within the NeXus
classes and what metadata item needs to be called in order to populate
them.

.. table:: NXsource
    
   =============  ===========================  ========  ==========================================================
   Field Name     Metadata Name                Datatype  Description
   =============  ===========================  ========  ==========================================================
   name           facility.name                String    Name
   type           instrument.source.type       String    Facility type (e.g. "Synchrotron X-ray Source")
   probe          instrument.source.probe      String    Radiation type (e.g. x-ray, IR, etc...)
   mode           source.fillMode	       String    synchrotron operating mode
   facility_mode  facility.mode		       String    Facility running mode (e.g. "User", "Machine Day", etc...)
   notes          instrument.source.notes      String    MCR messages
   frequency      instrument.source.frequency  Double    Synchrotron Frequency in Hz
   voltage        instrument.source.energy     Double    Synchrotron Energy in GeV
   power          instrument.source.power      Double    Power
   current        instrument.source.current    Double    Current
   top_up         instrument.source.top_up     Boolean   Is the synchrotron in top_up mode ? 
   =============  ===========================  ========  ==========================================================



.. table:: NXmonochromator
   
   ==========  ===================================  ========  ===========   
   Field name  Metadata Name                        Datatype  Description
   ==========  ===================================  ========  ===========  
   name        instrument.monochromator.name        String    Name
   wavelength  instrument.monochromator.wavelength  Double    wavelength
   energy      instrument.monochromator.energy      Double    energy
   ==========  ===================================  ========  ===========   



.. table:: NXbending_magnet

   ===============  =========================================  ========  =============== 
   Field name       Metadata Name                              Datatype  Description
   ===============  =========================================  ========  =============== 
   name             instrument.bending_magnet.name             String    Name
   bending_radius   instrument.bending_magnet.bending_radius   Double    Bending Radius
   spectrum         instrument.bending_magnet.spectrum         NXdata    Spectrum
   critical_energy  instrument.bending_magnet.critical_energy  Double    Critical Energy
   ===============  =========================================  ========  =============== 



.. table:: NXinsertion_device

   ==========  =====================================  ========  ================================
   Field name  Metadata name                          Datatype  Description
   ==========  =====================================  ========  ================================
   name        instrument.insertion_device.name       String    Name
   type        instrument.insertion_device.type       String    "undulator" or "wiggler"
   gap         instrument.insertion_device.gap        Double    Gap
   taper       instrument.insertion_device.taper      Double    Taper
   phase       instrument.insertion_device.phase      Double    Phase
   poles       instrument.insertion_device.poles      Integer   Number of Poles
   length      instrument.insertion_device.length     Double    Length of Device
   power       instrument.insertion_device.power      Double    Total Power delivered by device.
   energy      instrument.insertion_device.energy     Double    Energy of Peak
   bandwidth   instrument.insertion_device.bandwidth  Double    Bandwidth of Peak Energy
   spectrum    instrument.insertion_device.spectrum   NXdata    Spectrum of insertion device.
   harmonic    instrument.insertion_device.harmonic   Integer   Harmonic of Peak.
   ==========  =====================================  ========  ================================

Writing arbitrary metadata to Nexus files
-----------------------------------------
There are cases where it's useful to include arbitrary notes or fields in data
files. To enable this, metadata fields can be specified along with a path for
where in the file it should be written.

For instance to include a sample name field in the sample NXsample node, include
a bean similar to the following in the Spring GDA configuration:

.. code-block:: xml

    <bean class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
        <property name="staticMethod" value="gda.data.scan.datawriter.NexusDataWriter.setMetadata"/>
        <property name="arguments">
            <map>
                <entry key="sample_name" value="sample:NXsample/name"/>
            </map>
        </property>
    </bean>

This will include a name field in the sample group with the metadata value (read
at the start of the scan) for the entry with the name `sample_name`.
(NB metadata name does not have to match the entry in the nexus file)

The metadata to be included can be changed at run time via the Jython console:

.. code-block:: python
    :name: adding metadata

    >>> from gda.data.scan.datawriter import NexusDataWriter
    >>> NexusDataWriter.updateMetadata({'xyz': 'sample:NXsample/abc'})
    >>> # Scans from here will include xyz metadata

.. code-block:: python
    :name: removing metadata

    >>> NexusDataWriter.removeMetadata(['xyz'])
    >>> # xyz will no longer be written to scan files

GDA Helper functions
====================

With the `gda.data.nexus.NeXusUtils` class there are a number of
static helper functions which make writing to a NeXus file a but less
painful. Below there is a list of the most useful (but not all)
functions.

.. function:: openNeXusFile

   Opens a NeXus file (Read-Write) and returns the file handlen.

.. function:: openNeXusFileReadOnly

   Opens a NeXus file (Read-Only) and returns the file handle.

.. function:: writeNexusDouble
   
   Writes an double into a field with a specified name at the current file position.

.. function:: writeNexusDoubleArray

   Writes an array of doubles into a field with a specified name at the current file position.

.. function:: writeNexusInteger

   Writes an integer into a field with a specified name at the current file position.

.. function:: writeNexusIntegerArray

   Writes an array of integers into a field with a specified name at the current file position.

.. function:: writeNexusLong

   Writes a long into a field with a specified name at the current file position.

.. function:: writeNexusLongArray

   Writes an array of longs into a field with a specified name at the current file position.

.. function:: writeNexusString

   Writes a String into a field with a specified name at the current file position.

.. function:: writeNexusBoolean

   Writes a boolean into a field with a specified name at the current file position. 