#
# Some general helpful functions
#

from gda.epics import CAClient
from gda.factory import Finder
from contextlib import contextmanager
from gda.jython import JythonServerFacade
from gda.jython.commands import ScannableCommands

import logging
logger = logging.getLogger("uk.ac.gda.core.scripts.gdascripts.utils.py")

def frange(start,end,step):
	'Floating-point version of range():   frange(start,end,step)'
	start=float(start); end=float(end); step=float(step);
	r=abs(end-start)
	step=abs(step)*(end-start)/r
	if abs(r/step)>1e6:
		print 'Too many points in list!'
		raise
	out=[start]
	while (abs(out[-1]-start)-abs(step/1e6))<r:
		out+=[out[-1]+step]
	return out[:-1]

def listprint(list):
	'Vertical print of list elements'
	for thing in list:
		print thing

def iterableprint(iterable):
	'Verticle print of an iterables elements'
	while iterable.hasNext():
		print iterable.next()

def attributes(object):
	'Print top-level attributes of an object'
	attribs=dir(object)
	for attrib in attribs:
		print  attrib, '\t\t\t', eval('object.'+attrib)

def caget(pvstring):
	'caget from Jython'
	cli=CAClient(pvstring)
	cli.configure()
	out=cli.caget()
	cli.clearup()
	return out

def caput(pvstring,value):
	'caput from Jython'
	cli=CAClient(pvstring)
	cli.configure()
	cli.caput(value)
	cli.clearup()
	
def cagetArray(pvstring):
	cli=CAClient(pvstring)
	cli.configure()
	out=cli.cagetArrayDouble()
	cli.clearup()
	return out
	
	
def caput_wait(pvstring, value, timeout=10):
	cli=CAClient(pvstring)
	cli.configure()
	cli.caput(timeout, value)
	cli.clearup()
	
def caput_string2waveform(pvstring, value):
	arr=[]
	for each in value:
		arr.append(ord(each))
	caput(pvstring, arr)
	
def printJythonEnvironment():
	import os,sys
	print '  cwd =', os.getcwd()
	#print '  login =', os.getlogin() # TODO: Broken in Jython 2.5.3?
	print '  javapath = ',os.environ['CLASSPATH']
	print '  jythonpath = ', sys.path

@contextmanager
def default_scannables(*scn):
	current = set(ScannableCommands.get_defaults())
	new_scannables = set(scn) - current
	ScannableCommands.add_default(*new_scannables)
	try:
		yield
	finally:
		ScannableCommands.remove_default(*new_scannables)


command_server = JythonServerFacade.getInstance()

def jobs():
	"""
	Print a string showing the threads running in the command server.

	Only shows the live threads.
	"""
	logger.debug("jobs() called")

	s = "%-10s %-12s %-8s %-11s %s\n" % ('DATE', 'TIME', 'TYPE', 'INTERRUPTED', 'COMMAND')
	thread_infos = command_server.getCommandThreadInfo()
	for t in thread_infos:
			date = t.getDate()
			time = t.getTime()
			thread_type = t.getCommandThreadType()
			cmd = t.getCommand();
			intrpt = '     X     ' if t.isInterrupted() else ''
			s += "%(date)-10s %(time)-12s %(thread_type)-8s %(intrpt)-11s %(cmd)s\n" % locals()
	print s

