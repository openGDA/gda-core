import array
from java.lang import InterruptedException, System
from xas_scan import XasScan
from BeamlineParameters import JythonNameSpaceMapping, FinderNameMapping
from exafsscripts.exafs.i20.I20SampleIterators import XASXANES_Roomtemp_Iterator, XES_Roomtemp_Iterator, XASXANES_Cryostat_Iterator
from gda.configuration.properties import LocalProperties
from gda.data.scan.datawriter import  XasAsciiDataWriter
from gda.device import DeviceException
from gda.exafs.scan import BeanGroup, BeanGroups, RepetitionsProperties,ScanStartedMessage, ExafsScanPointCreator,XanesScanPointCreator
from gda.factory import Finder
from gda.jython import ScriptBase
from gda.jython.commands import ScannableCommands
from gda.jython.scriptcontroller.event import ScanCreationEvent, ScanFinishEvent, ScriptProgressEvent
from gda.jython.scriptcontroller.logging import LoggingScriptController, XasLoggingMessage, XasProgressUpdater
from gda.scan import ScanBase, ContinuousScan, ConcurrentScan
from gda.util import Element
from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.beans.exafs import XanesScanParameters, XasScanParameters
from uk.ac.gda.beans.exafs.i20 import I20SampleParameters, CryostatParameters
from uk.ac.gda.doe import DOEUtils

class I20XasScan(XasScan):

    def _doLooping(self,beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller, sampleBean, scanBean, detectorBean, outputBean):

        # configure deadtime only for i20
        if beanGroup.getDetector().getExperimentType() == 'Fluorescence':
            detType = beanGroup.getDetector().getFluorescenceParameters().getDetectorType()
            if detType == "Germanium":
                self.setDetectorCorrectionParameters(beanGroup)
                ScriptBase.checkForPauses()
            
        self.setUpIonChambers(beanGroup)
        
        if beanGroup.getSample().getUseSampleWheel():
            self.moveSampleWheel()
        
        ScriptBase.checkForPauses()
            
        # I20 always moves things back to initial positions after each scan. To save time, move mono to initial position here
        self.moveMonoToInitialPosition(scanBean)

        self.createIterator()
        energy_scannable.waitWhileBusy()
        
        self.setQueueProperties(numRepetitions)
        repetitionNumber = 0
        timeRepetitionsStarted = System.currentTimeMillis();
            
        try:
            while True:
                repetitionNumber+= 1
                beanGroup.setScanNumber(repetitionNumber)
                XasAsciiDataWriter.setBeanGroup(beanGroup)
                self._beforeEachRepetition(beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller,repetitionNumber)
                outputFolder = self.resolveOutputFolder(outputBean)
                
                try:
                    # inner sample environment loop here
                    if self.iterator == None:
                        logmsg = self.getLogMessage(numRepetitions, repetitionNumber, timeRepetitionsStarted, scan_unique_id, scriptType, scanBean, outputFolder)
                        self.printRepetition(numRepetitions, repetitionNumber, scriptType)
                        self._doScan(beanGroup,scriptType,scan_unique_id, xmlFolderName, controller,logmsg,timeRepetitionsStarted)
                    else :
                        self.iterator.resetIterator()
                        num_sample_repeats = int(self.iterator.getNumberOfRepeats())
                        total_repeats = num_sample_repeats * numRepetitions
                        for i in range(num_sample_repeats):
                            self.iterator.moveToNext()
                            # as this is wiped at the end of each scan. It is harmless to call this multiple times
                            XasAsciiDataWriter.setBeanGroup(beanGroup)
                            this_repeat = repetitionNumber + i
                            initialPercent = self.calcInitialPercent(total_repeats, this_repeat)
                            timeSinceRepetitionsStarted = System.currentTimeMillis() - timeRepetitionsStarted
                            logmsg = XasLoggingMessage(scan_unique_id, scriptType, "Starting "+scriptType+" scan...", str(repetitionNumber), str(numRepetitions), str(i+1), str(num_sample_repeats), initialPercent,str(0),str(timeSinceRepetitionsStarted),beanGroup.getScan(),outputFolder)
                            self._doScan(beanGroup,scriptType,scan_unique_id, xmlFolderName, controller,logmsg,timeRepetitionsStarted)

                except InterruptedException, e:
                    self.handleScanInterrupt(numRepetitions, repetitionNumber)

                self._runScript(beanGroup.getOutput().getAfterScriptName())# run the after scan script
                self.checkForPause(numRepetitions)
                finished=self.checkIfRepetitionsFinished(numRepetitions, repetitionNumber)
                if finished is True:
                    break
                numRepetitions = numRepsFromProperty
        finally:    
            energy_scannable.stop()
            # repetition loop completed, so reset things
            self.setQueuePropertiesEnd()
            XasAsciiDataWriter.setBeanGroup(None)
            self.restoreHeader()

            # remove extra columns from ionchambers output
            self.jython_mapper.topupChecker.collectionTime = 0.0
            self.jython_mapper.ionchambers.setOutputLogValues(False) 
            ScriptBase.checkForPauses()
            
    def moveMonoToInitialPosition(self, scanBean):
        if energy_scannable != None and isinstance(scanBean,XasScanParameters):
            intialPosition = scanBean.getInitialEnergy()
        elif self.energy_scannable != None and isinstance(scanBean,XanesScanParameters): 
            intialPosition = scanBean.getRegions().get(0).getEnergy()
        self.energy_scannable.waitWhileBusy()
        self.energy_scannable.asynchronousMoveTo(intialPosition)
        self.log( "Moving mono to initial position...")
    
    def moveSampleWheel(self):
        filter = beanGroup.getSample().getSampleWheelPosition()
        self.log( "Setting filter wheel to",filter,"...")
        self.jython_mapper.filterwheel(filter)
    
     # XAS / XANES room temperature sample stage  
    def createIterator(self):
        if beanGroup.getDetector().getExperimentType() != 'XES' and beanGroup.getSample().getSampleEnvironment() == I20SampleParameters.SAMPLE_ENV[1] :
            self.iterator = XASXANES_Roomtemp_Iterator()
            self.iterator.setBeanGroup(beanGroup)
        # XES room temp sample stage
        elif beanGroup.getDetector().getExperimentType() == 'XES' and beanGroup.getSample().getSampleEnvironment() == I20SampleParameters.SAMPLE_ENV[1] :
            self.iterator = XES_Roomtemp_Iterator()
            self.iterator.setBeanGroup(beanGroup)
        #XAS/XANES cryostat
        elif beanGroup.getDetector().getExperimentType() != 'XES' and beanGroup.getSample().getSampleEnvironment() == I20SampleParameters.SAMPLE_ENV[2] :
            self.iterator = XASXANES_Cryostat_Iterator()
            self.iterator.setBeanGroup(beanGroup)
        else :
            self.iterator = None