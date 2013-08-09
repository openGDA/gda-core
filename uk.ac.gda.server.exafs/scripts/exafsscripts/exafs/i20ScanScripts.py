import array

from java.lang import InterruptedException
from java.lang import System
from xas_scan import XasScan
from BeamlineParameters import JythonNameSpaceMapping, FinderNameMapping
from exafsscripts.exafs.i20.I20SampleIterators import XASXANES_Roomtemp_Iterator, XES_Roomtemp_Iterator, XASXANES_Cryostat_Iterator

from gda.configuration.properties import LocalProperties
from gda.data.scan.datawriter import  XasAsciiDataWriter
from gda.device import DeviceException
from gda.exafs.scan import BeanGroup, BeanGroups
from gda.exafs.scan import RepetitionsProperties,ScanStartedMessage
from gda.exafs.scan import ExafsScanPointCreator,XanesScanPointCreator
from gda.factory import Finder
from gda.jython import ScriptBase
from gda.jython.commands import ScannableCommands
from gda.jython.scriptcontroller.event import ScanCreationEvent, ScanFinishEvent, ScriptProgressEvent
from gda.jython.scriptcontroller.logging import LoggingScriptController
from gda.jython.scriptcontroller.logging import XasLoggingMessage
from gda.jython.scriptcontroller.logging import XasProgressUpdater
from gda.scan import ScanBase, ContinuousScan, ConcurrentScan
from gda.util import Element
from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.beans.exafs import XanesScanParameters
from uk.ac.gda.beans.exafs import XasScanParameters
from uk.ac.gda.beans.exafs.i20 import I20SampleParameters, CryostatParameters
from uk.ac.gda.doe import DOEUtils

class I20XasScan(XasScan):
    
    def _doLooping(self,beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller):

        self.jython_mapper = JythonNameSpaceMapping()
        self.finder = Finder.getInstance()
        
        # fluo detector, if in use
        if beanGroup.getDetector().getExperimentType() == 'Fluorescence':
            detType = beanGroup.getDetector().getFluorescenceParameters().getDetectorType()
            if detType == "Germanium":
                self.setDetectorCorrectionParameters(beanGroup)
                ScriptBase.checkForPauses()
            
        # ion chambers
        self.setUpIonChambers(beanGroup)
        
        # reference (filter) wheel
        if beanGroup.getSample().getUseSampleWheel():
            filter = beanGroup.getSample().getSampleWheelPosition()
            self.log( "Setting filter wheel to",filter,"...")
            self.jython_mapper.filterwheel(filter)
            ScriptBase.checkForPauses()
            
        # I20 always moves things back to initial positions after each scan. To save time, move mono to initial position here
        scan =  beanGroup.getScan()
        energy_scannable_name = scan.getScannableName()
        energy_scannable = self.finder.find(energy_scannable_name)
        if energy_scannable != None and isinstance(scan,XasScanParameters):
            intialPosition = scan.getInitialEnergy()
            energy_scannable.waitWhileBusy()
            energy_scannable.asynchronousMoveTo(intialPosition)
            self.log( "Moving",energy_scannable_name, "to initial position...")
        elif energy_scannable != None and isinstance(scan,XanesScanParameters): 
            intialPosition = scan.getRegions().get(0).getEnergy()
            energy_scannable.waitWhileBusy()
            energy_scannable.asynchronousMoveTo(intialPosition)
            self.log( "Moving",energy_scannable_name, "to initial position...")

        # identify sample environment and create the iterator object

