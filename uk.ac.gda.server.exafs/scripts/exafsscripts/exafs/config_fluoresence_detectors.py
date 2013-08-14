import sys
from gda.device.detector.xspress import Xspress2DetectorConfiguration
from gda.device.detector import VortexDetectorConfiguration
from gda.jython import InterfaceProvider
from gdascripts.parameters import beamline_parameters
from gda.configuration.properties import LocalProperties
from gdascripts.messages import handle_messages
from gdascripts.configuration.properties.scriptContext import defaultScriptFolder
import java

class XspressConfig():
    
    def __init__(self, xspress2system, ExafsScriptObserver):
        self.xspress2system=xspress2system
        self.ExafsScriptObserver=ExafsScriptObserver
        self.configuration=None
    
    def initialize(self):
        self.configuration = Xspress2DetectorConfiguration(self.xspress2system, self.ExafsScriptObserver)
        
    def createBeanFromXML(self, xmlPath):
        try:
            return self.configuration.createBeanFromXML(xmlPath)
        except java.lang.Exception, e:
            print "Could not create XspressParameters bean ", e
        
    def createXspressXMLfromBean(self, xspressBean):
        try:
            self.configuration.createXMLfromBean(xspressBean)
            print "Wrote new Xspress Parameters to: ", xmap.getConfigFileName()
        except java.lang.Exception, e:
            print "Could not save XspressParameters bean ", e
        self.configuration.createXMLfromBean(xspressBean)
    
    def configure(self, xmlFileName, onlyShowFF, showDTRawValues, saveRawSpectrum):
        self.configuration.configure(xmlFileName, onlyShowFF, showDTRawValues, saveRawSpectrum)

        
class VortexConfig():
    
    def __init__(self, xmap, ExafsScriptObserver):
        self.xmap=xmap
        self.ExafsScriptObserver=ExafsScriptObserver
        self.configuration=None
    
    def initialize(self):
        self.configuration = VortexDetectorConfiguration(self.xmap, self.ExafsScriptObserver)

    def createBeanFromXML(self, xmlPath):
        try:
            return self.configuration.createBeanFromXML(xmlPath)
        except java.lang.Exception, e:
            print "Could not create VortexParameters bean ", e
        
    def createXMLfromBean(self, vortexBean):
        try:
            self.configuration.createXMLfromBean(vortexBean)
            print "Wrote new Vortex Parameters to: ", xmap.getConfigFileName()
        except java.lang.Exception, e:
            print "Could not save VortexParameters bean ", e

    def configure(self, xmlFileName, isSaveRawSpectrum):
        self.configuration.configure(xmlFileName, isSaveRawSpectrum)