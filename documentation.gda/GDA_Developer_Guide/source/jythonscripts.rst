======================
 Jython Script types
======================

There are three different 'types' - ``CORE``, ``CONFIG``, and ``USER`` - of Jython scripts which may be written.
As their purposes are different it is helpful to place them in different directories,
and to even have different permissions on those directories. It is possible to have
any number of directories for each 'type' of scripts, so a beamline could use configuration
scripts from a shared library in addition to its own configuration folder. Setting up these
paths is defined in the :ref:`scripts-folder-configuration` section below. The locations given below are simply
examples provided for guidance.

.. _Core-Scripts-label:

**Core Scripts**
  * Location: ``${gda.root}/uk.ac.gda.core/scripts``
  * Type:     ``CORE``
  
  These are general purpose scripts to be shared by all beamlines. They are not intended to
  be edited by users or beamline staff. Scripts should be placed within the ``gdascripts`` folder
  to clearly show that the modules are not Java classes which have a gda top-level directory.

*EPICS scripts*
  * Location: ``${gda.root}/uk.ac.gda.epics/scripts``
  * Type:     ``CORE``
  
  These scripts have the same type as the :ref:`Core Scripts <Core-Scripts-label>` described above. 
  They are EPICS related scripts and require EPICS IOC to run successfully. 
  
**Beamline configuration scripts**
  * Location: ``${gda.config}/scripts``
  * Type:     ``CONFIG``
  
  These are beamline specific scripts. These will be data collection or analysis utilities
  which are not expected to be edited by users. They may be written by beamline staff.
  
  ``localStation.py`` is often located in this directory; it is run by the ``JythonServer`` object
  when the server is started to customise the Jython environment for that beamline.
  
**User scripts**
  * Location: any folder defined by the Spring IoC XML configuration for the GDA ``command_server``.
  * Type:     ``USER``
  
  These are scripts written or can be edited by users for their experiment. This is a separate folder from the
  other types of scripts as this directory may want to be emptied at the end of an experiment.

  An example of this ``USER`` type folder is ``example-config/users/scripts`` in the ``example-config`` project.

.. _scripts-folder-configuration:
 
Configuration
=============

The paths to script project folders are defined via beans which are given as parameters of the
Jython Server object in the server's Spring IoC configuration. Each project is defined as a
``gda.jython.ScriptProject`` object, which includes the path to the folder containing the
scripts, the name of the project and its type (``USER``, ``CONFIG`` or ``CORE``). The project name
appears in the RCP user interface in the Project Explorer view.

The project objects should be referenced by, or inner beans of, a ``gda.jython.ScriptPaths``
bean. The projects are defined as an ordered list: when a Jython ``run`` command is given, the
command server searches for the script file in the folders in the order they are listed in the
Spring configuration. The ``ScriptPaths`` object should also be configured with the full path
to the startup script (often named ``localStation.py``) which is run when the command server
starts up.

Finally, the ``ScriptPaths`` bean should itself be referred by the ``gda.jython.JythonServer``
instance, which must be named ``command_server``. This is the controller object for the Jython
environment in the GDA. Putting all of this together, the resultant Spring configuration will
look like this excerpt from the example configuration's ``server.xml`` file::

  <bean id="command_server" class="gda.jython.JythonServer">
    <property name="jythonScriptPaths">
      <bean class="gda.jython.ScriptPaths">
        <property name="projects">
          <list>
            <bean class="gda.jython.ScriptProject">
              <property name="path" value="${gda.config}/users/scripts" />
              <property name="name" value="Scripts: User" />
              <property name="type" value="USER" />
            </bean>
            <bean class="gda.jython.ScriptProject">
              <property name="path" value="${gda.config}/scripts" />
              <property name="name" value="Scripts: Config" />
              <property name="type" value="CONFIG" />
            </bean>
            <bean class="gda.jython.ScriptProject">
              <property name="path" value="${gda.root}/uk.ac.gda.core/scripts" />
              <property name="name" value="Scripts: Core" />
              <property name="type" value="CORE" />
            </bean>
          </list>
        </property>
        <property name="startupScript" value="${gda.config}/scripts/localStation.py" />
      </bean>
    </property>
    <property name="remotePort" value="9999" />
  </bean>
 
Adding a Script Queue to the Configuration
==========================================

