from time import sleep
import math

from java.lang import InterruptedException, System
from java.lang import Thread as JThread
import java.lang.Exception

from scan import Scan

from gda.configuration.properties import LocalProperties
#from gda.data.scan.datawriter import XasAsciiDataWriter, NexusExtraMetadataDataWriter
from gda.device import DeviceException
from gda.epics import CAClient
from gda.exafs.scan import BeanGroup, BeanGroups, ScanStartedMessage, RepetitionsProperties
from gda.jython import InterfaceProvider
from gda.jython import ScriptBase
from gda.jython.scriptcontroller.event import ScanCreationEvent, ScanFinishEvent, ScriptProgressEvent
from gda.jython.scriptcontroller.logging import XasProgressUpdater
from gda.jython.scriptcontroller.logging import LoggingScriptController
from gda.jython.scriptcontroller.logging import XasLoggingMessage
from gda.scan import ScanBase, ContinuousScan
from gdascripts.metadata.metadata_commands import meta_clear_alldynamical

class QexafsScan(Scan):
    
    def __init__(self,detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, ExafsScriptObserver, XASLoggingScriptController, datawriterconfig, original_header, energy_scannable, ionchambers, topupMonitor, beamMonitor, cirrus=None):
        Scan.__init__(self, detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, ExafsScriptObserver, XASLoggingScriptController, datawriterconfig, original_header, energy_scannable, ionchambers)
        self.cirrus = cirrus
        self.cirrusEnabled = False
        self.gmsd_enabled = False
        self.additional_channels_enabled = False
        self.topupMonitor = topupMonitor
        self.beamMonitor = beamMonitor
        
    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, experimentFullPath, numRepetitions= -1, validation=True):

        print ""
        self.log("Starting QEXAFS scan...")
        
        experimentFullPath, experimentFolderName = self.determineExperimentPath(experimentFullPath)
        #print "qexafs XML file names",sampleFileName, scanFileName, detectorFileName, outputFileName
        self.setXmlFileNames(sampleFileName, scanFileName, detectorFileName, outputFileName)

        if self.cirrusEnabled:
            self.t = None

        sampleBean, scanBean, detectorBean, outputBean = self._createBeans(experimentFullPath,  sampleFileName, scanFileName, detectorFileName, outputFileName) 
        controller = self.ExafsScriptObserver

        outputBean.setAsciiFileName(sampleBean.getName())
        beanGroup = self._createBeanGroup(experimentFolderName, validation, controller, experimentFullPath, sampleBean, scanBean, detectorBean, outputBean)
        # work out which detectors to use (they will need to have been configured already by the GUI)
        detectorList = self._getQEXAFSDetectors(detectorBean, outputBean, scanBean) 
        self.log("Detectors: " + str(detectorList))
        
        # send initial message to the log
        loggingcontroller = self.XASLoggingScriptController
        scriptType = "Qexafs"
        unique_id  = LoggingScriptController.createUniqueID(scriptType);
        outputFolder = outputBean.getAsciiDirectory()+ "/" + outputBean.getAsciiFileName()

        # reset the properties used to control repetition behaviour
        LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY,"false")
        LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
        LocalProperties.set(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY,str(numRepetitions))
        repetitionNumber = 0
        timeRepetitionsStarted = System.currentTimeMillis();
        
        try:
            while True:
                repetitionNumber+= 1
                self._resetHeader()
                self.detectorPreparer.prepare(scanBean, detectorBean, outputBean, experimentFullPath)
                meta_clear_alldynamical()
                self.outputPreparer._resetAsciiStaticMetadataList()
                self.samplePreparer.prepare(sampleBean)
                initial_energy = scanBean.getInitialEnergy()
                final_energy = scanBean.getFinalEnergy()
                step_size = scanBean.getStepSize()
                self.outputPreparer.prepare(outputBean, scanBean, sampleBean)
                #print 'Prepare output'
                if len(outputBean.getCheckedSignalList()) > 0:
                    print "Signal parameters not available with QEXAFS"
                if self.energy_scannable == None:
                    raise "No object for controlling energy during QEXAFS found! Expected qexafs_energy (or scannable1 for testing)"
                numberPoints = int(math.ceil((final_energy-initial_energy)/step_size))
                self._runScript(outputBean.getBeforeScriptName())
                scan_time = scanBean.getTime()

                initialPercent = str(int((float(repetitionNumber - 1) / float(numRepetitions)) * 100)) + "%" 
                timeSinceRepetitionsStarted = System.currentTimeMillis() - timeRepetitionsStarted
                sampleName = sampleBean.getName()
                try:
                    logmsg = XasLoggingMessage(self._getMyVisitID(), unique_id, scriptType, "Starting "+scriptType+" scan...", str(repetitionNumber), str(numRepetitions), str(1), str(1), initialPercent,str(0),str(timeSinceRepetitionsStarted),beanGroup.getScan(),outputFolder,sampleName, 0)
                except java.lang.Exception, e: 
                    print "Problem with XasLoggingMessage" 
                loggingcontroller.update(None,logmsg)
                loggingcontroller.update(None,ScanStartedMessage(scanBean,detectorBean))

                if self.cirrusEnabled:
                    self.acquireCirrus()

                loggingbean = XasProgressUpdater(loggingcontroller,logmsg,timeRepetitionsStarted)
            
                current_energy = self.energy_scannable();
                dist_to_init = math.fabs(initial_energy - current_energy)
                dist_to_final = math.fabs(final_energy - current_energy)

                self.log( "Current energy: "             + str(current_energy))
                self.log( "Initial energy:  "            + str( initial_energy))
                self.log( "Final energy: "               + str( final_energy))
                self.log( "Distance to initial energy: " + str( dist_to_init))
                self.log( "Distance to final energy: "   + str( dist_to_final))
                
                start=initial_energy
                end=final_energy
                
                if scanBean.getBothWays() == True:
                    if dist_to_init < dist_to_final:
                        #go forward
                        start=initial_energy
                        end=final_energy
                    else:
                        #go reverse
                        start=final_energy
                        end=initial_energy
                
                self.log( "Scan: " + str(self.energy_scannable.getName()) + " " + str(start) + " " + str(end) + " " + str(numberPoints) + " " + str(scan_time) + " " + str(detectorList))
                controller.update(None, ScriptProgressEvent("Running QEXAFS scan"))
                thisscan = ContinuousScan(self.energy_scannable , start, end, numberPoints, scan_time, detectorList)
                thisscan = self._setUpDataWriter(thisscan, scanBean, detectorBean, sampleBean, outputBean, sampleBean.getName(), sampleBean.getDescriptions(), repetitionNumber, experimentFolderName, experimentFullPath, detectorFileName, outputFileName, sampleFileName, scanFileName)
                controller.update(None, ScanCreationEvent(thisscan.getName()))
                try:
                    if numRepetitions > 1:
                        print ""
                        self.log("Starting repetition " + str(repetitionNumber) + " of " + str(numRepetitions))
                    loggingbean.atScanStart()
                    thisscan.runScan()
                    controller.update(None, ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK));
                    loggingbean.atScanEnd()            
                    
                except DeviceException, e:
                    self._resetHeader()
                    loggingbean.atCommandFailure()
                    if LocalProperties.get(RepetitionsProperties.SKIP_REPETITION_PROPERTY) == "true":
                        LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
                        # check if an abort or panic stop has been issued, so the whole script should stop
                        if JThread.currentThread().isInterrupted():
                            raise e
                        # only wanted to skip this repetition, so absorb the exception and continue the loop
                        if numRepetitions > 1:
                            self.log("Repetition " + str(repetitionNumber), + " skipped.")
                    else:
                        self.log( "Exception while running QEXAFS:" + str(e))
                        self.log( "Will not abort queue but will continue to the next scan, if available")
                except java.lang.Exception, e:
                    self._resetHeader()
                    loggingbean.atCommandFailure()
                    if LocalProperties.get(RepetitionsProperties.SKIP_REPETITION_PROPERTY) == "true":
                        LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
                        # check if an abort or panic stop has been issued, so the whole script should stop
                        if JThread.currentThread().isInterrupted():
                            raise e
                        # only wanted to skip this repetition, so absorb the exception and continue the loop
                        if numRepetitions > 1:
                            self.log("Repetition " + str(repetitionNumber) + " skipped.")
                    else:
                        raise e
                except Exception, e:
                    self._resetHeader()
                    loggingbean.atCommandFailure()
                    if LocalProperties.get(RepetitionsProperties.SKIP_REPETITION_PROPERTY) == "true":
                        LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
                        # check if a panic stop has been issued, so the whole script should stop
                        if JThread.currentThread().isInterrupted():
                            raise e
                        # only wanted to skip this repetition, so absorb the exception and continue the loop
                        if numRepetitions > 1:
                            self.log("Repetition " + str(repetitionNumber), + " skipped.")
                    else:
                        raise e
                    
                # run the after scan script
                self._runScript(outputBean.getAfterScriptName())
                
                #check if halt after current repetition set to true
                if LocalProperties.get(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY) == "true":
                    self.log("Paused scan after repetition " + str(repetitionNumber) + ". To resume the scan, press the Start button in the Command Queue view. To abort this scan, press the Skip Task button.")
                    LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY,"false")
                    ScriptBase.setPaused(True)#
                    ScriptBase.checkForPauses()
                
                #check if the number of repetitions has been altered and we should now end the loop
                numRepsFromProperty = int(LocalProperties.get(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY))
                if numRepsFromProperty != numRepetitions and numRepsFromProperty <= (repetitionNumber):
                    self.log("The number of repetitions has been reset to " + str(numRepsFromProperty) + ". As " + str(repetitionNumber) + "repetitions have been completed this scan will now end.")
                    self._resetHeader()
                    break
                elif numRepsFromProperty <= (repetitionNumber):
                    self._resetHeader()
                    break
        finally:
            LocalProperties.set("gda.scan.useScanPlotSettings", "false")
            LocalProperties.set("gda.plot.ScanPlotSettings.fromUserList", "false")
            self._resetHeader()
            if self.cirrusEnabled:
                self.t.stop
            
            self.log("QEXAFS finished.")
                

    def _getQEXAFSDetectors(self, detectorBean, outputBean, scanBean):
        expt_type = detectorBean.getExperimentType()
        detectorList = []
        if expt_type == "Transmission":
            #print "This is a transmission scan"
            if self.gmsd_enabled==True:
                return self._createDetArray(["qexafs_counterTimer01_gmsd"], scanBean)
            elif self.additional_channels_enabled==True:
                return self._createDetArray(["qexafs_counterTimer01", "qexafs_counterTimer01_gmsd" ], scanBean)
            else:
                # when using xspress3 in qexafs scans. NB: must use the Transmission option in the UI
