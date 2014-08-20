from gda.device.detector.xspress import Xspress2DetectorConfiguration
from gda.device.detector.xmap import VortexDetectorConfiguration
from gda.epics import CAClient
from gda.exafs.scan import BeanGroup, BeanGroups
from gda.exafs.scan import ExafsScanPointCreator,XanesScanPointCreator
from gda.device.detector.xspress import XspressDetector
from gda.jython import InterfaceProvider
from gda.jython.commands import ScannableCommands
from gda.jython.commands.ScannableCommands import scan, add_default
from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.beans.exafs import XanesScanParameters
from uk.ac.gda.beans.exafs import XasScanParameters
from uk.ac.gda.beans.exafs import QEXAFSParameters

from java.io import File

from gdascripts.parameters.beamline_parameters import JythonNameSpaceMapping

class I18DetectorPreparer:
    def __init__(self,xspressConfig, vortexConfig):
        self.xspressConfig = xspressConfig
        self.vortexConfig = vortexConfig

    def prepare(self, scanBean, detectorBean, outputParameters, scriptFolder):
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
            self._control_all_ionc(fluoresenceParameters.getIonChamberParameters())
        elif detectorBean.getExperimentType() == "Transmission":
            transmissionParameters = detectorBean.getTransmissionParameters()
            self._control_all_ionc(transmissionParameters.getIonChamberParameters())
            
        if isinstance (scanBean,XasScanParameters) or isinstance (scanBean,XanesScanParameters) or isinstance (scanBean,QEXAFSParameters):
            times = []
            self._configureMonitors(scanBean, detectorBean)
            if isinstance(scanBean,XasScanParameters):
                times = ExafsScanPointCreator.getScanTimeArray(scanBean)
            elif isinstance(scanBean,XanesScanParameters):
                times = XanesScanPointCreator.getScanTimeArray(scanBean)
            if len(times) > 0:
                print "Setting scan times, using array of length" + str(len(times))
                jython_mapper = JythonNameSpaceMapping()
                jython_mapper.counterTimer01.setTimes(times)                
        return

    def completeCollection(self):
        pass
        # this will be called at the end of a loop of scans, or after an abort
#        detectorFillingMonitor = InterfaceProvider.getJythonNamespace().getFromJythonNamespace("detectorFillingMonitor")
#        if detectorFillingMonitor != None:
#            self.finder.find("command_server").removeDefault(detectorFillingMonitor);

    def addMonitors(self, topupMonitor, beam, detectorFillingMonitor):
        self.topupMonitor=topupMonitor
        self.beam=beam
        self.detectorFillingMonitor=detectorFillingMonitor
        
    def _configureMonitors(self, scanBean, detectorBean):
        collectionTime = 0.0
        if isinstance(scanBean, XanesScanParameters):
            regions = scanBean.getRegions()        
            for region in regions:
                if(collectionTime < region.getTime()):
                    collectionTime = region.getTime()
        elif isinstance(scanBean, QEXAFSParameters):
            pass
        else:
            # EXAFS
            collectionTime = scanBean.getExafsTime()
            if(scanBean.getExafsToTime() > collectionTime):
                collectionTime = scanBean.getExafsToTime()
        print "setting collection time to " + str(collectionTime)

        if self.topupMonitor!=None:
            self.topupMonitor.setPauseBeforePoint(True)
            self.topupMonitor.setPauseBeforeLine(False)
            self.topupMonitor.setCollectionTime(collectionTime)

        if self.beam!=None:
            self.beam.setPauseBeforePoint(True)
            self.beam.setPauseBeforeLine(True)

        if self.detectorFillingMonitor!=None and detectorBean.getExperimentType() == "Fluorescence" and detectorBean.getFluorescenceParameters().getDetectorType() == "Germanium": 
            self.detectorFillingMonitor.setPauseBeforePoint(True)
            self.detectorFillingMonitor.setPauseBeforeLine(False)
            print "Adding the detectorFillingMonitor to the list of defaults"
            ScannableCommands.add_default([self.detectorFillingMonitor])
        else :
            ScannableCommands.remove_default([self.detectorFillingMonitor])
            
        
#     def _beforeEachRepetition(self,beanGroup,scriptType,scan_unique_id, numRepetitions, controller, repNum):
#         times = []
#         self._configureMonitors(beanGroup)
#         if isinstance(beanGroup.getScan(),XasScanParameters):
#             times = ExafsScanPointCreator.getScanTimeArray(beanGroup.getScan())
#         elif isinstance(beanGroup.getScan(),XanesScanParameters):
#             times = XanesScanPointCreator.getScanTimeArray(beanGroup.getScan())
#         if len(times) > 0:
#             self.log( "Setting scan times, using array of length",len(times))
#             jython_mapper = JythonNameSpaceMapping()
#             jython_mapper.counterTimer01.setTimes(times)
#             ScriptBase.checkForPauses()
#         return

    def _control_all_ionc(self, ion_chambers_bean):
        self._control_ionc(ion_chambers_bean, 0)
        self._control_ionc(ion_chambers_bean, 1)

    def _control_ionc(self, ion_chambers_bean, ion_chamber_num):
        ion_chamber = ion_chambers_bean[ion_chamber_num]
        change_sensitivity = ion_chamber.getChangeSensitivity()
        if change_sensitivity == True:
            gain = ion_chamber.getGain()
            print "I0 sensitivity: ", gain
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
