import sys
import java

from gda.configuration.properties import LocalProperties
from gda.device.detector.xspress import Xspress2DetectorConfiguration
from gda.device.detector.xspress import Xspress2BeanUtils

from gda.device.detector.xmap import VortexDetectorConfiguration
from gda.device.detector.xmap import VortexBeanUtils

from gda.jython import InterfaceProvider
from gda.util import Element
from uk.ac.gda.beans.exafs import XasScanParameters, XanesScanParameters
from uk.ac.gda.beans.exafs.i20 import I20OutputParameters


class XspressConfig():
    
    def __init__(self, xspress2system, ExafsScriptObserver):
        self.xspress2system=xspress2system
        self.ExafsScriptObserver=ExafsScriptObserver
        self.configuration=None
        self.xpressUtils=None
        
    def initialize(self):
        self.configuration = Xspress2DetectorConfiguration(self.xspress2system, self.ExafsScriptObserver)
        self.xpressUtils = Xspress2BeanUtils()
        
    def __call__(self,XMLFileNameToLoad,OutputParametersToLoad):
        
        if (self.configuration == None):
            self.initialize()
        
        onlyShowFF = False
        showDTRawValues = False
        saveRawSpectrum = False
        
        if (OutputParametersToLoad != None and isinstance(OutputParametersToLoad,I20OutputParameters)):
            onlyShowFF = OutputParametersToLoad.isXspressOnlyShowFF()
            showDTRawValues = OutputParametersToLoad.isXspressShowDTRawValues()
            saveRawSpectrum = OutputParametersToLoad.isXspressSaveRawSpectrum()

        self.configure(XMLFileNameToLoad, onlyShowFF, showDTRawValues, saveRawSpectrum)
        
    def createBeanFromXML(self, xmlPath):
        try:
            return self.xpressUtils.createBeanFromXML(xmlPath)
        except java.lang.Exception, e:
            print "Could not create XspressParameters bean ", e
            raise e
        
    def createXspressXMLfromBean(self, xspress, xspressBean):
        try:
            self.xpressUtils.createXMLfromBean(xspress, xspressBean)
            print "Wrote new Xspress Parameters to: ", xmap.getConfigFileName()

        except java.lang.Exception, e:
#             print "Could not save XspressParameters bean ", e
            raise e
        self.xpressUtils.createXMLfromBean(xspress, xspressBean)
    
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
        edge = scanBean.getEdge()
        element = scanBean.getElement()
        elementObj = Element.getElement(element)
        energy = self.getEmissionEnergy(elementObj,edge)
        energy /= 1000 # convert from eV to keV
        print "Setting Ge detector deadtime calculation energy to be",str(energy),"keV based on element",element,"and edge",edge
        return energy
    
    def resolveXESDeadtimeCalculationEnergy(self, scanBean):
        finalEnergy = scanBean.getFinalEnergy() 
        return self.convertEVtoKEV(finalEnergy)
    
    def convertEVtoKEV(self, energy):
        return energy/1000
    
    def setDeadtimeCalculationEnergy(self, energy):
        self.xspress2system.setDeadtimeCalculationEnergy(energy)
    
    def configure(self, xmlFileName, onlyShowFF, showDTRawValues, saveRawSpectrum):
        self.configuration.setOnlyShowFF(onlyShowFF)
        self.configuration.setShowDTRawValues(showDTRawValues)
        self.configuration.setSaveRawSpectrum(saveRawSpectrum)
        self.configuration.configure(xmlFileName)
    
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
        self.vortexUtils=None
        
    def initialize(self):
        self.configuration = VortexDetectorConfiguration(self.xmap, self.ExafsScriptObserver)
        self.vortexUtils=VortexBeanUtils()
        
    def __call__(self,XMLFileNameToLoad,OutputParametersToLoad):

        if (self.configuration == None):
            self.initialize()
        
        saveRawSpectrum = False
        
        if (OutputParametersToLoad != None and isinstance(OutputParametersToLoad,I20OutputParameters)):
            saveRawSpectrum = OutputParametersToLoad.isVortexSaveRawSpectrum()
#         print "XMLFileNameToLoad=", XMLFileNameToLoad
#         print "isSaveRawSpectrum", saveRawSpectrum
        self.configure(XMLFileNameToLoad, saveRawSpectrum)

    def createBeanFromXML(self, xmlPath):
        try:
            return self.vortexUtils.createBeanFromXML(xmlPath)
        except java.lang.Exception, e:
            print "Could not create VortexParameters bean ", e
            raise e
        
    def createXMLfromBean(self, xmap, vortexBean):
        try:
            self.vortexUtils.createXMLfromBean(xmap, vortexBean)
            print "Wrote new Vortex Parameters to: ", xmap.getConfigFileName()
        except java.lang.Exception, e:
            print "Could not save VortexParameters bean ", e
            raise e

    def configure(self, xmlFileName, saveRawSpectrum):
        print "xmlFileName=", xmlFileName
        print "isSaveRawSpectrum", saveRawSpectrum
        self.configuration.setSaveRawSpectrum(saveRawSpectrum)
        self.configuration.configure(xmlFileName)

    def getConfigureResult(self):
        return self.configuration.getMessage();
