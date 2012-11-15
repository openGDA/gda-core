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
        
    def prepare(self, detectorParameters, outputParameters, scriptFolder, beanGroup):
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
            
        scan = beanGroup.getScan()
        collectionTime = 0.0
        if isinstance(scan, XanesScanParameters):
            regions = scan.getRegions()        
            for region in regions:
                if(collectionTime < region.getTime()):
                    collectionTime = region.getTime()
        elif isinstance(scan, QEXAFSParameters):
            pass
        else:
            collectionTime = scan.getExafsTime()
            if(scan.getExafsToTime() > collectionTime):
                collectionTime = scan.getExafsToTime()
        print "setting collection time to" , str(collectionTime)
        
        command_server = Finder.getInstance().find("command_server")

        topupMonitor = command_server.getFromJythonNamespace("topupMonitor", None)
        topupMonitor.setPauseBeforePoint(True)
        topupMonitor.setPauseBeforeLine(False)
        topupMonitor.setCollectionTime(collectionTime)

        beam = command_server.getFromJythonNamespace("beam", None)
        beam.setPauseBeforePoint(True)
        beam.setPauseBeforeLine(True)
        add_default(beam)

        detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
        if(beanGroup.getDetector().getExperimentType() == "Fluorescence" and beanGroup.getDetector().getFluorescenceParameters().getDetectorType() == "Germanium"): 
            add_default(detectorFillingMonitor)
            detectorFillingMonitor.setPauseBeforePoint(True)
            detectorFillingMonitor.setPauseBeforeLine(False)
            detectorFillingMonitor.setCollectionTime(collectionTime)

        trajBeamMonitor = command_server.getFromJythonNamespace("trajBeamMonitor", None)
        trajBeamMonitor.setActive(False)

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