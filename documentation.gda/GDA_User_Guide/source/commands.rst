==========================
 Extended Syntax Commands
==========================

A list of the GDA 'Extended Syntax' commands which are available on
all beamlines:


.. function:: foo(x)
   
	Return a line of text input from the user.


.. function:: list

	lists all the types of objects representing hardware on this
	beamline

.. function:: list <interfacename>

	lists all the objects of the given type (interface) on this
	beamline

.. function:: pos

	lists all Scannables and Detectors i.e. the objects which can
	be used in scans

.. function:: pos <object>

	returns the position of the Scannable object

.. function:: pos <object> <position>

	moves the Scannable to the given position

.. function:: pos <object> <position> <object> <position>

	concurrent move of multiple Scannables

.. function:: inc <object> <amount>

	relative move version of the pos command

.. function:: inc <object> <amount> <object> <amount>

 	concurrent relative move of multiple Scannables

.. function:: help

 	lists the extended syntax commands

.. function:: help <object>

 	gives a description of the object if available


.. function:: run <script_name>

	runs the given script as if it were opened and run from the
	JythonEditor. Note that the script must be located in the
	/dls/iXX/scripts folder.

.. function:: pause

 	checks to see if one of the pause/resume or halt buttons in
 	the JythonTerminal panel have been pressed. Use this in long
 	scripts to have a convenient place to pause/resume/abort the
 	script.

.. function:: reset_namepsace

 	restarts the GDA Jython environment without the need to
 	restart the entire GDA software. This is useful if you have a
 	problem with the namespace. Note: this will not rebuild
 	connections to hardware.

.. function:: alias <method_name>

 	add the given function to the extended syntax so that you do
 	not have to add ()'s to call the function. Useful for very
 	commonly used methods on the beamline.

.. function:: watch <object>

 	opens a pop-up box in the JythonTerminal panel and shows a
 	constantly refreshed value of the Scannable

.. function:: history

 	list the history of commands typed into that terminal

.. function:: !<command_string>

 	repeat the latest command which starts the same as the given
 	string.

.. function:: record [on|off]

 	starts/stops recording all terminal output to a file placed in
 	the scripts directory

.. function:: level <object>

 	returns the level attribute for this Scannable. Levels are
 	used to provide ordering when moving Scannables during scans

.. function:: level <object> <value>

 	changes the level value for this Scannable. 5 is the default
 	for Scannable objects and 10 is default for Detectors.

.. function:: list_defaults

 	lists the Scannables and detectors whose positions and outputs
 	will be included in scans by default without including them
 	when typing a scan command

.. function:: add_default <object>

 	adds a Scannable or detector to the list of defaults

.. function:: remove_default <object>

 	removes a Scannable or Detector from the list of defaults

The format of commands related to scanning are listed later. There are
also more commands available from Scannable objects which are also
listed later.
