=================
 Writing Scripts
=================

Basic scripting
===============

Scripts can be entered and run directly from the script editor panel
of the GDA. You may work on more than one script, and of course open
and save them.

To run a script from the command line type::
  
   >>> run "ScriptName"


locations
---------

``/dls/iXX/scripts``
   scripts written by users for their experiment.

``/dls/iXX/software/gda/config/scripts``
   beamline-specific scripts written by Diamond staff (read-only).

``/dls/iXX/software/gda/scripts``
   core GDA scripts available to all beamline (read-only).

(where XX is the beamline number)


namespaces
----------

The Jython command-line in the JythonTerminal panel of the GDA GUI
uses the same namespace as any script, so variables created in one are
accessible from the other.


coding Standards
================
The Python language has an industry accepted set of coding and
documentation standards. In this section we present the standards that
are of particular benefit to GDA script writers.

We discuss how to and how not to layout code and how to document your
scripts (including documentation of classes and modules). The section
concludes with recommended practise on naming variables, objects,
functions, classes and modules.


Indentation
-----------

We recommend that you always use 4 spaces per indentation level. This
will make cutting and pasting code far easier and this indentation is
easy to read. Never mix tabs and spaces.


Long lines
----------

Try to limit all lines to a maximum of 72 characters. You can wrap
long lines using Python's implied line continuation inside parentheses
or use a backslash. For example::

   # wrapping lines with backslash
   if width == 0 and height == 0 and \
   colour == 'red' and emphasis == 'strong':
       f()
       
   # wrapping lines with parentheses
   if (width == 0 and height == 0 and
   colour == 'red' and emphasis == 'strong'):
       f()
    						
Documentation Standards and help
--------------------------------

You should write comments in your code as much as possible to explain
to any other user of the code (or yourself if you look at the code at
some point in the future) what the code does and why. Don't overdo it
either.

You should write docstrings for all public modules, functions,
classes, and methods. DocStrings are not necessary on non-public
methods.

