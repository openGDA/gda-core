from gda.factory import Finder
from gda.device.detector.xspress import Xspress2DetectorConfiguration
from gda.device.detector import VortexDetectorConfiguration
from gda.exafs.scan import BeanGroup, BeanGroups
from gda.device.detector.xspress import XspressDetector
from gda.jython.commands.ScannableCommands import scan, add_default
from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.beans.exafs import XanesScanParameters

class I18DetectorPreparer:
    def __init__(self):
        pass
        
    def prepare(self, detectorParameters, outputParameters, scriptFolder):
        if detectorParameters.getExperimentType() == "Fluorescence":
            if (detectorParameters.getDetectorType() == "Germanium"):
                fullFileName = scriptFolder + detectorParameters.getConfigFileName()
                bean = BeansFactory.getBean(File(fullFileName))
                bean.setReadoutMode(XspressDetector.READOUT_MCA)
                bean.setResGrade(ResGrades.NONE)
                elements = bean.getDetectorList()
                for element in elements: 
                    rois = element.getRegionList()
                    element.setWindow(rois.get(0).getRoiStart(), rois.get(0).getRoiEnd())
                BeansFactory.saveBean(File(fullFileName), bean)
            self.configFluoDetector(detectorParameters, outputParameters, scriptFolder)

    """
    Validates the bean which defines the detectors and then configures the vortex or xspress
    based on the xml file whose name is in the bean.
    """
    def configFluoDetector(self, detectorParameters, outputParameters, scriptFolder):
        detType = detectorParameters.getFluorescenceParameters().getDetectorType()
        fullFileName = scriptFolder + detectorParameters.getFluorescenceParameters().getConfigFileName()
        print "configuring", detType, "detector using", fullFileName
        if detType == "Germanium":
            Xspress2DetectorConfiguration(Finder.getInstance().find("ExafsScriptObserver"), fullFileName, None, outputParameters).configure()
        else:
            VortexDetectorConfiguration(Finder.getInstance().find("ExafsScriptObserver"), fullFileName, None, outputParameters).configure()