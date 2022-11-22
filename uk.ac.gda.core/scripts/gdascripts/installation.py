'''
functions to test GDA installation is live or dummy.
It handles 2 different properties used in GDA LocalProperties class!

Created on Nov 22, 2022

@author: fy65
'''
from gda.configuration.properties import LocalProperties

def __getGdaModeProperty():
	mode = str(LocalProperties.get("gda.mode"))
	if mode not in ("live", "dummy"):
		raise ValueError("gda.mode LocalProperty (perhaps via a System property) must be 'live' or 'dummy' not:", mode)
	return mode

def isLive():
	return __getGdaModeProperty()=="live" or not LocalProperties.isDummyModeEnabled()

def isDummy():
	return __getGdaModeProperty()=="dummy" or LocalProperties.isDummyModeEnabled()
