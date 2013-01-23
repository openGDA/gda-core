from BeamlineParameters import JythonNameSpaceMapping
from gda.configuration.properties import LocalProperties
from gda.data.scan.datawriter import XasAsciiDataWriter, NexusExtraMetadataDataWriter
from gda.exafs.scan import BeanGroup, BeanGroups
from gda.exafs.scan import ScanStartedMessage
from gda.exafs.scan import RepetitionsProperties
from gda.factory import Finder
from java.lang import InterruptedException
from java.lang import System
from gda.jython import ScriptBase
from gda.jython.scriptcontroller.event import ScanCreationEvent, ScanFinishEvent, ScriptProgressEvent
from gda.jython.scriptcontroller.logging import XasProgressUpdater
from gda.jython.scriptcontroller.logging import LoggingScriptController
from gda.jython.scriptcontroller.logging import XasLoggingMessage
from gda.scan import ScanBase, ContinuousScan
from scan import Scan
from exafs_environment import ExafsEnvironment
from gda.epics import CAClient
from time import sleep

class QexafsScan(Scan):
    
    def __init__(self,loggingcontroller, detectorPreparer, samplePreparer, outputPreparer, energy_scannable, ion_chambers_scannable):
        Scan.__init__(self, loggingcontroller,detectorPreparer, samplePreparer, outputPreparer,None)
        self.energy_scannable = energy_scannable
        self.ion_chambers_scannable = ion_chambers_scannable
        #self.t = None
        self.beamCheck = True
        
    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, numRepetitions= -1, validation=True):
        xmlFolderName = ExafsEnvironment().getXMLFolder() + folderName + "/"

        sampleBean, scanBean, detectorBean, outputBean = self._createBeans(xmlFolderName, sampleFileName, scanFileName, detectorFileName, outputFileName) 
        controller = Finder.getInstance().find("ExafsScriptObserver")
        outputBean.setAsciiFileName(sampleBean.getName())
        beanGroup = self._createBeanGroup(controller, xmlFolderName, folderName, sampleBean, detectorBean, outputBean, scanBean)
        # work out which detectors to use (they will need to have been configured already by the GUI)
        detectorList = self._getQEXAFSDetectors(detectorBean, outputBean, scanBean) 
        print "detectors to be used:", str(detectorList)
        
        
        # send initial message to the log
        from gda.jython.scriptcontroller.logging import LoggingScriptController
        from gda.jython.scriptcontroller.logging import XasLoggingMessage
        loggingcontroller   = Finder.getInstance().find("XASLoggingScriptController")
        scriptType = "Qexafs"
        unique_id  = LoggingScriptController.createUniqueID(scriptType);
        outputFolder = outputBean.getAsciiDirectory()+ "/" + outputBean.getAsciiFileName()

        print "Starting Qexafs scan..."
        print ""
        print "Output to",xmlFolderName
        print ""

        # reset the properties used to control repetition behaviour
        LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY,"false")
        LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
        LocalProperties.set(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY,str(numRepetitions))
        repetitionNumber = 0
        timeRepetitionsStarted = System.currentTimeMillis();
        
        try:
            while True:
                repetitionNumber+= 1
                self.detectorPreparer.prepare(detectorBean, outputBean, xmlFolderName)
                self.samplePreparer.prepare(sampleBean)
                self.outputPreparer.prepare(outputBean)
                beanGroup.setScanNumber(repetitionNumber)
                XasAsciiDataWriter.setBeanGroup(beanGroup)
                if len(outputBean.getCheckedSignalList()) > 0:
                    print "Signal parameters not available with QEXAFS"
                if self.energy_scannable == None:
                    raise "No object for controlling energy during QEXAFS found! Expected qexafs_energy (or scannable1 for testing)"
                initial_energy = scanBean.getInitialEnergy()
                final_energy = scanBean.getFinalEnergy()
                step_size = scanBean.getStepSize()
                import math
                numberPoints = int(math.ceil((final_energy-initial_energy)/step_size))
                self._runScript(outputBean.getBeforeScriptName())
                scan_time = scanBean.getTime()

                initialPercent = str(int((float(repetitionNumber - 1) / float(numRepetitions)) * 100)) + "%" 

                logmsg = XasLoggingMessage(unique_id, scriptType, "Starting "+scriptType+" scan...", str(repetitionNumber), str(numRepetitions), initialPercent,str(0),str(0),beanGroup.getScan(),outputFolder)
               
                loggingcontroller.update(None,logmsg)
                loggingcontroller.update(None,ScanStartedMessage(scanBean,detectorBean))

                ### cirrus test
                #import threading
                #import datetime
                #from time import sleep
                #from gda.data import PathConstructor
                #from cirrus import ThreadClass

                #finder = Finder.getInstance()
                #cirrus=finder.find("cirrus")
                #cirrus.setMasses([2, 28, 32])
                #energyScannable = finder.find("qexafs_energy")
                #self.t = ThreadClass(cirrus, energyScannable, initial_energy, final_energy, "cirrus_scan.dat")
                #self.t.setName("cirrus")
                #self.t.start()
                ###

                loggingbean = XasProgressUpdater(loggingcontroller,logmsg,timeRepetitionsStarted)
            
                #3=closed
                if self.beamCheck==True:
                    feAbsPV = "FE18B-RS-ABSB-02:STA"
                    feAbsStatus = int(CAClient().caget(feAbsPV))
                    print "feAbsStatus=",feAbsStatus
                    while feAbsStatus!=1 and self.beamCheck==True:
                        feAbsStatus = int(CAClient().caget(feAbsPV))
                        print "Checking whether front end absorber is open. Currently "+str(feAbsStatus)
                        sleep(2)
            
                print "running QEXAFS scan:", self.energy_scannable.getName(), scanBean.getInitialEnergy(), scanBean.getFinalEnergy(), numberPoints, scan_time, detectorList
                controller.update(None, ScriptProgressEvent("Running QEXAFS scan"))
                thisscan = ContinuousScan(self.energy_scannable , scanBean.getInitialEnergy(), scanBean.getFinalEnergy(), numberPoints, scan_time, detectorList)
                controller.update(None, ScanCreationEvent(thisscan.getName()))
                try:
                    if numRepetitions > 1:
                        print ""
                        print "Starting repetition", str(repetitionNumber),"of",numRepetitions
                    loggingbean.atScanStart()
                    thisscan.runScan()  
                    controller.update(None, ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK));
                    loggingbean.atScanEnd()            
                except InterruptedException, e:
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
                    loggingbean.atCommandFailure()
                    raise
                    
                # run the after scan script
                self._runScript(outputBean.getAfterScriptName())
                
                #check if halt after current repetition set to true
                if numRepetitions > 1 and LocalProperties.get(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY) == "true":
                    print "Paused scan after repetition",str(repetitionNumber),". To resume the scan, press the Start button in the Command Queue view. To abort this scan, press the Skip Task button."
                    LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY,"false")
                    Finder.getInstance().find("commandQueueProcessor").pause(500);
                    ScriptBase.checkForPauses()
                
                #check if the number of repetitions has been altered and we should now end the loop
                numRepsFromProperty = int(LocalProperties.get(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY))
                if numRepsFromProperty != numRepetitions and numRepsFromProperty <= (repetitionNumber):
                    print "The number of repetitions has been reset to",str(numRepsFromProperty), ". As",str(repetitionNumber),"repetitions have been completed this scan will now end."
                    break
                elif numRepsFromProperty <= (repetitionNumber):
                    break
       
        finally:    
            # repetition loop completed, so reset things
            if (self.beamlineReverter != None):
                self.beamlineReverter.scanCompleted() #NexusExtraMetadataDataWriter.removeAllMetadataEntries() for I20
            LocalProperties.set("gda.scan.useScanPlotSettings", "false")
            LocalProperties.set("gda.plot.ScanPlotSettings.fromUserList", "false")
            XasAsciiDataWriter.setBeanGroup(None)
            #remove added metadata from default metadata list to avoid multiple instances of the same metadata
            jython_mapper = JythonNameSpaceMapping()
            if (jython_mapper.original_header != None):
                original_header=jython_mapper.original_header[:]
                Finder.getInstance().find("datawriterconfig").setHeader(original_header)
            #self.t.stop

 
    def _getNumberOfFrames(self, detectorBean, scanBean):
         # work out the number of frames to collect
        numberPoints = 0
        if detectorBean.getExperimentType() == "Fluorescence" and detectorBean.getFluorescenceParameters().getDetectorType() == "Germanium":
            maxPoints = self.ion_chambers_scannable.maximumReadFrames()
            if scanBean.isChooseNumberPoints():
                return maxPoints
            else:
                numberPoints = scanBean.getNumberPoints()
                if numberPoints > maxPoints:
                    raise "Too many frames for the given detector configuration."
                return numberPoints
        # for ion chambers only use a default value
        elif scanBean.isChooseNumberPoints():
            print "using default number of frames: 1000"
            return 1000 # a default value as user has selected max possible but is only using the ion chambers (so max will be very high)
        # have a limit anyway of 4096
        else:
            numberPoints = scanBean.getNumberPoints()
            if numberPoints > 4096:
                    raise "Too many frames for the given detector configuration."
            return numberPoints
 
    def _getQEXAFSDetectors(self, detectorBean, outputBean, scanBean):
        expt_type = detectorBean.getExperimentType()
        detectorList = []
        if expt_type == "Transmission":
            return self._createDetArray(["qexafs_counterTimer01"], scanBean)
        else:
            if detectorBean.getFluorescenceParameters().getDetectorType() == "Silicon":
                return self._createDetArray(["qexafs_counterTimer01", "qexafs_xmap", "VortexQexafsFFI0"], scanBean)
            else:
                return self._createDetArray(["qexafs_counterTimer01", "qexafs_xspress", "QexafsFFI0"], scanBean)
        
        

    def _createBeanGroup(self, controller, xmlFolderName, folderName, sampleBean, detectorBean, outputBean, scanBean):
        # give the beans to the xasdatawriter class to help define the folders/filenames 
        beanGroup = BeanGroup()
        beanGroup.setController(controller)
        beanGroup.setScriptFolder(xmlFolderName)
        beanGroup.setExperimentFolderName(folderName)
        #beanGroup.setScanNumber(scanNumber)
        beanGroup.setSample(sampleBean)
        beanGroup.setDetector(detectorBean)
        beanGroup.setOutput(outputBean)
        beanGroup.setScan(scanBean)
        return beanGroup

    def isBeamCheck(self):
        return self.beamCheck
    
    def turnOnBeamCheck(self):
        self.beamCheck = True
        
    def turnOffBeamCheck(self):
        self.beamCheck=False