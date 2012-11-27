from java.lang import Exception

from gda.device.detector.xspress import Xspress2DetectorConfiguration


class I20DetectorPreparer:
    """
     Assume the sensitivities and offsets are arrays of scannables in order: [I0,It,Iref,I1]
    """
    def __init__(self, xspress2system, ExafsScriptObserver,sensitivities, sensitivity_units ,offsets, offset_units):
        self.xspress2system = xspress2system
        self.ExafsScriptObserver = ExafsScriptObserver
        self.sensitivities = sensitivities 
        self.sensitivity_units = sensitivity_units 
        self.offsets = offsets
        self.offset_units = offset_units
        
    def prepare(self, detectorParameters, outputParameters, scriptFolder):
        """
        Validates the bean which defines the detectors and then configures the vortex or xspress
        based on the xml file whose name is in the bean.
        """
        if detectorParameters.getExperimentType() == "Fluorescence" :
            detType = detectorParameters.getFluorescenceParameters().getDetectorType()
            fullFileName = str(scriptFolder) + str(detectorParameters.getFluorescenceParameters().getConfigFileName())
            print "Configuring",detType,"detector using",fullFileName
            if detType == "Germanium":
               Xspress2DetectorConfiguration(self.ExafsScriptObserver,fullFileName,None,outputParameters).configure()
            else:
                from gda.device.detector import VortexDetectorConfiguration
                VortexDetectorConfiguration(self.ExafsScriptObserver,fullFileName,None,outputParameters).configure()
        
        ionChamberParamsArray = None
        if detectorParameters.getExperimentType() == "Fluorescence" :
            ionChamberParamsArray = detectorParameters.getFluorescenceParameters().getIonChamberParameters()
        elif detectorParameters.getExperimentType() == "Transmission" :
            ionChamberParamsArray = detectorParameters.getTransmissionParameters().getIonChamberParameters()
        elif detectorParameters.getExperimentType() == "XES" :
            ionChamberParamsArray = detectorParameters.getXesParameters().getIonChamberParameters()
        
        for ionChamberParams in ionChamberParamsArray:
            if ionChamberParams.getChangeSensitivity():
                ionChamberName = ionChamberParams.getName()
                if ionChamberParams.getGain() == None or ionChamberParams.getGain() == "":
                    continue
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
                    self.sensitivities[index](sensitivity)
                    self.sensitivity_units[index](units)
                except Exception, e:
                    print "Exception while trying to change the sensitivity of ion chamber",ionChamberName
                    print "Set the ion chamber sensitivity manually, uncheck the box in the Detector Parameters editor and restart the scan"
                    print "Please report this problem to Data Acquisition"
                    raise e