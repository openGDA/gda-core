import array

from java.lang import InterruptedException
from java.lang import System
from xas_scan import XasScan
from exafs_environment import ExafsEnvironment
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
from uk.ac.gda.beans.exafs import XesScanParameters
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
                    Finder.getInstance().find("commandQueueProcessor").pause(500);
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
                
            print "**********************************"
            
            # remove extra columns from ionchambers output
            self.jython_mapper.topupChecker.collectionTime = 0.0
            self.jython_mapper.ionchambers.setOutputLogValues(False) 
            ScriptBase.checkForPauses()
         
        
    def _beforeEachRepetition(self,beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller, repNum):
        times = []
        if isinstance(beanGroup.getScan(),XasScanParameters):
            times = ExafsScanPointCreator.getScanTimeArray(beanGroup.getScan())
        elif isinstance(beanGroup.getScan(),XanesScanParameters):
            times = XanesScanPointCreator.getScanTimeArray(beanGroup.getScan())
        if len(times) > 0:
            self.jython_mapper.ionchambers.setTimes(times)
            self.log( "Setting detector frame times, using array of length",str(len(times)) + "...")
            ScriptBase.checkForPauses()
        return
        

    def setDetectorCorrectionParameters(self,beanGroup):
        scanObj = beanGroup.getScan()
        dtEnergy = 0.0
        if isinstance(scanObj,XasScanParameters):
            edge = scanObj.getEdge()
            element = scanObj.getElement()
            elementObj = Element.getElement(element)
            dtEnergy = self._getEmissionEnergy(elementObj,edge)
            dtEnergy /= 1000 # convert from eV to keV
            print "Setting Ge detector deadtime calculation energy to be",str(dtEnergy),"keV based on element",element,"and edge",edge
        elif isinstance(scanObj,XanesScanParameters):
            edge = scanObj.getEdge()
            element = scanObj.getElement()
            elementObj = Element.getElement(element)
            dtEnergy = self._getEmissionEnergy(elementObj,edge)
            dtEnergy /= 1000 # convert from eV to keV
            print "Setting Ge detector deadtime calculation energy to be",str(dtEnergy),"keV based on element",element,"and edge",edge
        else :
            dtEnergy = scanObj.getFinalEnergy() 
            dtEnergy /= 1000 # convert from eV to keV
        
        self.jython_mapper.xspress2system.setDeadtimeCalculationEnergy(dtEnergy)
 
    def _getEmissionEnergy(self,elementObj,edge):
        if str(edge) == "K":
            return elementObj.getEmissionEnergy("Ka1")
        elif str(edge) == "L1":
            return elementObj.getEmissionEnergy("La1")
        elif str(edge) == "L2":
            return elementObj.getEmissionEnergy("La1")
        elif str(edge) == "L3":
            return elementObj.getEmissionEnergy("La1")
        elif str(edge) == "M1":
            return elementObj.getEmissionEnergy("Ma1")
        elif str(edge) == "M2":
            return elementObj.getEmissionEnergy("Ma1")
        elif str(edge) == "M3":
            return elementObj.getEmissionEnergy("Ma1")
        elif str(edge) == "M4":
            return elementObj.getEmissionEnergy("Ma1")
        elif str(edge) == "M5":
            return elementObj.getEmissionEnergy("Ma1")
        else:
            return elementObj.getEmissionEnergy("Ka1")
    
    def setUpIonChambers(self,beanGroup):    
        # determine max collection time
        scanBean = beanGroup.getScan()
        maxTime = 0;
        if isinstance(scanBean,XanesScanParameters):
            for region in scanBean.getRegions():
                if region.getTime() > maxTime:
                    maxTime = region.getTime()
                
        elif isinstance(scanBean,XasScanParameters):
            if scanBean.getEdgeTime() > maxTime:
                maxTime = scanBean.getEdgeTime()
            if scanBean.getExafsToTime() > maxTime:
                maxTime = scanBean.getExafsToTime()
            if scanBean.getExafsFromTime() > maxTime:
                maxTime = scanBean.getExafsFromTime()
            if scanBean.getExafsTime() > maxTime:
                maxTime = scanBean.getExafsTime()
            if scanBean.getPreEdgeTime() > maxTime:
                maxTime = scanBean.getPreEdgeTime()
    
        # set dark current time and handle any errors here
        if maxTime > 0:
            self.log( "Setting ionchambers dark current collectiom time to",str(maxTime),"s.")
            self.jython_mapper.ionchambers.setDarkCurrentCollectionTime(maxTime)
            self.jython_mapper.I1.setDarkCurrentCollectionTime(maxTime)
            
            topupPauseTime = maxTime + self.jython_mapper.topupChecker.tolerance
            print "Setting the topup checker to pause scans for",topupPauseTime,"s before topup"
            self.jython_mapper.topupChecker.collectionTime = maxTime
            
                    
    def _createTemperaturesArrayFromString(self,tempString):
        
        parts = tempString.split(",")
        dblArray = array.array('d')
        for part in parts:
            dblArray.append(float(part))
        return dblArray
        
    def _configureCryostat(self, cryoStatParameters):
        cryostat_scannable = self.finder.find("cryostat")
        if LocalProperties.get("gda.mode") != 'dummy':
            cryostat_scannable.setupFromBean(cryoStatParameters)
        
