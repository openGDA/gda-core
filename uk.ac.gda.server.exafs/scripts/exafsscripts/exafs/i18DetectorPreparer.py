from gda.device.detector.xspress import Xspress2DetectorConfiguration
from gda.device.detector.xmap import VortexDetectorConfiguration
from gda.factory import Finder
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
from uk.ac.gda.beans.microfocus import MicroFocusScanParameters

from java.io import File

from gdascripts.parameters.beamline_parameters import JythonNameSpaceMapping

class I18DetectorPreparer:
    def __init__(self,xspressConfig, vortexConfig, xspress3Config, I0_keithley, It_keithley, cmos):
        self.xspressConfig = xspressConfig
        self.vortexConfig = vortexConfig
        self.xspress3Config = xspress3Config
        self.I0_keithley = I0_keithley
        self.It_keithley = It_keithley
        self.cmos = cmos

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
            elif detType == "Xspress3":
                self.xspress3Config.initialize()
                self.xspress3Config.configure(xmlFileName)
                finder = Finder.getInstance()
                xspress3 = finder.find("xspress3")
                if (isinstance(scanBean,XasScanParameters)or isinstance(scanBean,XanesScanParameters)):
                    xspress3.setReadDataFromFile(False)
                else:
                     xspress3.setReadDataFromFile(True)
            self._control_all_ionc(fluoresenceParameters.getIonChamberParameters())
            if fluoresenceParameters.isCollectDiffractionImages() and isinstance (scanBean,MicroFocusScanParameters):
                self._control_cmos(scanBean)
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

    def _control_all_ionc(self, ion_chambers_bean):
        self._control_ionc(ion_chambers_bean, 0)
        self._control_ionc(ion_chambers_bean, 1)

    def _control_ionc(self, ion_chambers_bean, ion_chamber_num):
        ion_chamber = ion_chambers_bean[ion_chamber_num]
        change_sensitivity = ion_chamber.getChangeSensitivity()
        if change_sensitivity == True:
            gain = ion_chamber.getGain()
            if ion_chamber_num==0:
                print "I0 sensitivity: ", gain
                self.I0_keithley(gain)
            elif ion_chamber_num==1:
                print "It sensitivity: ", gain
                self.It_keithley(gain)
                
    def _control_cmos(self, scanBean):
        if scanBean.isRaster():
            rowLength = scanBean.getXEnd() - scanBean.getXStart();
            pointsPerRow = (rowLength / scanBean.getXStepSize()) + 1.0;
            print "points per row",str(pointsPerRow)
            collectionTime = scanBean.getRowTime() /  pointsPerRow;
            print "time per point",str(collectionTime)
            print "Setting cmos to collect for",str(collectionTime),"s"
            self.cmos.setCollectionTime(collectionTime)
        else:
            print "Setting cmos to collect for",str(scanBean.getCollectionTime()),"s"
            self.cmos.setCollectionTime(scanBean.getCollectionTime());
            
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
