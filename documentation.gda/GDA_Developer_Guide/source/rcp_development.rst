==============================
 Client GUI development
==============================


Introduction
============

An example RCP client product may be found in the 
uk.ac.gda.example.feature plugin. This contains two 
perspectives:

 - Scripts
 - Experiment
 
Scripts provides tools to view, edit and run Jython scripts.
It has the Jython Console view which displays the GDA's 
Jython environment. The Jython engine runs server-side but
the terminal console provided in this view runs on the 
client.

Client-Server Interaction
=========================

Most beamlines have 'production' Jython scripts which do the core 
work of the beamline. These script contain the logic of most frequently used data
collection - although at the heart of them is normally a regular GDA scan.

Having such script separate from the UI decouples how the experiment
is presented to the user from the actual data collection. The two parts
can be developed separately making for a more stable, flexible and
maintainable design.

This section outlines the different ways in which the UI can drive, and
communicate with, Jython scripts.

Interface Provider
------------------

A singleton object which provides access to the Command Server (Jython Server). 
It enables the same  code to interact with this whether client or sever side through
a series of static methods.
 
This design makes unit testing much simpler. It separates all the functionality
into groups of methods making it easier to find what you need. The functionality you would probably find most useful is ::

 - CommandRunner
    - To run commands in the GDA Jython environment
 - TerminalPrinter
    - To print to the Jython Console to provider user with progress
 - JythonServerStatusProvider
    - To find out if scans or scripts are running
 - ScanDataPointProvider
    - To be sent ScanDataPoints during a scan
 - JythonNamespace
    - To fetch and put objects in the Jython namespace
    - Objects must be serializable!


See the code for the individual interfaces listed above for the methods each
interface provides.

ScriptController
----------------

ScriptControllers are server-side, distributed objects used for point-to-point communication
between a specific script and parts of the UI which drive it and wish for
progress information or data back from the script.

The script is started by the UI through the CommandRunner provided by the Interface Provider (as above). The script then
sends progress information back to the UI through the ScriptController via its update method. Parts of the UI interested
in these message should fetch the ScriptController object reference through the Finder and then 
register themselves as IObservers of that ScriptController.

If the UI wishes to see all data points from a scan it should use the ScanDataPointProvider
object provided by the Interface Provider.

Server configuration:

.. code-block:: xml

   <bean id="MyScriptController" class="gda.jython.scriptcontroller.ScriptControllerBase"/>

In the client, use the Finder as it would be exported using CORBA:

.. code-block:: java

   controller = Finder.getInstance().find("MyScriptController")
   controller.update("MyScript","<message you want to send>")

See the exampleScriptControllerScript.py and the configuration file scriptController.xml for a running example in the example-config.


The Command Queue
-----------------

This is an improvement over simply running scripts directly from the UI.  The Command Queue is a distributed, server-side
object which hold a queue (sequence) of jobs to run. These jobs are normally Jython commands (which could be simple commands
or could start a script), but could be other objects.

The Command Queue provides a view which shows the list of jobs, control buttons and provides feedback with a progress bar and a text field.
For a more rich amount of feedback to the user either have the script provide output to the Jython console; or have a specific 
part of the UI update using a ScriptController as above; or use the ScriptLog (below).

One important configuration option is whether the Command Queue should auto-start when items are added to it (so it acts like a 
command buffer) or whether its should have to be manually started by the user, so the user has to build the list of commands first
before starting the queue. This is defined by the pauseWhenQueueEmpty boolean attribute of the FindableProcessorQueue.
 
There is a Command Queue configured in the example-config to demonstrate its use. 

To drive the Command Queue from the UI, create Command object using a CommandProvider:

.. code-block:: java

   JythonCommandCommandProvider command = 
      new JythonCommandCommandProvider("<command to run>","<command to display>",null)


Then add the command to the queue using the CommandQueueViewFactory:

.. code-block:: java

   CommandQueueViewFactory.getQueue().addToTail(command)


You can also interact with the queue programmatically. For example:

.. code-block:: java

   CommandQueueViewFactory.getProcessor().start(100000)


Within script, you should send updates to the Command Queue in your script using the JythonScriptProgressProvider :

.. code-block:: java

   from gda.commandqueue import JythonScriptProgressProvider
   JythonScriptProgressProvider.sendProgress(int percent, String message)


