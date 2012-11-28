from gda.factory import Finder
from uk.ac.gda.beans.exafs import XasScanParameters
from uk.ac.gda.beans.microfocus import MicroFocusScanParameters

def configFluoDetector(beanGroup):
    """
    Validates the bean which defines the detectors and then configures the vortex or xspress
    based on the xml file whose name is in the bean.
    """
    
    detType = beanGroup.getDetector().getFluorescenceParameters().getDetectorType()
    fullFileName = str(beanGroup.getScriptFolder()) + str(beanGroup.getDetector().getFluorescenceParameters().getConfigFileName())
    outputParameters = beanGroup.getOutput()
    #print "configuring",detType,"detector using",fullFileName
    if detType == "Germanium":
        from gda.device.detector.xspress import Xspress2DetectorConfiguration
        scanObj = beanGroup.getScan()
        edgeEnergy = 0.0
        if isinstance(scanObj,XasScanParameters):
            edgeEnergy = scanObj.getEdgeEnergy()
            edgeEnergy /= 1000 # convert from eV to keV
        elif not isinstance(scanObj,MicroFocusScanParameters):
            edgeEnergy = scanObj.getFinalEnergy() 
            edgeEnergy /= 1000 # convert from eV to keV
        Finder.getInstance().find("xspress2system").setDeadtimeCalculationEnergy(edgeEnergy)
        Xspress2DetectorConfiguration(Finder.getInstance().find("ExafsScriptObserver"),fullFileName,None,outputParameters).configure()
    else:
        from gda.device.detector import VortexDetectorConfiguration
        VortexDetectorConfiguration(Finder.getInstance().find("ExafsScriptObserver"),fullFileName,None,outputParameters).configure()
