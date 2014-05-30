============================================
 GDA Developement Frequently Asked Questions
============================================
 
 This is a list of answers to frequently asked questions or snippets of information that have not yet be added to the main Developer Guide
 
What are the causes for GDA Server hanging during startup
---------------------------------------------------------

* GDA Server will hang if gda.epics.interface.schema points to stopped webserver

The Java property gda.epics.interface.schema gives address of xsd file against which the Epics interface file is validated.If this address is a url to a web server then gda will hang on startup if the server is not running.
It is best not to define this property so that the default is used. However the default points to the epics plugin via the environment variable gda.install.git.loc. So you need to ensure that is set correctly. This is done by the gda launcher and has been added to the dls-config/bin/gdaclient command (gda 8.34) as an additional vmarg


How to solve "java.lang.NoSuchMethodError: org.jscience.JScience.initialize()V2"
--------------------------------------------------------------------------------

When running GDA server as java application within IDE launcher, you may see the following error message:
"Could not instantiate bean class [gda.device.scannable.ScannableMotor]: Constructor threw exception; nested exception is java.lang.NoSuchMethodError: org.jscience.JScience.initialize()V"
To solve it, you need to make sure the "uk.ac.diamond.org.jscience_2.0.2.jar" comes before "uk.ac.gda.core" plugin in the java classpath.


How to solve "Missing required bundle uk.ac.diamond.org.jscience_[2.0.2,3.0.0)"
-------------------------------------------------------------------------------
openGDA core and epics plugins depends on JSCience 2.0.2 and Scisoft depends on 4.3. When launcher GDA RCP client from IDE you must select all plugins in the Target Platform to resolve this issue at the moment. Otherwise the launcher can only resolve to the newer version of JScience4.3.openGDA core and epics plugins depends on JSCience 2.0.2 and Scisoft depends on 4.3. When launcher GDA RCP client from IDE you must select all plugins in the Target Platform to resolve this issue at the moment. Otherwise the launcher can only resolve to the newer version of JScience 4.3.


How to solve "Unrecognized Windows Sockets error: 0: Cannot bind" on Windows 7?
-------------------------------------------------------------------------------

When run GDA client or server on Windows 7 PC and trying to connect to EPICS IOC you may see this error message. To solve it you need to set

::

	-Djava.net.preferIPv4Stack=true

to your launcher property.

What do I need to run the SWT Browser inside Eclipse on Ubuntu 13.04?
---------------------------------------------------------------------
To enable 'internal web browser' in eclipse on Ubuntu 13.04, you need to install the old version of webkitgtk:

::

	sudo apt-get install libwebkitgtk-1.0.0

SWT Browser in eclipse Juno does not work with WebKitGTK 3.0.0 that comes with Ubuntu 13.04.


Where GDA server dependency is defined?
---------------------------------------
GDA server dependencies are defined using buckmister.cspec in your gda configuration folder. So if you have ClassNotFound issue when starting gda servers, this is the first place you should check.
