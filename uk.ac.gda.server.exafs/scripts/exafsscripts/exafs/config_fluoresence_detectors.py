import sys
from gda.device.detector.xspress import Xspress2DetectorConfiguration
from gda.device.detector import VortexDetectorConfiguration
from gda.jython import InterfaceProvider
from gdascripts.parameters import beamline_parameters
from gda.configuration.properties import LocalProperties
from gdascripts.messages import handle_messages
from gdascripts.configuration.properties.scriptContext import defaultScriptFolder

class FluoresenceDetectorsConfig():
    
    def __init__(self, xspress2system, vortex, ExafsScriptObserver):
        self.xspress2system=xspress2system
        self.vortex=vortex
        self.ExafsScriptObserver=ExafsScriptObserver
        self.xspress2DetectorConfiguration=None
    
    def initialize(self, detectorParameters, scriptFolder):
        self.xspress2DetectorConfiguration = Xspress2DetectorConfiguration(self.xspress2system, self.ExafsScriptObserver)
    
    def createBeanFromXML(self, xmlPath):
        return self.xspress2DetectorConfiguration.createBeanFromXML(xmlPath)
        
    def createXMLfromBean(self, xspressBean):
        self.xspress2DetectorConfiguration.createXMLfromBean(xspressBean)
    
    def configXspress(self, xmlFileName, onlyShowFF, showDTRawValues, saveRawSpectrum):
        self.xspress2DetectorConfiguration.configure(xmlFileName, onlyShowFF, showDTRawValues, saveRawSpectrum)
        
    def configVortex(self, detectorParameters, scriptFolder):
        fullFileName = scriptFolder + detectorParameters.getFluorescenceParameters().getConfigFileName()
        VortexDetectorConfiguration(self.vortex, self.ExafsScriptObserver, fullFileName).configure()