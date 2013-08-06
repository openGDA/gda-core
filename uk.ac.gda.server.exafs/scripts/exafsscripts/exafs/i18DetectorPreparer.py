from gda.device.detector.xspress import Xspress2DetectorConfiguration
from gda.device.detector import VortexDetectorConfiguration
from gda.exafs.scan import BeanGroup, BeanGroups
from gda.device.detector.xspress import XspressDetector
from gda.jython.commands.ScannableCommands import scan, add_default
from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.beans.exafs import XanesScanParameters
from java.io import File
from gda.device.detector.xspress import ResGrades
from gda.epics import CAClient

class I18DetectorPreparer:
    def __init__(self):
        pass
        
    def prepare(self, detectorParameters, outputParameters, scriptFolder):
        if detectorParameters.getExperimentType() == "Fluorescence":
            if (detectorParameters.getFluorescenceParameters().getDetectorType() == "Germanium"):
                fullFileName = scriptFolder + detectorParameters.getFluorescenceParameters().getConfigFileName()
                bean = BeansFactory.getBean(File(fullFileName))
                bean.setReadoutMode(XspressDetector.READOUT_MCA)
                bean.setResGrade(ResGrades.NONE)
                elements = bean.getDetectorList()
                for element in elements: 
                    rois = element.getRegionList()
                    element.setWindow(rois.get(0).getRoiStart(), rois.get(0).getRoiEnd())
                BeansFactory.saveBean(File(fullFileName), bean)
            self.configFluoDetector(detectorParameters, outputParameters, scriptFolder)   
            self._control_all_ionc(detectorParameters.getFluorescenceParameters().getIonChamberParameters())
        elif detectorParameters.getExperimentType() == "Transmission":
            self._control_all_ionc(detectorParameters.getTransmissionParameters().getIonChamberParameters())

    def _control_all_ionc(self, ion_chambers_bean):
        self._control_ionc(ion_chambers_bean, 0)
        self._control_ionc(ion_chambers_bean, 1)

    def _control_ionc(self, ion_chambers_bean, ion_chamber_num):
        ion_chamber = ion_chambers_bean[ion_chamber_num]
        change_sensitivity = ion_chamber.getChangeSensitivity()
        if change_sensitivity == True:
            gain = ion_chamber.getGain()
            print "setting i0 sensitivity to ", gain
            if ion_chamber_num==0:
                pv = CAClient("BL18I-EA-IAMP-02:Gain.VAL")
            elif ion_chamber_num==1:
                pv = CAClient("BL18I-EA-IAMP-03:Gain.VAL")
            pv.configure() 
            pv.caput(self._resolve_gain_index(gain))
            
    def _resolve_gain_index(self, gain):
        if gain == "10^3 V/A":
            return 0 
        elif gain == "10^4 V/A":
            return 1
        elif gain == "10^5 V/A":
            return 2
        elif gain == "10^6 V/A":
            return 3
        elif gain == "10^7 V/A":
            return 4
        elif gain == "10^8 V/A":
            return 5
        elif gain == "10^9 V/A":
            return 6
        elif gain == "10^10 V/A":
            return 7
       
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