Jython has a built-in mechanism for documentation. This is the __doc__
attribute. You write your comments in triple quotes (''') in the very
first line of every class or method definition. The contents of these
quote are then accessible via the __doc__ attribute. In the GDA you
can quickly access these comments via the help command::

   def myMethod():
       '''This is my comment explaining what this method does.
       
       The Jython convention is to have a single line which is a
       concise summary of the object or method starting with a
        capital letter and ending with a period. It should have
       not mention the functions name. If you need more detail,
       leave a blank line and then one or more paragraphs separated
       by blank lines.
       '''

Access the comments in two ways:

   >>> print myMethod.__doc__ 
   >>> help myMethod
    						
Help is available in this way for many built-in GDA objects and
classes.

Naming Conventions
------------------

Recommended naming conventions are discussed for all the common
entries in Jython


Package and module names
~~~~~~~~~~~~~~~~~~~~~~~~
Modules should have short, all-lowercase names with underscores as
separators: e.g:::
    
   my_module
    							
Class names
~~~~~~~~~~~
Class names should start with a uppercase letter and subsequent words
should be capitalised. This is known as CamelCase e.g.::
   
   MyClass
    							
Exception names
~~~~~~~~~~~~~~~

Exceptions should be classes, so use the class naming convention with
the suffix "Exception": e.g.::

   MyClassException
    							
Global variable names
~~~~~~~~~~~~~~~~~~~~~

Global variables are meant for use inside one module only. Start with
a lowercase letter and capitalise words: e.g.::

   myGlobalVariable
    							
Function names
~~~~~~~~~~~~~~

Function names should start with a lowercase letter and subsequent
words should be capitalised: e.g.::

   myFunction
    							
Always use 'self' for the first argument to instance methods of
classes. Always use 'cls' for the first argument to class (static)
methods.


Importing Modules
=================

Jython import statements require discussion since there are several
ways we can import modules. If we use the simplest form of import::
    
   >>> import myModule
    				
all code in the module is executed and then all the symbols defined in
the module are added to the namespace myModule. To access any of the
symbols in the namespace you must qualify the symbol with the
namespace. For example, if the module defines a class called MyClass,
then to instantiate an object of this class you must write::

   >>> import myModule
   >>> myObject = myModule.MyClass()
    				
However if you use an alternate form of import::
    
   >>> from myModule import *

all code in the module is executed and then all the symbols defined in
the module are added to the global namespace, rather than the
namespace myModule. This means we can now use unqualified names to
access symbols defined in the module. Thus the above example can now
be written::

   >>> from myModule import *
   >>> myObject = MyClass()
    				

Interrupting Scripts
====================

Although the GDA provides buttons to pause and stop your scripts, you
will find that your script may not get paused or stopped immediately.
Indeed it may take quite a long time for these actions to take effect.
This is a consequence of how the underlying Java system works. If you
understand the mechanism the GDA uses to pause and stop your scripts
you will be able to write additional code that will make your script
more responsive.

When you press either the pause or stop button in the GDA workbench,
the GDA system sends a signal to your Jython script. Upon receipt of
the signal the Java system will perform one of two possible actions.
If your script is busy executing code, then Java sets an interrupt
flag to indicate you should pause or stop. However the Jython system
will not attempt to check this flag until you complete the current
function call or loop statement and hence your script becomes
unresponsive. But if your script is already paused (in a sleep state),
then Java will interrupt your code immediately and stop the script.

Because you can never be sure when you will need to pause or stop the
script you can call the function::

   >>> pause()

or as it is aliased just::

   >>> pause
    				
to ask the GDA to check the interrupt flag at any time. If you make
this call inside a function or loop you will ensure a prompt response.
For example modify your loops to call pause periodically::
  
   >>> for i in range(1,10000):
   >>>	    if (i % 100 = 0) pause() # pause every 100 iterations
   >>>		# your_code ...
    				

Matrices
========

The GDA is deployed with the Jama matrix package to perform matrix
calculations. Jama is intended to be the standard matrix package for
Java. It was co-authored by MathWorks who produce Matlab. As any Java
class can be used within Jython without any special programming, Jama
classes may be called directly. However a wrapper class has been
written to enable Jama objects to interact with the Jython environment
more easily.

To use this library, import the GDA Jama wrapper by typing::

   >>> from Jama import Matrix as M

Then matrices can be created easily by typing command such as::

   >>> a = M([[1,2,3], [4,5,6]])

These matrices can then easily be manipulated in Jython. They can
interact with Jython lists, tuples and integers in ways you would
expect of matrices. To illustrate:

   >>> print a * 3
        [[3, 6, 9], [12, 15, 18]]

And::

   >>> b = M([1 2 3])
   >>> print b + [4, 5, 6]
       [5,7,9].


Advanced Scripting
==================

This section will be more of interest to beamline staff writing
scripts which are repeatedly used on the beamline by users.



Interacting with the user
-------------------------

You might write a script which requires user input every time it is
run, such as asking for a variable. To do this within the GDA
scripting environment you should use the requestInput command.

To get this command import the command from the Input class::

	from gda.jython.commands.Input import requestInput

When this is called the script will be paused and the prompt in the
JythonTerminal GUI panel changes to ask the user to input something
there. After the user has pressed return whatever has been typed in is
returned as a string by the requestInput command. For example::

   >>> target = requestInput("Where would you like to move s1_upper to?")
   >>> pos s1_upper float(target)
    					
Error handling in scripts
-------------------------

If you have a script which will be repeatedly used for a task, rather
than a simple one-off script to perform an experiment, you may wish to
add error handling to your script so that if anything goes wrong (such
as a hardware failure) while running the script an appropriate message
is displayed and action taken to cleanly stop equipment or resolve the
problem.

The Jython language has an error handling mechanism similar to many
other languages. Protected code is contained within try/except blocks
which 'wrap' the enclosed code and pickup exceptions if they occur
within them and run code to resolve or report the problem.

One thing to note is that all Jython errors inherit the Jython class
org.python.core.exceptions. This does not inherit from the Java
exception base class java.lang.Exception. This is important to
remember when writing error handling for scripts. When scripts are
halted or stopped by user intervention, it is a Java exception which
is raised; however many other errors which may occur in scripts will
be Jython errors. Scripts error handling should be able to catch both
forms of exception.

For example::

   >>> try:
   >>>     # your logic goes here
   >>>     # have lots of calls to gda.jython.commands.ScannableControl.pause()
   >>>     # to allow users to pause/halt the script cleanly
   >>> except InterruptedException, e:
   >>>     print "a user-requested halt!"
   >>>     # code here to stop the equipment and return the beamline to a safe state
   >>> except java.lang.Exception, e:
   >>>     print "a Java exception must have occurred!"
   >>>     # code here to stop the equipment and return the beamline to a safe state
   >>> except:
   >>>     print "a Jython error must have occurred!"
   >>>     # code here to stop the equipment and return the beamline to a safe state

Alternatively, Jython has a finally clause which is always operated
after a try block whether an exception has been thrown or not. This
may be more suitable in some circumstances. As you cannot have both
finally and except clauses with the same try block, you must nest try
blocks to make execution order unambiguous::


   >>> try:
   >>>	    try:
   >>> 	# your logic goes here
   >>> 	# have lots of calls to gda.jython.commands.ScannableControl.pause()
   >>> 	#to allow users to pause/halt the script cleanly
   >>>     finally:
   >>> 	# code here to stop the equipment and return the beamline to a safe state
   >>> except InterruptedException, e:
   >>>     print "a user-requested halt!"
   >>> except java.lang.Exception, e:
   >>>     print "a Java exception must have occurred!"
   >>> except:
   >>>     print "a Jython error must have occurred!"


Persistence
-----------

Persistence is when you wish to store variables so that they can be
saved a retrieved after software restarts. The GDA provides an easy to
use mechanism for this. Values are stored in xml files stored in the
/dls/iXX/software/gda/var directory. These xml files can be created,
read and saved from the Jython environment.

To use this system you must create a XMLConfiguration object (which
represents the XML file). You can then easily read and store values to
this file using a name to identify each piece of information. You must
ensure that you call the save method to make sure the XML file is
saved after every change. Here is example code on how to use this::

   >>> from uk.ac.diamond.daq.persistence.jythonshelf import LocalParameters
   >>> config = LocalParameters.getXMLConfiguration("my_parameters_file")
   >>> config.setProperty("mythings.myint", 42)
   >>> config.setProperty("mythings.mystring", "blarghh")
   >>> aJavaListOfStrings = ['one', 'two', 'three']
   >>> config.setProperty("otherthings.mylist", aJavaListOfStrings)
   >>>  
   >>> config.save() # Make sure to save them!
   >>>  
   >>> Integer myint = config.getInt("mythings.myint")
   >>> String mystring = config.getString("mythings.myint")
   >>> String[] stringArray = config.getStringArray("otherthings.mylist")
   >>> List stringList = config.getList("otherthings.mylist")