On the Server add the following beans::

	<bean id="commandQueue" class = "gda.commandqueue.CommandQueue">
	</bean>

	<bean id="commandQueueProcessor" 
		class = "gda.commandqueue.FindableProcessorQueue">
		<property name="queue" ref="commandQueue"/>
		<property name="startImmediately" value="true"/>
		<property name="logFilePath" value="${gda.var}/commandQueueProcessor.log"/>		
	</bean>

	<bean class="uk.ac.gda.remoting.server.GdaRmiServiceExporter">
		<property name="serviceName" value="gda/commandQueueProcessor" />
		<property name="service" ref="commandQueueProcessor" />
		<property name="serviceInterface" 
			value="gda.commandqueue.IFindableQueueProcessor" />
	</bean>
	
On the client add the following beans::

	<bean id="commandQueueProcessor" 
		class="uk.ac.gda.remoting.client.GdaRmiProxyFactoryBean">
		<property name="serviceUrl" 
			value="rmi://<server-host-name>/gda/commandQueueProcessor" />
		<property name="serviceInterface" 
			value="gda.commandqueue.IFindableQueueProcessor" />
		<property name="refreshStubOnConnectFailure" value="true" />
	</bean>
	<bean class="gda.rcp.util.OSGIServiceRegister">
		<property name="class" value="gda.commandqueue.Processor" />
		<property name="service" ref="commandQueueProcessor" />
	</bean>
	<bean class="gda.rcp.util.OSGIServiceRegister">
		<property name="class" value="gda.commandqueue.Queue" />
		<property name="service" ref="commandQueueProcessor" />
	</bean>
	
Scripts that take a long time should inform the user of progress and regularly allow the
script to be paused. These two actions can be done by code of the form::

	from gda.commandqueue import JythonScriptProgressProvider
	JythonScriptProgressProvider.sendProgress( percent, msg)

where percent is percent complete ( integer) and msg is a string to be displayed.

How to submit a script to the queue from the RCP GUI is documented in the class ``uk.ac.gda.client.actions.QueueScriptSelectionActionDelegate``

To submit a script to the queue from the Jython terminal enter the command::

	finder.find("commandQueue").addToTail(
		JythonScriptFileCommandProvider(<path to script>))

Linking Scripts and the GUI
=========================================

It is beneficial to hold beamline-specific experimental logic in Jython scripts instead of Java
code as the Jython can be edited at run-time by both GDA developers and other beamline staff.
These Jython scripts would probably use the main scanning mechanism to collect data, but there may 
be work outside of the scans to perform, such as preparing sample environments or running some
beamline-alignment logic.  

To store this logic as Jython scripts makes development and maintenance simpler of the what is often
the most complex and, over time, changing part of a GDA installation. However it is often useful to 
report progress of these scripts to the user.

To enable communication from scripts, a distributed object is used which acts as a middle-man between
a specific script and parts of the GUI interested in this script's work. The gda.jython.scriptcontroller.ScriptController
class is a server-side object which fans out messages from a script to IObserver classes on the client-side.
The GUI can then report progress back to the users.

Example configuration::

	<bean id="MyScriptObserver" class="gda.jython.scriptcontroller.ScriptControllerBase"/>
	
Client-side Java classes would then implement the IObservable interface, register themselves as 
Observers of this object and receive events through the update(Object,Object) method. The script
would send these messages by retrieving this object from the finder and sending messages via the
ScriptController's update() method::

	controller = Finder.getInstance().find("MyScriptObserver")
	controller.update(None,ScriptProgressEvent("I have got to this point in the script"))
	
Although the IObserver/IObservable interfaces allow any serializable object to be passed to the GUI,
communication is clearer when specific event objects are used. There are some in the 
gda.jython.scriptcontroller.event package. New event types, if generic enough, should be added there
to give some form of standardisation to the system.

For example, scans initiated by the script can have their unique IDs broadcast to the script's observers 
using the ScanCreationEvent class::

	myscan = ConcurrentScan(args)
	scan_id = myscan.getName()
	controller.update(None,ScriptProgressEvent("Starting scan..."))
	controller.update(None,ScanCreationEvent(scan_id)
	myscan.runScan()  
	controller.update(None,ScanFinishEvent(scan_id,ScanFinishEvent.FinishType.OK))

A simple way to start a script from the client is to use the gda.jython.scriptcontroller.ScriptExecutor
class. See the Javadoc for more details::

	ScriptExecutor.Run("MyScriptObserver", this, null, "myscript()",null);
	
This would run the script, which would send progress messages back to the Java class via its update()
method, and block until the script has finished.

	






 