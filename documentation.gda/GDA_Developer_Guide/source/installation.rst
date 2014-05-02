=================
 GDA installation
=================


Introduction
============

This section will outline the GDA installation and the various ways
in which the GDA is configured.

Installation Folder Structure
=============================

Following the Quick Start guide and using the example downloadable from 
the opengda website, the directory structure will have the folders:

builder
  Contains scripts to compile the source and to build the client and 
  the CORBA java code from the IDL source.
 
documentation
  Contains the documentation source. Compile this by running ``make all``. 
  The compiled documentation in various forms will be placed in a sub-folder
  called build.

example-config
  Contains the example configuration. Configuartion folder have a standard 
  structure which will be explained below.
  
features
  Standard Eclipse Java IDE workspace directory. Contains the *features* projects.

licenses
  Licensing information of the third-party software used in the GDA.
  
plugins
  Standard Eclipse Java IDE workspace directory. Contains the *plugins* projects.

thirdparty
  Contains some third-party software required to compile the GDA. This software is 
  referenced by the GDA *build target* which should be set when compiling the source
  code from an Eclipse IDE. This is referenced automatically by the build scripts.
  
For new developers, the folder of most interest will probably be the example-config
where the beamline-specific configuration will need be defined.

Adding new or existing plugins
------------------------------

When creating a new plugin, the plugin project must be located in the plugins directory or
the build and startup scripts will not work properly. 

Existing plugins can be imported into the workspace from the Eclipse IDE by running the 
import wizard: File->Import...->General->Existing Projects. Ensure that the *Copy Into
Workspace* checkbox is selected. Once the project has been imported, if it has not been
placed into the plugins folder, perform a Refactor->Move... to relocate the new plugin project.

Configuration Folder Structure
==============================

You are recoommended to use the :ref:`new-style-configuration-directory-structure` described below.

The example-config follows the standard folder structure. This structure needs to be adhered 
to as the locations of the sub-folders in the directory are assumed from the java property ``gda.config``.

The contents of the configuration directory is:

css
   Configuration files for the CSS synoptic perspective. (RCP client only)
   
lookupTables
   Location of lookup tables for the Scannable objects which use them.
   
properties
   Java, Jacorb and JCA (EPICS API) properties files. The Jacorb file has to be placed in a sub-folder
   named 'etc'.
   
scripts
   Location of beamline-specific Jython scripts
   
users
   For the example-config, contains log files, user scripts and is where data will be written to. It is 
   likely that these files will want to be placed in other locations. Use Java properties to define the
   directories where these types of files are to be located. See the later section for more information
   about these Java properties.
   
var
   For files which are changed whilst the GDA is running.
   
xml
   The XML files which define the contents of the ObjectServer processes.
   
It is likely that for new developers, when starting out with the GDA, the items of most interest will be 
the java.properties file to customise folder locations etc. and the ObjectServer xml files to customise the
objects representing hardware.

File permissions
----------------

The var and users directory are likely to require broader permissions from the rest of the GDA installation
to allow processes run by any user to edit the files in those directories.

It is recommended in a production environment to use a var directory outside of the GDA installation as
some of the files kept there may need to be used consistent across different versions of the GDA. This
make upgrading to new GDA versions easier.

.. _new-style-configuration-directory-structure:

New style configuration directory structure
-------------------------------------------

Starting with version 8.10, GDA is moving to a new, more explicit configuration directory structure to 
support easy switching between different running modes, server and client profiles. 
A brief descriptions of this new configuration layout can be found :ref:`here <new-configuration-layout-section>`.

Below is an example of this new configuration layout extended to include configuration for logging, epics, authorisation, etc:   

.. literalinclude:: configdirsessential.txt

**clients**
	Location for Spring configurations of client objects, which should have two profiles: ``main`` and ``rcp``. Each profile should in turn contain two modes: ``dummy`` and ``live``. Each mode must contain ``client.xml`` file
	
**epics**
	Location for GDA-EPICS integration configuration if required (optional), which should contain two modes: ``dummy`` and ``live``. Each of these should contain EPICS integration specification and ``epics.properties``.
	
**jacorb**
	Loaction for CORBA configuration. It must contain two modes: ``dummy`` and ``live``, which must has ``etc`` directory containing ``jacorb.properties``.
	
**jca**
	Location for EPICS Channel Access configuration (only required to communicate to EPICS IOC). It must contain two modes: ``dummy`` and ``live``, which in turn contains the ``JCALibrary.properties`` file.

**logging**
	Location for GDA logging configuration files. It should contain two modes: ``dummy`` and ``live``. Each of these modes has a ``log.properties`` and ``LogServer.xml`` configuration for the Log Server. Object logging and log viewer configurations can be made mode-agnostic.  
	
**lookupTables**
   Location of lookup tables for the Scannable objects which use them.
   
**permission**
	Location for default user authorisation settings for GDA object access control if enabled.
	
**properties**
	``beamline.properties`` - defines beamline specific properties ;
	``gda.properties`` - defines GDA generic properties, some of which reference to properties defined in other properties files;
	``java.properties`` - defines properties for mode-dependent customisation.  
	The ``java.properties`` file must include other properties as demonstrated by the following example:
	
	.. literalinclude:: java.properties
	
	The ``beamline.properties`` must be included at the first line, the ``gda.properties`` must be included at the last line of ``java.properties`` file.
	
**scripts**
	 Location of beamline-specific Jython scripts. It should at least contain the GDA startup script file ``localStation.py``.
	 
**servers**
	Location for Spring configurations of server objects, which should contain at least one ``main`` profile. Each profile should in turn contain two modes: ``dummy`` and ``live``. Each mode must contain ``server.xml`` file.
	The ``server.xml`` imports ``main-common.xml`` in parent folder which defines common objects configurations available for all modes; The ``main-common.xml`` in turn imports ``servers-common.xml`` which defines common objects
	configurations for all profiles.  
	
**var**
	GDA cache data directory, for files which are changed whilst the GDA is running.


Other directories and files that are often used on Diamond beamlines are show below: 

.. literalinclude:: configdirutils.txt

**bin**
	Shell scripts to launch GDA components

**Desktop**
	Linux desktop files for launching GDA components
	
**doc**
	Beamline specific documents
	
**etc** 
	Beamline specific environment for GDA

**launcherInstaller**
	Shell scripts to install GDA launchers in ``Applications`` menu on Linux.
	
**launchers**
	eclipse launchers for GDA components
	
**pytools**
	Beamline specific python scripts for online and off-line data processing.


Starting to develop the GDA
===========================

A few notes for new developers:


1. Once you have a version of the GDA source, and a beamline or 
   example configuration downloaded/checked out you may compile by either
   opening the source in an Eclipse IDE or run the compile script. The
   compile script will place the compiled classes in the same location as
   the Eclipse IDE.
   
2. The GDA server runs as a regular Java process, so uses the compiled classes
   located in the projects in the plugins and features directories.

3. However the client is an RCP 'product' and once compiled runs using its own compiled 
   classes inside the client directory (created when the client is compiled using the
   gda-build script).
   
4. You may run the client from the Eclipse IDE, and this will not use any source code
   or other files from inside the client directory. This would run entirely from compiled code
   in the plugins and features directory.
   
   
Vanilla GDA
-----------
   
For an 'empty' GDA installation to add your own objects, make the following changes to the example-config:

 1. remove all scripts in the scripts directory, except localStation.py
 2. remove all scripts in the users/scripts directory
 3. in xml/client/client.xml, remove all the objects below the comment
 4. in xml/server, remove all files and sub-folders except server.xml
 5. in xml/server/server.xml, remove the imports near the bottom of the file
