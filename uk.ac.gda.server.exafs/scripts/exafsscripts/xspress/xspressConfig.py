from gda.device.detector.xspress import Xspress2DetectorConfiguration
from gda.jython import InterfaceProvider
from gdascripts.parameters import beamline_parameters
from gda.factory import Finder
from gda.configuration.properties import LocalProperties
#from edu.sdsc.grid.io import local
import sys
from gdascripts.messages import handle_messages
from gdascripts.configuration.properties.scriptContext import defaultScriptFolder


# Top level function to be called by user with exceptions reported and not re-thrown
def xspress (xspressFile,outputFile, path=None):
	"""
	main xspress configuration command. 
	usage
	xspress fileName  - name of the XML file used to configure the detector
	
	The filepaths are relative to the gda user folder
	"""
	try:
		controller = Finder.getInstance().find("ExafsScriptObserver")
		return xspressEx(controller, xspressFile,outputFile,path)
	except:
		type, exception, traceback = sys.exc_info()
		log(None,"Error in xspress", type, exception, traceback, False)

def xspressEx(controller, xspressFile,outputFile,path):
	""" Version of xspress that does not catch exception """
	return worker(controller, xspressFile,outputFile, path)

def worker (controller, xspressFile,outputFile,path):
	try:
		if(path == None):
			conf = Xspress2DetectorConfiguration(controller, defaultScriptFolder(), xspressFile, outputFile)
		else:
			conf = Xspress2DetectorConfiguration(controller, defaultScriptFolder(), xspressFile, outputFile,path)
		msg  = conf.configure() #  Might throw Exception
		log(controller, "Xspress configuration successfully applied", None, None, None, False)
		return msg
	except :
		type, exception, traceback = sys.exc_info()
		log(controller, "Error configuring ", type, exception, traceback, True)


def log(controller, msg, exceptionType=None, exception=None, traceback=None, Raise=False):
	handle_messages.log(controller, msg, exceptionType, exception, traceback, Raise)


