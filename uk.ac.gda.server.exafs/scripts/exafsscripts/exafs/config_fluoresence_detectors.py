import sys

from gda.configuration.properties import LocalProperties
from gda.device.detector.xspress import Xspress2DetectorConfiguration
from gda.device.detector import VortexDetectorConfiguration
from gda.jython import InterfaceProvider
from gda.util import Element
from uk.ac.gda.beans.exafs import XasScanParameters, XanesScanParameters
from uk.ac.gda.beans.exafs.i20 import I20OutputParameters

from gdascripts.parameters import beamline_parameters
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
        
    def __call__(self,XMLFileNameToLoad,OutputParametersToLoad):
        onlyShowFF = False
        showDTRawValues = False
        saveRawSpectrum = False
        
        if (OutputParametersToLoad != None and isinstance(OutputParametersToLoad,I20OutputParameters)):
            onlyShowFF = OutputParametersToLoad.isXspressOnlyShowFF()
            showDTRawValues = OutputParametersToLoad.isXspressShowDTRawValues()
            saveRawSpectrum = OutputParametersToLoad.isXspressSaveRawSpectrum()

        self.configure(XMLFileNameToLoad,onlyShowFF, showDTRawValues, saveRawSpectrum)
        
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
    
    def setDetectorCorrectionParameters(self,scanBean):
        # Use the fluo (emission) energy of the nearest transition based on the element and excitation edge
        # to calculate the energy dependent deadtime parameters.
        dtEnergy=None
        if isinstance(scanBean,XasScanParameters) or isinstance(scanBean,XanesScanParameters):
            dtEnergy=self.resolveXASDeadtimeCalculationEnergy(scanBean)
        else :
            dtEnergy=self.resolveXESDeadtimeCalculationEnergy(scanBean)
        self.xspress2system.setDeadtimeCalculationEnergy(dtEnergy)
    
    def resolveXASDeadtimeCalculationEnergy(self, scanBean):
        dtEnergy = 0.0
        edge = scanBean.getEdge()
        element = scanBean.getElement()
        elementObj = Element.getElement(element)
        energy = self.getEmissionEnergy(elementObj,edge)
        energy /= 1000 # convert from eV to keV
        print "Setting Ge detector deadtime calculation energy to be",str(dtEnergy),"keV based on element",element,"and edge",edge
        return energy
    
    def resolveXESDeadtimeCalculationEnergy(self, scanBean):
        finalEnergy = scanBean.getFinalEnergy() 
        return self.convertEVtoKEV(finalEnergy)
    
    def convertEVtoKEV(self, energy):
        return energy/1000
    
    def setDeadtimeCalculationEnergy(self, energy):
        self.xspress2system.setDeadtimeCalculationEnergy(energy)
    
    def configure(self, xmlFileName, onlyShowFF, showDTRawValues, saveRawSpectrum):
        self.configuration.configure(xmlFileName, onlyShowFF, showDTRawValues, saveRawSpectrum)
    
    def getConfigureResult(self):
        return self.configuration.getMessage();
    
    def getEmissionEnergy(self,elementObj,edge):
        if str(edge) == "K":
            return elementObj.getEmissionEnergy("Ka1")
        elif str(edge) == "L1":
            return elementObj.getEmissionEnergy("La1")
        elif str(edge) == "L2":
            return elementObj.getEmissionEnergy("La1")
        elif str(edge) == "L3":
            return elementObj.getEmissionEnergy("La1")
        elif str(edge) == "M1":
            return elementObj.getEmissionEnergy("Ma1")
        elif str(edge) == "M2":
            return elementObj.getEmissionEnergy("Ma1")
        elif str(edge) == "M3":
            return elementObj.getEmissionEnergy("Ma1")
        elif str(edge) == "M4":
            return elementObj.getEmissionEnergy("Ma1")
        elif str(edge) == "M5":
            return elementObj.getEmissionEnergy("Ma1")
        else:
            return elementObj.getEmissionEnergy("Ka1")

        
class VortexConfig():
    
    def __init__(self, xmap, ExafsScriptObserver):
        self.xmap=xmap
        self.ExafsScriptObserver=ExafsScriptObserver
        self.configuration=None
    
    def initialize(self):
        self.configuration = VortexDetectorConfiguration(self.xmap, self.ExafsScriptObserver)
        
    def __call__(self,XMLFileNameToLoad,OutputParametersToLoad):
        saveRawSpectrum = False
        
        if (OutputParametersToLoad != None and isinstance(OutputParametersToLoad,I20OutputParameters)):
            saveRawSpectrum = OutputParametersToLoad.isVortexSaveRawSpectrum()
    
        self.configure(XMLFileNameToLoad, saveRawSpectrum)

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