#       # XAS / XANES room temperature sample stage 
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
        
        energy_scannable.waitWhileBusy()
        LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY,"false")
        LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
        LocalProperties.set(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY,str(numRepetitions))
        repetitionNumber = 0
        timeRepetitionsStarted = System.currentTimeMillis();
            
        try:
            while True:
                repetitionNumber+= 1
                beanGroup.setScanNumber(repetitionNumber)
                XasAsciiDataWriter.setBeanGroup(beanGroup)
                self._beforeEachRepetition(beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller,repetitionNumber)
        
                try:
                    # inner sample environment loop here
                    if self.iterator == None:
                        outputFolder = beanGroup.getOutput().getAsciiDirectory()+ "/" + beanGroup.getOutput().getAsciiFileName()
                        initialPercent = str(int((float(repetitionNumber - 1) / float(numRepetitions)) * 100)) + "%" 
                        timeSinceRepetitionsStarted = System.currentTimeMillis() - timeRepetitionsStarted
                        logmsg = XasLoggingMessage(scan_unique_id, scriptType, "Starting "+scriptType+" scan...", str(repetitionNumber), str(numRepetitions),str(1),str(1), initialPercent,str(0),str(timeSinceRepetitionsStarted),beanGroup.getScan(),outputFolder)
                        if numRepetitions > 1:
                            print ""
                            self.log( "Starting repetition", str(repetitionNumber),"of",numRepetitions)
                        else:
                            print ""
                            self.log( "Starting "+scriptType+" scan...")
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
                            initialPercent = str(int((float(this_repeat - 1) / float(total_repeats)) * 100)) + "%" 
                            timeSinceRepetitionsStarted = System.currentTimeMillis() - timeRepetitionsStarted
                            outputFolder = beanGroup.getOutput().getAsciiDirectory()+ "/" + beanGroup.getOutput().getAsciiFileName()
                            logmsg = XasLoggingMessage(scan_unique_id, scriptType, "Starting "+scriptType+" scan...", str(repetitionNumber), str(numRepetitions), str(i+1), str(num_sample_repeats), initialPercent,str(0),str(timeSinceRepetitionsStarted),beanGroup.getScan(),outputFolder)
                            self._doScan(beanGroup,scriptType,scan_unique_id, xmlFolderName, controller,logmsg,timeRepetitionsStarted)
        
                except InterruptedException, e:
                    ScanBase.interrupted = False
                    if LocalProperties.get(RepetitionsProperties.SKIP_REPETITION_PROPERTY) == "true":
                        LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
                        # check if a panic stop has been issued, so the whole script should stop
                        if ScriptBase.isInterrupted():
                            ScriptBase.interrupted = False
                            raise e
                        # only wanted to skip this repetition, so absorb the exception and continue the loop
                        if numRepetitions > 1:
                            self.log( "Repetition", str(repetitionNumber),"skipped.")
                    else:
                        print e
                        raise # any other exception we are not expecting so raise whatever this is to abort the script
                        
                    
                # run the after scan script
                self._runScript(beanGroup.getOutput().getAfterScriptName())
                
                #check if halt after current repetition set to true
                if numRepetitions > 1 and LocalProperties.get(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY) == "true":
                    self.log( "Paused scan after repetition",str(repetitionNumber),". To resume the scan, press the Start button in the Command Queue view. To abort this scan, press the Skip Task button.")
                    self.commandQueueProcessor.pause(500);
                    LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY,"false")
                    ScriptBase.checkForPauses()
                
                #check if the number of repetitions has been altered and we should now end the loop
                numRepsFromProperty = int(LocalProperties.get(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY))
                if numRepsFromProperty != numRepetitions and numRepsFromProperty <= (repetitionNumber):
                    self.log( "The number of repetitions has been reset to",str(numRepsFromProperty), ". As",str(repetitionNumber),"repetitions have been completed this scan will now end.")
                    break
                elif numRepsFromProperty <= (repetitionNumber):
                    # normal end to loop
                    break
                numRepetitions = numRepsFromProperty
        finally:    
            energy_scannable.stop()
            
            # repetition loop completed, so reset things
            # TODO remove metadata enteries
            LocalProperties.set("gda.scan.useScanPlotSettings", "false")
            LocalProperties.set("gda.plot.ScanPlotSettings.fromUserList", "false")
            XasAsciiDataWriter.setBeanGroup(None)
            
            #remove added metadata from default metadata list to avoid multiple instances of the same metadata
            jython_mapper = JythonNameSpaceMapping()
            if (jython_mapper.original_header != None):
                original_header=jython_mapper.original_header[:]
                self.datawriterconfig.setHeader(original_header)
                
            print "**********************************"
            
            # remove extra columns from ionchambers output
            self.jython_mapper.topupChecker.collectionTime = 0.0
            self.jython_mapper.ionchambers.setOutputLogValues(False) 
            ScriptBase.checkForPauses()