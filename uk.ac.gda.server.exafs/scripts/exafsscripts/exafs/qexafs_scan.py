from time import sleep
import math

from java.lang import InterruptedException, System
import java.lang.Exception

from scan import Scan

from gda.configuration.properties import LocalProperties
from gda.data.scan.datawriter import XasAsciiDataWriter, NexusExtraMetadataDataWriter
from gda.epics import CAClient
from gda.exafs.scan import BeanGroup, BeanGroups, ScanStartedMessage, RepetitionsProperties
from gda.jython import ScriptBase
from gda.jython.scriptcontroller.event import ScanCreationEvent, ScanFinishEvent, ScriptProgressEvent
from gda.jython.scriptcontroller.logging import XasProgressUpdater
from gda.jython.scriptcontroller.logging import LoggingScriptController
from gda.jython.scriptcontroller.logging import XasLoggingMessage
from gda.scan import ScanBase, ContinuousScan

class QexafsScan(Scan):
    
    def __init__(self,detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, ExafsScriptObserver, XASLoggingScriptController, datawriterconfig, original_header, energy_scannable, ionchambers, cirrus=None):
        Scan.__init__(self, detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, ExafsScriptObserver, XASLoggingScriptController, datawriterconfig, original_header, energy_scannable, ionchambers)
        self.cirrus = cirrus
        self.cirrusEnabled = False
        self.beamCheck = True
        self.gmsd_enabled = False
        self.additional_channels_enabled = False
        
    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, experimentFullPath, numRepetitions= -1, validation=True):
        experimentFullPath, experimentFolderName = self.determineExperimentPath(experimentFullPath)

        if self.cirrusEnabled:
            self.t = None

        sampleBean, scanBean, detectorBean, outputBean = self._createBeans(experimentFullPath, sampleFileName, scanFileName, detectorFileName, outputFileName) 
        controller = self.ExafsScriptObserver

        outputBean.setAsciiFileName(sampleBean.getName())
        beanGroup = self._createBeanGroup(experimentFolderName, validation, controller, experimentFullPath, sampleBean, scanBean, detectorBean, outputBean)
        # work out which detectors to use (they will need to have been configured already by the GUI)
        detectorList = self._getQEXAFSDetectors(detectorBean, outputBean, scanBean) 
        print "detectors to be used:", str(detectorList)
        
        # send initial message to the log
        loggingcontroller = self.XASLoggingScriptController
        scriptType = "Qexafs"
        unique_id  = LoggingScriptController.createUniqueID(scriptType);
        outputFolder = outputBean.getAsciiDirectory()+ "/" + outputBean.getAsciiFileName()

        print "Starting Qexafs scan..."

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
                self.samplePreparer.prepare(sampleBean)
                initial_energy = scanBean.getInitialEnergy()
                final_energy = scanBean.getFinalEnergy()
                step_size = scanBean.getStepSize()
                self.outputPreparer.prepare(outputBean, scanBean)
                if len(outputBean.getCheckedSignalList()) > 0:
                    print "Signal parameters not available with QEXAFS"
                if self.energy_scannable == None:
                    raise "No object for controlling energy during QEXAFS found! Expected qexafs_energy (or scannable1 for testing)"
                numberPoints = int(math.ceil((final_energy-initial_energy)/step_size))
                self._runScript(outputBean.getBeforeScriptName())
                scan_time = scanBean.getTime()

                initialPercent = str(int((float(repetitionNumber - 1) / float(numRepetitions)) * 100)) + "%" 
                timeSinceRepetitionsStarted = System.currentTimeMillis() - timeRepetitionsStarted
                logmsg = XasLoggingMessage(unique_id, scriptType, "Starting "+scriptType+" scan...", str(repetitionNumber), str(numRepetitions), str(1), str(1), initialPercent,str(0),str(timeSinceRepetitionsStarted),beanGroup.getScan(),outputFolder)
               
                loggingcontroller.update(None,logmsg)
                loggingcontroller.update(None,ScanStartedMessage(scanBean,detectorBean))

                if self.cirrusEnabled:
                    self.acquireCirrus()

                loggingbean = XasProgressUpdater(loggingcontroller,logmsg,timeRepetitionsStarted)
            
                if (LocalProperties.get("gda.mode") == 'live'):
                    if self.beamCheck==True:
                        feAbsPV = "FE18B-RS-ABSB-02:STA"
                        feAbsStatus = int(CAClient().caget(feAbsPV))
                        print "feAbsStatus=",feAbsStatus
                        while feAbsStatus!=1 and self.beamCheck==True:
                            feAbsStatus = int(CAClient().caget(feAbsPV))
                            print "Checking whether front end absorber is open. Currently "+str(feAbsStatus)
                            sleep(2)
            
                current_energy = self.energy_scannable();
                dist_to_init = math.fabs(initial_energy - current_energy)
                dist_to_final = math.fabs(final_energy - current_energy)

                print "energy currently at ", current_energy
                print "initial_energy ", initial_energy
                print "final_energy", final_energy
                print "dist_to_init", dist_to_init
                print "dist_to_final", dist_to_final
                
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
                
                print "running QEXAFS scan:", self.energy_scannable.getName(), start, end, numberPoints, scan_time, detectorList
                controller.update(None, ScriptProgressEvent("Running QEXAFS scan"))
                thisscan = ContinuousScan(self.energy_scannable , start, end, numberPoints, scan_time, detectorList)
                controller.update(None, ScanCreationEvent(thisscan.getName()))
                try:
                    if numRepetitions > 1:
                        print ""
                        print "Starting repetition", str(repetitionNumber),"of",numRepetitions
                    loggingbean.atScanStart()
                    thisscan.runScan()
                    controller.update(None, ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK));
                    loggingbean.atScanEnd()            
                except java.lang.Exception, e:
                    #print "abort due to other Java exception"
                    self._resetHeader()
                    loggingbean.atCommandFailure()
                    raise e
                except InterruptedException, e:
                    self._resetHeader()
                    loggingbean.atCommandFailure()
                    if LocalProperties.get(RepetitionsProperties.SKIP_REPETITION_PROPERTY) == "true":
                        LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
                        ScanBase.interrupted = False
                        # check if a panic stop has been issued, so the whole script should stop
                        if ScriptBase.isInterrupted():
                            raise e
                        # only wanted to skip this repetition, so absorb the exception and continue the loop
                        if numRepetitions > 1:
                            print "Repetition", str(repetitionNumber),"skipped."
                    else:
                        print e
                        raise # any other exception we are not expecting so raise whatever this is to abort the script
                except:
                    self._resetHeader()
                    loggingbean.atCommandFailure()
                    raise
                    
                # run the after scan script
                self._runScript(outputBean.getAfterScriptName())
                
                #check if halt after current repetition set to true
                if LocalProperties.get(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY) == "true":
                    print "Paused scan after repetition",str(repetitionNumber),". To resume the scan, press the Start button in the Command Queue view. To abort this scan, press the Skip Task button."
                    LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY,"false")
                    self.commandQueueProcessor.pause(500)
                    ScriptBase.checkForPauses()
                
                #check if the number of repetitions has been altered and we should now end the loop
                numRepsFromProperty = int(LocalProperties.get(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY))
                if numRepsFromProperty != numRepetitions and numRepsFromProperty <= (repetitionNumber):
                    print "The number of repetitions has been reset to",str(numRepsFromProperty), ". As",str(repetitionNumber),"repetitions have been completed this scan will now end."
                    self._resetHeader()
                    break
                elif numRepsFromProperty <= (repetitionNumber):
                    self._resetHeader()
                    break
        finally:
            #self.energy_scannable.stop()
            # repetition loop completed, so reset things
            # TODO remove metadata enteries
            LocalProperties.set("gda.scan.useScanPlotSettings", "false")
            LocalProperties.set("gda.plot.ScanPlotSettings.fromUserList", "false")
            #remove added metadata from default metadata list to avoid multiple instances of the same metadata
            self._resetHeader()
            if self.cirrusEnabled:
                self.t.stop
 
    def _getQEXAFSDetectors(self, detectorBean, outputBean, scanBean):
        expt_type = detectorBean.getExperimentType()
        detectorList = []
        if expt_type == "Transmission":
            print "This is a transmission scan"
            if self.gmsd_enabled==True:
                return self._createDetArray(["qexafs_counterTimer01_gmsd"], scanBean)
            elif self.additional_channels_enabled==True:
                return self._createDetArray(["qexafs_counterTimer01", "qexafs_counterTimer01_gmsd" ], scanBean)
            else:
                return self._createDetArray(["qexafs_counterTimer01"], scanBean)
        else:
            if detectorBean.getFluorescenceParameters().getDetectorType() == "Silicon":
                print "This scan will use the vortex detector"
                return self._createDetArray(["qexafs_counterTimer01", "qexafs_xmap", "VortexQexafsFFI0"], scanBean)
                #return self._createDetArray(["qexafs_counterTimer01", "qexafs_xmap"], scanBean)
            else:
                print "This scan will use the Xspress detector"
                return self._createDetArray(["qexafs_counterTimer01", "qexafs_xspress", "QexafsFFI0"], scanBean)

    def isBeamCheck(self):
        return self.beamCheck
    
    def turnOnBeamCheck(self):
        self.beamCheck = True
        
    def turnOffBeamCheck(self):
        self.beamCheck=False
    
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