Script Log
----------

This is a view which provides the user with a history of the times a script is run, with more progress information
which is displayed in the Command Queue view. As in the above sections, this is intended to be used with a script
which performs a beamlines main data collections.

It works by using LoggingScriptController objects in place of ScriptControllers. This class required progress
information to be provided to it from the script using a message bean which implements ScriptControllerLoggingMessage.
By using this interface the LoggingScriptController stores the messages in a Derby database. The history in the
databases allows the Script Log view in the UI to show the history.

There is an example implementation of this in the exmaple-config. When running the example-config, open the ScriptLog view
and run the "MessagingDemoScript" script to see output in that view.


The Experiment Perspective
==========================

This perspective is a generic perspective aimed at beamlines which
repeatedly run scans defined using a large number of parameters. The parameters
for each scan are stored in one or more xml files. The xml files are
stored with the data and are edited by users in the UI using graphical 
editors. 

Scan maybe grouped in multiscans, and the xml files may be stored in
more than one directory. The UI helps users to organise their scans.

The scans are run using the Command Queue mechanism, and progress is
displayed in the Script Log view.

This functionality is provided by the uk.ac.gda.client.experimentdefinition plugin.

Each type of xml file has its own graphical editor
and behind each editor is a Java bean. The beans are used to 
transport the parameters to Jython scripts which hold the 
experimental logic.

The xml files provide persistence of the experimental options
and are stored in folders so users may build up a library of 
xml files during their visit to the beamline. These xml files
could then be retrieved in subsequent visits to repeat
experiments.

The relationship between the Java beans, xml files and editors
is held in the GDAs RichBean framework. The perspective which
uses these objects is mostly generic, but for each implementation 
of this perspective some coding is required. There is are extension
points which should be contributed to. This lists the 
implementation specific classes and options required to 
operate the Experiment perspective.

Development required
--------------------

To configure your own implementation of the Experiment 
Perspective:

1) Required dependencies
  
   a) Your plugin will need to depend on the following plugins::

      - uk.ac.gda.common
      - uk.ac.gda.common.rcp
      - uk.ac.gda.client.experimentdefinition
      - org.eclipse.core.resources
      - org.dawnsci.common.widgets
      

   b) It will also need to be a registered buddy of ``experimentdefinition`` by including the following line in the MANIFEST.MF file::
   
   		Eclipse-RegisterBuddy: uk.ac.gda.client.experimentdefinition
      


2) create the Java beans and related editor

   This will define your experiments. For more information, see the Javadoc in ``uk.ac.gda.common.rcp/src/uk/ac/gda/richbeans/package-info.java``. 
   The Beans must implement ``XMLRichBean``.
 
   a) Write the Java beans which will define your experiments and export that package.
   b) Write a Composite which will be used to as the UI to edit the contents of the bean and XML file. This must use a standard constructor for composites: (Composite parent, int style). As the RichBean framework uses reflection to map bean attributes to UI widgets there must be method names in the composite which match the getters in the bean. The Composite getter must return the RichBean widgets which extend FieldComposite. This composite could also be included in a view outside of the rest of the ExperimentDefinition infrastructure as a stand-alone part of the UI. This would be useful if the composite is for configuring hardware or if the whole experiment could be defined using a single bean.
   c) Write the mapping file and XSD file which is used to map the Java beans to xml.  The tag for the experiment object should match the class name. These should be referenced in each Java bean class by two public static URLs and two methods:

	::

 		static public final URL mappingURL = MyBean.class.getResource("MyBeanMapping.xml");
		static public final URL schemaURL  = MyBean.class.getResource("MyBeanMapping.xsd");

		public static MyBean createFromXML(String filename) throws Exception {
			return (MyBean) XMLHelpers.createFromXML(mappingURL, MyBean.class, schemaURL, filename);
		}

		public static void writeToXML(MyBean scanParameters, String filename) throws Exception {
			XMLHelpers.writeToXML(mappingURL, scanParameters, filename);
		}
		
	d) Write the Editor which will display the Composite and the XML. This editor should extend ExperimentBeanMultiPageEditor and so will be mostly boiler-plate:
	
	::
	
		public final class MyBeanEditor extends RichBeanMultiPageEditorPart {
	
		@Override
		public Class<?> getBeanClass() {
			return MyBean.class;
		}
	
		@Override
		public URL getMappingUrl() {
			return MyBean.mappingURL;
		}
	
		@Override
		public RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {
			DelegatingRichBeanEditorPart editor = new DelegatingRichBeanEditorPart(path,getMappingUrl(),this,editingBean);
			editor.setEditorClass(MyBean.class);
			editor.setRichEditorTabText("Example Custom UI");
			return editor;
		}
	
		@Override
		public URL getSchemaUrl() {
			return MyBean.schemaURL;
		}

}
	

