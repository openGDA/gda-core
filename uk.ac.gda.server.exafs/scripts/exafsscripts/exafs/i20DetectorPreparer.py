from java.lang import Exception

from gda.device.detector.xspress import Xspress2DetectorConfiguration
from gda.device.detector import VortexDetectorConfiguration
from uk.ac.gda.beans.exafs import XasScanParameters, XanesScanParameters

class I20DetectorPreparer:
    """
     Assume the sensitivities and offsets are arrays of scannables in order: [I0,It,Iref,I1]
    """
    def __init__(self, xspress2system, ExafsScriptObserver,sensitivities, sensitivity_units ,offsets, offset_units, cryostat_scannable,ionchambers,I1,vortex,topupChecker):
        self.xspress2system = xspress2system
        self.ExafsScriptObserver = ExafsScriptObserver
        self.sensitivities = sensitivities
        self.sensitivity_units = sensitivity_units
        self.offsets = offsets
        self.offset_units = offset_units
        self.cryostat_scannable=cryostat_scannable
        self.ionchambers = ionchambers
        self.I1 = I1
        self.vortex = vortex
        self.topupChecker = topupChecker
        
    def prepare(self, scanBean, detectorBean, outputBean, scriptFolder):
        """
        Validates the bean which defines the detectors and then configures the vortex or xspress
        based on the xml file whose name is in the bean.
        """
        
        self.setUpIonChambers(scanBean)
        
        if detectorBean.getExperimentType() == "Fluorescence" :
            detType = detectorBean.getFluorescenceParameters().getDetectorType()
            fullFileName = str(scriptFolder) + str(detectorBean.getFluorescenceParameters().getConfigFileName())
            print "Configuring",detType,"detector using",fullFileName
            if detType == "Germanium":
               Xspress2DetectorConfiguration(self.ExafsScriptObserver,fullFileName,None,outputBean).configure()
            else:
                VortexDetectorConfiguration(self.ExafsScriptObserver,fullFileName,None,outputBean).configure()
        elif detectorBean.getExperimentType() == "XES" :
            fullFileName = str(scriptFolder) + str(detectorBean.getXesParameters().getConfigFileName())
            VortexDetectorConfiguration(self.ExafsScriptObserver,fullFileName,None,outputBean).configure()
            
        ionChamberParamsArray = None
        if detectorBean.getExperimentType() == "Fluorescence" :
            ionChamberParamsArray = detectorBean.getFluorescenceParameters().getIonChamberParameters()
        elif detectorBean.getExperimentType() == "Transmission" :
            ionChamberParamsArray = detectorBean.getTransmissionParameters().getIonChamberParameters()
        elif detectorBean.getExperimentType() == "XES" :
            ionChamberParamsArray = detectorBean.getXesParameters().getIonChamberParameters()
        
        for ionChamberParams in ionChamberParamsArray:
            self.setup_amp_sensitivity(ionChamberParams, self.sensitivities, self.sensitivity_units)
            self.setup_amp_offset(ionChamberParams, self.offsets, self.offset_units)
            
    def setup_amp_sensitivity(self, ionChamberParams, sensitivities, sensitivity_units):
        if ionChamberParams.getChangeSensitivity():
            ionChamberName = ionChamberParams.getName()
            if ionChamberParams.getGain() == None or ionChamberParams.getGain() == "":
                return
            sensitivity, units = ionChamberParams.getGain().split()
            index = 0
            if ionChamberName == "It":
                index = 1;
            elif ionChamberName == "Iref":
                index = 2;
            elif ionChamberName == "I1":
                index = 3;
            try:
                print "Changing sensitivity of",ionChamberName,"to",ionChamberParams.getGain()
                sensitivities[index](sensitivity)
                sensitivity_units[index](units)
            except Exception, e:
                print "Exception while trying to change the sensitivity of ion chamber",ionChamberName
                print "Set the ion chamber sensitivity manually, uncheck the box in the Detector Parameters editor and restart the scan"
                print "Please report this problem to Data Acquisition"
                raise e
            
    def setup_amp_offset(self, ionChamberParams, offsets, offset_units):
        if ionChamberParams.getChangeSensitivity():
            if ionChamberParams.getOffset() == None or ionChamberParams.getOffset() == "":
                return
            offset, units = ionChamberParams.getOffset().split()
            index = 0
            if ionChamberName == "It":
                index = 1;
            elif ionChamberName == "Iref":
                index = 2;
            elif ionChamberName == "I1":
                index = 3;
            try:
                print "Changing amp offset of",ionChamberName,"to",ionChamberParams.getOffset()
                offsets[index](offset)
                offset_units[index](units)
            except Exception, e:
                print "Exception while trying to change the sensitivity of ion chamber",ionChamberName
                print "Set the ion chamber sensitivity manually, uncheck the box in the Detector Parameters editor and restart the scan"
                print "Please report this problem to Data Acquisition"
                raise e

    def setUpIonChambers(self,scanBean):    
        # determine max collection time
        maxTime = 0;
        if isinstance(scanBean,XanesScanParameters):
            for region in scanBean.getRegions():
                if region.getTime() > maxTime:
                    maxTime = region.getTime()
                
        elif isinstance(scanBean,XasScanParameters):
            if scanBean.getPreEdgeTime() > maxTime:
                maxTime = scanBean.getPreEdgeTime()
            if scanBean.getEdgeTime() > maxTime:
                maxTime = scanBean.getEdgeTime()
            if scanBean.getExafsTimeType() == "Constant Time":
                if scanBean.getExafsTime() > maxTime:
                    maxTime = scanBean.getExafsTime()
            else:
                if scanBean.getExafsToTime() > maxTime:
                    maxTime = scanBean.getExafsToTime()
                if scanBean.getExafsFromTime() > maxTime:
                    maxTime = scanBean.getExafsFromTime()
   
        # set dark current time and handle any errors here
        if maxTime > 0:
            print "Setting ionchambers dark current collectiom time to",str(maxTime),"s."
            self.ionchambers.setDarkCurrentCollectionTime(maxTime)
            self.I1.setDarkCurrentCollectionTime(maxTime)
            
            topupPauseTime = maxTime + self.topupChecker.tolerance
            print "Setting the topup checker to pause scans for",topupPauseTime,"s before topup"
            self.topupChecker.collectionTime = maxTime
            
    def _configureCryostat(self, cryoStatParameters):
        if LocalProperties.get("gda.mode") != 'dummy':
            self.cryostat_scannable.setupFromBean(cryoStatParameters)

    # This is only required for I20
    def setDetectorCorrectionParameters(self,beanGroup):
        scanObj = beanGroup.getScan()
        dtEnergy = 0.0
        # Use the fluo (emission) energy of the nearest transition based on the element and excitation edge
        # to calculate the energy dependent deadtime parameters.
        edge = scanObj.getEdge()
        if isinstance(scanObj,XasScanParameters) or isinstance(scanObj,XanesScanParameters):
            element = scanObj.getElement()
            elementObj = Element.getElement(element)
            dtEnergy = self._getEmissionEnergy(elementObj,edge)
            dtEnergy /= 1000 # convert from eV to keV
            print "Setting Ge detector deadtime calculation energy to be",str(dtEnergy),"keV based on element",element,"and edge",edge
        else :
            dtEnergy = scanObj.getFinalEnergy() 
            dtEnergy /= 1000 # convert from eV to keV
        self.xspress2system.setDeadtimeCalculationEnergy(dtEnergy)
 
    def _getEmissionEnergy(self,elementObj,edge):
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
 
    def _getEmissionEnergy(self,elementObj,edge):
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