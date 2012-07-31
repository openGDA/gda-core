from gda.device.detector.xspress import Xspress2DetectorConfiguration


class I20DetectorPreparer:
    def __init__(self, xspress2system, ExafsScriptObserver):
        self.xspress2system = xspress2system
        self.ExafsScriptObserver = ExafsScriptObserver
        
    def prepare(self, detectorParameters, outputParameters, scriptFolder):
        """
        Validates the bean which defines the detectors and then configures the vortex or xspress
        based on the xml file whose name is in the bean.
        """
        
        detType = detectorParameters.getFluorescenceParameters().getDetectorType()
        fullFileName = str(scriptFolder) + str(detectorParameters.getFluorescenceParameters().getConfigFileName())
        print "configuring",detType,"detector using",fullFileName
        if detType == "Germanium":
           Xspress2DetectorConfiguration(self.ExafsScriptObserver,fullFileName,None,outputParameters).configure()
        else:
            from gda.device.detector import VortexDetectorConfiguration
            VortexDetectorConfiguration(self.ExafsScriptObserver,fullFileName,None,outputParameters).configure()
            
