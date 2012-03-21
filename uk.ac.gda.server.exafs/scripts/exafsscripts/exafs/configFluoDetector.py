from gda.factory import Finder

def configFluoDetector(beanGroup):
    """
    Validates the bean which defines the detectors and then configures the vortex or xspress
    based on the xml file whose name is in the bean.
    """
    
    detType = beanGroup.getDetector().getFluorescenceParameters().getDetectorType()
    fullFileName = beanGroup.getScriptFolder() + beanGroup.getDetector().getFluorescenceParameters().getConfigFileName()
    outputParameters = beanGroup.getOutput()
    print "configuring",detType,"detector using",fullFileName
    if detType == "Germanium":
        from gda.device.detector.xspress import Xspress2DetectorConfiguration
        Xspress2DetectorConfiguration(Finder.getInstance().find("ExafsScriptObserver"),fullFileName,None,outputParameters).configure()
    else:
        from gda.device.detector import VortexDetectorConfiguration
        VortexDetectorConfiguration(Finder.getInstance().find("ExafsScriptObserver"),fullFileName,None,outputParameters).configure()