class I20XesScan(XasScan):
    
    def __init__(self, loggingcontroller, detectorPreparer, samplePreparer, outputPreparer, beamlineReverter):
        self.detectorPreparer = detectorPreparer
        self.samplePreparer = samplePreparer
        self.outputPreparer = outputPreparer
        self.loggingcontroller = loggingcontroller
        self.beamlineReverter = beamlineReverter
        self.jython_mapper = JythonNameSpaceMapping()
        self.finder = Finder.getInstance()

        
    def __call__ (self,sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, numRepetitions= 1, validation=True):

        # Create the beans from the file names
        xmlFolderName = ExafsEnvironment().getXMLFolder() + folderName + "/"
        sampleBean = BeansFactory.getBeanObject(xmlFolderName, sampleFileName)
        xesScanBean = BeansFactory.getBeanObject(xmlFolderName, scanFileName)
        detectorBean = BeansFactory.getBeanObject(xmlFolderName, detectorFileName)
        outputBean = BeansFactory.getBeanObject(xmlFolderName, outputFileName)

        self.finder_mapper = FinderNameMapping()
        self.jython_mapper = JythonNameSpaceMapping()
        loggingcontroller = Finder.getInstance().find("XASLoggingScriptController")

        # create unique ID for this scan (all repetitions will share the same ID)
        scriptType = "Xes"
        scan_unique_id = LoggingScriptController.createUniqueID(scriptType);


        # give the beans to the xasdatawriter class to help define the folders/filenames 
        beanGroup = BeanGroup()
        beanGroup.setController(Finder.getInstance().find("ExafsScriptObserver"))
        beanGroup.setScriptFolder(xmlFolderName)
        beanGroup.setExperimentFolderName(folderName)
        beanGroup.setSample(sampleBean)
        beanGroup.setDetector(detectorBean)
        beanGroup.setOutput(outputBean)
        beanGroup.setScan(xesScanBean)
        
        # if get here then its an XES step scan
        self._doLooping(beanGroup,folderName,numRepetitions, validation,scan_unique_id)
        
    def _doLooping(self,beanGroup,folderName,numRepetitions, validation,scan_unique_id):

        loggingcontroller = Finder.getInstance().find("XASLoggingScriptController")
        
        # ion chambers
        self.setUpIonChambers(beanGroup)

        if beanGroup.getSample().getSampleEnvironment() == I20SampleParameters.SAMPLE_ENV_XES[1] :
            
            sampleStageParameters = beanGroup.getSample().getRoomTemperatureParameters()
            for i in range(0,4):
                doUse = sampleStageParameters.getUses()[i]
               
                if not doUse:
                    continue
                    
                x = sampleStageParameters.getXs()[i]
                y = sampleStageParameters.getYs()[i]
                z = sampleStageParameters.getZs()[i]
                rotation = sampleStageParameters.getRotations()[i]
                finerotation = sampleStageParameters.getFineRotations()[i]
                samplename = sampleStageParameters.getSampleNames()[i]
                sampledescription = sampleStageParameters.getSampleDescriptions()[i]
                
                samx = self.finder.find("sample_x")
                samy = self.finder.find("sample_y")
                samz = self.finder.find("sample_z")
                samrot = self.finder.find("sample_rot")
                samfinerot = self.finder.find("sample_fine_rot")
                
                if samx == None or samy ==None or samz == None or samrot == None or samfinerot == None:
                    raise DeviceException("I20 XES scan script - could not find all sample stage motors!")
                
                
                self.log( "Moving sample stage to",x,y,z,rotation,finerotation,"...")
                samx.asynchronousMoveTo(x)
                samy.asynchronousMoveTo(y)
                samz.asynchronousMoveTo(z)
                samrot.asynchronousMoveTo(rotation)
                samfinerot.asynchronousMoveTo(finerotation)
                samx.waitWhileBusy()
                samy.waitWhileBusy()
                samz.waitWhileBusy()
                samrot.waitWhileBusy()
                samfinerot.waitWhileBusy()
                self.log( "Sample stage move complete.\n")
                ScriptBase.checkForPauses()
                
                
                # change the strings in the filewriter so that the ascii filename changes
                beanGroup.getSample().setName(samplename)
                beanGroup.getSample().setDescriptions([sampledescription])

                
                self._doScan(beanGroup,folderName,numRepetitions, validation,scan_unique_id)
                
        else:
            self._doScan(beanGroup,folderName,numRepetitions, validation,scan_unique_id)


    def _doScan(self,beanGroup,folderName,numRepetitions, validation,scan_unique_id):

        loggingcontroller = Finder.getInstance().find("XASLoggingScriptController")
        xmlFolderName = ExafsEnvironment().getXMLFolder() + folderName + "/"

        # now that the scan has been defined, run it in a loop
        
        
        # work out which detectors to use (they will need to have been configured already by the GUI)
        detectorList = self._getDetectors(beanGroup.getDetector(), beanGroup.getScan()) 

        # get the relevant objects from the namespace
        xes_energy = self.jython_mapper.XESEnergy
        mono_energy = self.jython_mapper.bragg1
        analyserAngle = self.jython_mapper.XESBragg
        #L = jython_mapper.xtal_x

        scanType = beanGroup.getScan().getScanType()
        args = []
        
        if analyserAngle.isBusy():
            self.log( "XESBragg is moving. Waiting for it to finish...")
            analyserAngle.waitWhileBusy()
            self.log( "XESBragg move completed.")

        
        if scanType == XesScanParameters.SCAN_XES_FIXED_MONO:
            self.log( "Starting XES scan with fixed mono...")
            print""
            self.log( "Output to",xmlFolderName)
