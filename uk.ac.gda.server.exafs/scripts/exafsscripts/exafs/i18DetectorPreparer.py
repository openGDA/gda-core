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
    def __init__(self,xspressConfig, vortexConfig):
        self.xspressConfig = xspressConfig
        self.vortexConfig = vortexConfig
        
    def prepare(self, detectorBean, outputParameters, scriptFolder):
        if detectorBean.getExperimentType() == "Fluorescence":
            fluoresenceParameters = detectorBean.getFluorescenceParameters()
            detType = fluoresenceParameters.getDetectorType()
            xmlFileName = scriptFolder + fluoresenceParameters.getConfigFileName()
            if detType == "Germanium":
                self.xspressConfig.initialize()
                xspressBean = self.xspressConfig.createBeanFromXML(xmlFileName)
                onlyShowFF = xspressBean.isOnlyShowFF()
                showDTRawValues = xspressBean.isShowDTRawValues()
                saveRawSpectrum = xspressBean.isSaveRawSpectrum()
                self.xspressConfig.configure(xmlFileName, onlyShowFF, showDTRawValues, saveRawSpectrum)
            elif detType == "Silicon":
                self.vortexConfig.initialize()
                vortexBean = self.vortexConfig.createBeanFromXML(xmlFileName)
                saveRawSpectrum = vortexBean.isSaveRawSpectrum()
                self.vortexConfig.configure(xmlFileName, saveRawSpectrum)
        elif detectorBean.getExperimentType() == "Transmission":
            transmissionParameters = detectorBean.getTransmissionParameters()
        self._control_all_ionc(transmissionParameters.getIonChamberParameters())

    def completeCollection(self):
        # this will be called at the end of a loop of scans, or after an abort
        pass

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