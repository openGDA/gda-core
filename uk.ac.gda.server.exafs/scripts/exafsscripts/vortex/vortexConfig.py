from gda.device.detector import VortexDetectorConfiguration
from gda.jython import InterfaceProvider
from gdascripts.parameters import beamline_parameters
from gda.factory import Finder
from gda.configuration.properties import LocalProperties
import sys
from gdascripts.messages import handle_messages
from gdascripts.configuration.properties.scriptContext import defaultScriptFolder


# Top level function to be called by user with exceptions reported and not re-thrown
def vortex (vortexFileName, outputfile, path=None):
	"""
	main vortex configuration command. 
	usage
	vortex fileName  - name of the XML file used to configure the detector
	
	The filepaths are relative to the gda user folder
	"""
	try:
		controller = Finder.getInstance().find("ExafsScriptObserver")
		return vortexEx(controller, vortexFileName, outputfile, path)
	except:
		# do nothing here as exceptions would have already been logged
		pass

def vortexEx(controller, vortexFileName, outputfile,path):
	""" Version of vortex that does not catch exception """
	return worker(controller, vortexFileName, outputfile, path)

def worker (controller, vortexFileName,outputfile,path,LocalProperties=LocalProperties):
	try:
		if(path == None):
			conf = VortexDetectorConfiguration(controller, defaultScriptFolder(), vortexFileName,outputfile)
		else:
			conf = VortexDetectorConfiguration(controller, defaultScriptFolder(), vortexFileName,outputfile, path)
		msg  = conf.configure() #  Might throw Exception
		log(controller, "Vortex configuration successfully applied", None, None, None, False)
		return msg
	except :
		type, exception, traceback = sys.exc_info()
		log(controller, "Error configuring Vortex", type, exception, traceback, True)


def log(controller, msg, exceptionType=None, exception=None, traceback=None, Raise=False):
	handle_messages.log(controller, msg, exceptionType, exception, traceback, Raise)