#            print "switching data output format to XesAsciiNexusDataWriter"
#            LocalProperties.set("gda.data.scan.datawriter.dataFormat","XesAsciiNexusDataWriter")
            args += [xes_energy, beanGroup.getScan().getXesInitialEnergy(), beanGroup.getScan().getXesFinalEnergy(), beanGroup.getScan().getXesStepSize(), mono_energy, beanGroup.getScan().getMonoEnergy()]
            
            # I20 always moves things back to initial positions after each scan. To save time, move mono to intial position here
            self.log( "Moving spectrometer to initial position...")
            xes_energy.moveTo(beanGroup.getScan().getXesInitialEnergy())
            self.log( "Move done.")

    
        elif scanType == XesScanParameters.SCAN_XES_SCAN_MONO:
            self.log( "Starting 2D scan over XES and mono...")
            print""
            self.log( "Output to",xmlFolderName)
#            print "switching data output format to XesAsciiNexusDataWriter"
#            LocalProperties.set("gda.data.scan.datawriter.dataFormat","XesAsciiNexusDataWriter")

             # create scannable which will control 2D plotting in this mode
#            self.jython_mapper.twodplotter.setX_colName(xes_energy.getInputNames()[0])
#            self.jython_mapper.twodplotter.setY_colName(mono_energy.getInputNames()[0])
#            self.jython_mapper.twodplotter.setZ_colName(self.finder_mapper.xmapMca.getExtraNames()[0])
            self.jython_mapper.twodplotter.setX_colName("XESEnergy")
            self.jython_mapper.twodplotter.setY_colName("bragg1")
            self.jython_mapper.twodplotter.setZ_colName("FFI0")
            # note that users will have to open a 'plot 1' view or use the XESPlot perspective for this to work

            ef_args = [xes_energy, beanGroup.getScan().getXesInitialEnergy(), beanGroup.getScan().getXesFinalEnergy(), beanGroup.getScan().getXesStepSize()]
            e0_args = [mono_energy, beanGroup.getScan().getMonoInitialEnergy(), beanGroup.getScan().getMonoFinalEnergy(), beanGroup.getScan().getMonoStepSize()]
            
            if beanGroup.getScan().getLoopChoice() == XesScanParameters.LOOPOPTIONS[0]:
                args += ef_args + e0_args
            else:
                args += e0_args + ef_args
            args += [self.jython_mapper.twodplotter]
            
            # I20 always moves things back to initial positions after ecah scan. To save time, move mono to intial position here
            self.log( "Moving mono and spectrometer to initial positions...")
            xes_energy(beanGroup.getScan().getXesInitialEnergy())
            mono_energy(beanGroup.getScan().getMonoInitialEnergy())
            xes_energy.waitWhileBusy()
            mono_energy.waitWhileBusy()
            self.log( "Moves done.")


        elif scanType == XesScanParameters.FIXED_XES_SCAN_XAS:
            self.log( "Starting XAS scan with fixed analyser energy...")
            print""
            self.log( "Output to",xmlFolderName)

            # add xes_energy, analyserAngle to the defaults and then call the xas command
            xas_scanfilename = beanGroup.getScan().getScanFileName()

            self.log( "moving XES analyser stage to collect at", beanGroup.getScan().getXesEnergy())
            initialXESEnergy = xes_energy()
            xes_energy.waitWhileBusy()
            xes_energy(beanGroup.getScan().getXesEnergy())
            ScannableCommands.add_default([xes_energy,analyserAngle])
            

            try:
                self.outputPreparer.mode = "xes"
                self.jython_mapper.xas(beanGroup.getSample(), xas_scanfilename, beanGroup.getDetector(), beanGroup.getOutput(), folderName, numRepetitions, validation)
            finally:
                self.log( "cleaning up scan defaults")
                self.outputPreparer.mode = "xas"
                ScannableCommands.remove_default([xes_energy,analyserAngle])
                xes_energy(initialXESEnergy)
            return

        elif scanType == XesScanParameters.FIXED_XES_SCAN_XANES:
            self.log( "Starting XANES scan with fixed analyser energy...")
            print""
            self.log( "Output to",xmlFolderName)
            # add xes_energy, analyserAngle, to the signal parameters bean and then call the xanes command
            xanes_scanfilename = beanGroup.getScan().getScanFileName()

            self.log( "moving XES analyser stage to collect at", beanGroup.getScan().getXesEnergy())
            initialXESEnergy = xes_energy()
            xes_energy.waitWhileBusy()
            xes_energy(beanGroup.getScan().getXesEnergy())
            ScannableCommands.add_default([xes_energy,analyserAngle])

            try:
                self.outputPreparer.mode = "xes"
                self.jython_mapper.xanes(beanGroup.getSample(), xanes_scanfilename, beanGroup.getDetector(), beanGroup.getOutput(), folderName, numRepetitions, validation)
            finally:
                self.log( "cleaning up scan defaults")
                self.outputPreparer.mode = "xas"
                ScannableCommands.remove_default([xes_energy,analyserAngle])
                xes_energy(initialXESEnergy)
            return
        else:
            raise "scan type in XES Scan Parameters bean/xml not acceptable"

        self.log( "Setting up the detectors...")
        self.detectorPreparer.prepare(beanGroup.getDetector(), beanGroup.getOutput(), xmlFolderName)
        self.log( "Vortex and ionchambers configured.")
        sampleScannables = self.samplePreparer.prepare(beanGroup.getSample())
        outputScannables = self.outputPreparer.prepare(beanGroup.getOutput(), beanGroup.getScan())
        scanPlotSettings = self.outputPreparer.getPlotSettings(beanGroup)

        # run the before scan script
        self._runScript(beanGroup.getOutput().getBeforeScriptName())

        args += [analyserAngle]
        args += detectorList 
        args += [beanGroup.getScan().getXesIntegrationTime()]
        signalParameters = self._getSignalList(beanGroup.getOutput())
        if len(signalParameters) > 0:
            args += signalParameters

        # reset the properties used to control repetition behaviour
        LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY,"false")
        LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
        LocalProperties.set(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY,str(numRepetitions))
        repetitionNumber = 0
        timeRepetitionsStarted = System.currentTimeMillis();

        scriptType = "Xes"
        # set the dark current and integration time for all detectors
        itime = beanGroup.getScan().getXesIntegrationTime()
        for det in detectorList:
            det.setCollectionTime(itime)
        
        try:
            while True:
                repetitionNumber+= 1
                beanGroup.setScanNumber(repetitionNumber)
                XasAsciiDataWriter.setBeanGroup(beanGroup)

            # send out initial messages for logging and display to user
                outputFolder = beanGroup.getOutput().getAsciiDirectory()+ "/" + beanGroup.getOutput().getAsciiFileName()
                #print "Starting "+scriptType+" scan...", str(repetitionNumber)
                initialPercent = str(int((float(repetitionNumber - 1) / float(numRepetitions)) * 100)) + "%" 
                logmsg = XasLoggingMessage(scan_unique_id, scriptType, "Starting "+scriptType+" scan...", str(repetitionNumber), str(numRepetitions), initialPercent,str(0),str(0),beanGroup.getScan(),outputFolder)
                loggingcontroller.update(None,logmsg)
                loggingcontroller.update(None,ScanStartedMessage(beanGroup.getScan(),beanGroup.getDetector())) # informs parts of the UI about current scan
                loggingbean = XasProgressUpdater(loggingcontroller,logmsg,timeRepetitionsStarted)
                argsForThisScan = args + [loggingbean]
                try:
                    loggingcontroller.update(None, ScriptProgressEvent("Running scan"))
                    ScanBase.interrupted = False
                    if numRepetitions > 1:
                        print ""
                    thisscan = ConcurrentScan(argsForThisScan)
                    if numRepetitions != 1:
                        self.log( "Starting repetition", str(repetitionNumber),"of",numRepetitions)

                    thisscan = self._setUpDataWriter(thisscan,beanGroup)
                    thisscan.setReturnScannablesToOrginalPositions(False)
                    loggingcontroller.update(None, ScanCreationEvent(thisscan.getName()))
                    thisscan.runScan()
                except InterruptedException, e:
                    ScanBase.interrupted = False
                    if LocalProperties.get(RepetitionsProperties.SKIP_REPETITION_PROPERTY) == "true":
                        LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
                    # only wanted to skip this repetition, so absorb the exception and continue the loop
                        if numRepetitions > 1:
                            self.log( "Repetition", str(repetitionNumber),"skipped.")
                    else:
                        print e
                        raise # any other exception we are not expecting so raise whatever this is to abort the script                       
                #update observers
                loggingcontroller.update(None, ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK));

                # run the after scan script
                self._runScript(beanGroup.getOutput().getAfterScriptName())

                #check if halt after current repetition set to true
                if numRepetitions > 1 and LocalProperties.get(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY) == "true":
                    self.log( "Paused scan after repetition",str(repetitionNumber),". To resume the scan, press the Start button in the Command Queue view. To abort this scan, press the Skip Task button.")
                    LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY,"false")
                    Finder.getInstance().find("commandQueueProcessor").pause(500);
                    ScriptBase.checkForPauses()
                
                #check if the number of repetitions has been altered and we should now end the loop
                numRepsFromProperty = int(LocalProperties.get(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY))
                if numRepsFromProperty != numRepetitions and numRepsFromProperty <= (repetitionNumber):
                    self.log( "The number of repetitions has been reset to",str(numRepsFromProperty), ". As",str(repetitionNumber),"repetitions have been completed this scan will now end.")
                    break
                elif numRepsFromProperty <= (repetitionNumber):
                    break
                numRepetitions = numRepsFromProperty
        finally:
#            LocalProperties.set("gda.data.scan.datawriter.dataFormat", originalDataFormat)
            # make sure the plotter is switched off
            self.jython_mapper.twodplotter.atScanEnd()
            self.jython_mapper.ionchambers.setOutputLogValues(False) 
            XasAsciiDataWriter.setBeanGroup(None)
            

    def setUpIonChambers(self,beanGroup):
        # get ion chmabers
        ct = self.jython_mapper.I1
        ct.setDarkCurrentCollectionTime(beanGroup.getScan().getXesIntegrationTime())

