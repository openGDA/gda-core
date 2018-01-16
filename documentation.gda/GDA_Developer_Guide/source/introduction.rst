=========================
 Introduction to the GDA
=========================


Aims of the framework
---------------------

The Generic Data Acquisition framework (GDA) is an open source project which 
provides a configurable platform for running experiments on synchrotron and 
neutron beamlines. It is intended that the software provides visiting 
scientists with a science focused, rather than hardware-centric environment
to perform experiments.

There is a graphical interface (GUI) with an embedded command-line interface 
(CLI). The GUI contains tools for data visualisation and analysis.

Hardware may be operated directly from the GDA, or via control systems such as
EPICS or TANGO.

The software is designed to be configurable and extensible. This makes the
software flexible to the requirements of different beamlines and facilities, 
and maximises code sharing.


Design overview
---------------

Java is the principal language used in the GDA code. Jython is the scripting 
interpreter embedded in the GDA server.

The GDA has a client-server design. The server provides connectivity to the 
beamline control systems and hardware. The server distributes objects 
representing the hardware via a CORBA server. These objects are configured using
Spring_ XML files. 

The server also holds the Jython interpreter within which the data collection 
scans and scripts are run. This is known as the Command Server. The Command
Server also provides central services for the GDA on that beamline such as 
role-based access control.

The client is an Eclipse RCP product which communicates with the server via
the CORBA (for both remote procedure calls and events). It performs data
visualisation of live data, and browse old data files. 

The code base is split into Eclipse plugins. GUI code, facility-specific and
technique-specific code are separated from a generic 'core' set of plugins.
The same code is used on both server and client. However the server runs as a
regular (non-OSGi) Java process whereas the client is an Eclipse RCP (OSGi)
product.

Data is principally written to Nexus_ format files, but a GDA installation may
be configured to write to any format.

.. _Nexus: http://www.nexusformat.org/
.. _Spring: http://www.spingsource.org/


Major design concepts
---------------------


Scans
   The principal context in which data is collected. The generic step scan is
   run from a single command in the Jython environment. The Scan operates a
   series of Scannables and broadcasts its data after each point to clients
   for display and to file writers.
   There are several variants of the main step scan. It is possible to write
   other types of scan classes which fulfil the Scan interface. Alternatively
   scripts could be written which simply operate Scannables and Detectors
   and create ScanDataPoints within the scripts.

ScanDataPoint
   A Serializable object which holds the data from a single point in a scan,
   as well as metadata such as the number of that point in the scan. These 
   objects are passed to Clients for display in graphs and to data writers to 
   be recorded in files.

Devices
   Interface for an object representing a piece of hardware. Device is a super
   interface to Scannable. Device has a large number of sub interfaces for 
   different types of hardware. By operating types of devices via Device
   interfaces rather than having stand alone objects, the underlying control 
   systems used by GDA are abstracted. This makes the higher levels of the GDA
   agnostic to control system or brands of hardware types. 

Scannables
   Interface for objects which wish to participate in step scans. Most objects
   available to beamline users are Scannables. A Scannable is a high-level
   object representing a number or an array of numbers. These numbers could be
   as simple as motor positions, a value held in a file or could be the results
   of a calculation. 

Detectors
   Sub-interface of Scannable. Detectors are distinguished from Scannables as
   during a step scan, each node (point) in the scan has two phases: a movement 
   phase in which non-Detector Scannables are operated and then a data 
   collection phase in which Detectors are operated. 

Object Server
   The name of the GDA server-side process. This contains a list of objects (mainly
   Devices) which are defined in Spring XML files. The objects are distributed via
   CORBA or RMI for access by the GDA client (or potentially other Object Servers).
   One of the server-side objects is the Command Server which contains the GDA
   Jython interpreter for sequencing of operations. There is a role-based access
   control layer added to Object Servers to restrict certain operations of Devices.

Finder
   Singleton class providing a service to locate objects (both local or 
   distributed via CORBA) by name.

Command Server
   The GDA's Jython environment and related services. These services include
   role-based access control, script and scan status, broadcast of scan data
   points.

InterfaceProvider
   Singleton class providing resources to services provided by the Command 
   Server.