#                return self._createDetArray(["qexafs_FFI0_xspress3","qexafs_xspress3","qexafs_counterTimer01"], scanBean)
                return self._createDetArray(["qexafs_counterTimer01"], scanBean)
        else:
            if detectorBean.getFluorescenceParameters().getDetectorType() == "Silicon":
                return self._createDetArray(["qexafs_counterTimer01", "qexafs_xmap", "VortexQexafsFFI0"], scanBean)
            elif detectorBean.getFluorescenceParameters().getDetectorType() == "Xspress3":
                return self._createDetArray(["qexafs_counterTimer01", "qexafs_xspress3", "qexafs_FFI0_xspress3"], scanBean)
            else:
                return self._createDetArray(["qexafs_counterTimer01", "qexafs_xspress", "QexafsFFI0"], scanBean)
    
    def turnOnBeamCheck(self):
        InterfaceProvider.getDefaultScannableProvider().addDefault(self.topupMonitor)
        InterfaceProvider.getDefaultScannableProvider().addDefault(self.beamMonitor)

    def turnOffBeamCheck(self):
        InterfaceProvider.getDefaultScannableProvider().removeDefault(self.topupMonitor)
        InterfaceProvider.getDefaultScannableProvider().removeDefault(self.beamMonitor)

    def useCirrus(self, isUsed):
        self.cirrusEnabled = isUsed

    def acquireCirrus(self):
        from cirrus import ThreadClass
        self.cirrus.setMasses([2, 28, 32])
        self.t = ThreadClass(self.cirrus, self.energy_scannable, initial_energy, final_energy, "cirrus_scan.dat")
        self.t.setName("cirrus")
        self.t.start()
        
    def enableGMSD(self):
        self.gmsd_enabled=True
    
    def disableGMSD(self):
        self.gmsd_enabled=False
        
    def enableAdditionalChannels(self):
        self.additional_channels_enabled=True
    
    def disableAdditionalChannels(self):
        self.additional_channels_enabled=False