3) Use extension points to configure your Experiment perspective.
 
   a) for each bean class contribute one entry to the following extension points:

      i) uk.ac.common.beans.factory
      ii) uk.ac.gda.richbeans.beantypes
      iii) org.eclipse.core.contenttype.contentTypes
      iv) org.eclipse.ui.editors (editor id must match to the Java class, and this must reference the content-type)

   b) make a contribution to the ``uk.ac.gda.client.experimentdefinition`` extension point. This references the implementation-specific classes which are used by the Experiment perspective to perform certain roles. Classes which extend the following abstract base classes are required:

    i) ExperimentObjectManager  -- creates and manages the experiments in a multi-scan
    ii) ExperimentObject  -- the scan object - holds references to the xml files
    iii) AbstractValidator -- logic for validating the values in the beans in each scan

   c) A command and handler to copy a template file for each bean type to the working directory need to be defined in your plugin. The underlying handler class is always uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler but separate contributions to the org.eclipse.ui.handlers and org.eclipse.ui.commands extension points need to be made for each bean type.
   
4) Configure the Command Queue, which is used by the Experiment perspective to run the scans. In the server add:

	::
	
		<bean id="commandQueue" class="gda.commandqueue.CommandQueue">
		</bean>
		<bean id="commandQueueProcessor" class="gda.commandqueue.FindableProcessorQueue">
			<property name="queue" ref="commandQueue" />
			<property name="startImmediately" value="false" />
			<property name="pauseWhenQueueEmpty" value="true" />
			<property name="logFilePath" value="${gda.logs.dir}/commandQueueProcessor.log" />
		</bean>
		<bean class="uk.ac.gda.remoting.server.GdaRmiServiceExporter">
			<property name="serviceName" value="gda/commandQueueProcessor" />
			<property name="service" ref="commandQueueProcessor" />
			<property name="serviceInterface" value="gda.commandqueue.IFindableQueueProcessor" />
		</bean>
		<bean id="MyLoggingScriptController"
			class="gda.jython.scriptcontroller.logging.LoggingScriptController">
			<property name="messageClassToLog"
				value="<an implementation specific bean implementing gda.jython.scriptcontroller.logging.ScriptControllerLoggingMessage>" />
			<property name="directory" value="${gda.var}/" />
			<property name="local" value="true"/>
		</bean>
	
		<bean class="uk.ac.gda.remoting.server.GdaRmiServiceExporter">
			<property name="serviceName" value="gda/MyLoggingScriptController" />
			<property name="service" ref="MyLoggingScriptController" />
			<property name="serviceInterface"
				value="gda.jython.scriptcontroller.logging.ILoggingScriptController" />
		</bean>

	And in the client xml configuration:

	:: 
	
		<bean id="MyLoggingScriptController" class="uk.ac.gda.remoting.client.GdaRmiProxyFactoryBean">
			<property name="serviceUrl" value="${gda.rmi.prefix}MyLoggingScriptController" />
			<property name="serviceInterface" value="gda.jython.scriptcontroller.logging.ILoggingScriptController" />
			<property name="refreshStubOnConnectFailure" value="true" />
		</bean>
		
		<bean id="commandQueueProcessor" class="uk.ac.gda.remoting.client.GdaRmiProxyFactoryBean">
			<property name="serviceUrl" value="${gda.rmi.prefix}commandQueueProcessor" />
			<property name="serviceInterface" value="gda.commandqueue.IFindableQueueProcessor" />
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
	     
	
Once configured, the Experiment Perspective can then be 
included in your own product and have the same
functionality for managing xml files and running scans 
as in the example product.

Example classes are in the gda.example.richbean package of the uk.ac.gda.example plugin.



    
