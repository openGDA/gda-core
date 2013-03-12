from BeamlineParameters import JythonNameSpaceMapping
from gda.configuration.properties import LocalProperties
from gda.data.scan.datawriter import XasAsciiDataWriter, NexusExtraMetadataDataWriter
from gda.exafs.scan import ExafsScanPointCreator, XanesScanPointCreator
from gda.exafs.scan import BeanGroup, BeanGroups
from gda.exafs.scan import ScanStartedMessage
from gda.device.scannable import XasScannable
from gda.device.scannable import XasScannableWithDetectorFramesSetup
from gda.device.scannable import JEPScannable
from gda.exafs.scan import RepetitionsProperties
from gda.factory import Finder
from java.lang import InterruptedException
from java.lang import System
from gda.jython import ScriptBase
from gda.jython.scriptcontroller.event import ScanCreationEvent, ScanFinishEvent, ScriptProgressEvent
from gda.jython.scriptcontroller.logging import XasProgressUpdater
from gda.jython.scriptcontroller.logging import LoggingScriptController
from gda.jython.scriptcontroller.logging import XasLoggingMessage
from gda.scan import ScanBase, ConcurrentScan
from uk.ac.gda.beans.exafs import XanesScanParameters
from scan import Scan
from exafs_environment import ExafsEnvironment

class XasScan(Scan):
    
    def _defineBeanGroup(self, folderName, validation, controller, xmlFolderName, sampleBean, scanBean, detectorBean, outputBean):
        originalGroup = BeanGroup()
        originalGroup.setController(controller)
        originalGroup.setScriptFolder(xmlFolderName)
        originalGroup.setScannable(Finder.getInstance().find(scanBean.getScannableName())) #TODO
        originalGroup.setExperimentFolderName(folderName)
        if (sampleBean != None):
            originalGroup.setSample(sampleBean)
        originalGroup.setDetector(detectorBean)
        outputBean.setAsciiFileName(sampleBean.getName())
        originalGroup.setOutput(outputBean)
        originalGroup.setValidate(validation)
        originalGroup.setScan(scanBean)
        return originalGroup
    
    def _configureScannable(self,beanGroup):
        xas_scannable = XasScannable()
        xas_scannable.setName("xas_scannable")
        xas_scannable.setEnergyScannable(Finder.getInstance().find(beanGroup.getScan().getScannableName()))
        return xas_scannable

    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, numRepetitions= 1, validation=True):
        ScanBase.interrupted = False
        ScriptBase.interrupted = False
        ScriptBase.paused = False
        controller = Finder.getInstance().find("ExafsScriptObserver")
        
        # Create the beans from the file names
        xmlFolderName = ExafsEnvironment().getXMLFolder() + folderName + "/"

        print "xmlFolderName=" + str(xmlFolderName)
        print "sampleFileName=" + str(sampleFileName)
        print "scanFileName=" + str(scanFileName)
        print "detectorFileName=" + str(detectorFileName)
        print "outputFileName=" + str(outputFileName)
        
        sampleBean, scanBean, detectorBean, outputBean = self._createBeans(xmlFolderName, sampleFileName, scanFileName, detectorFileName, outputFileName)

        # create unique ID for this scan (all repetitions will share the same ID)
        scriptType = "Exafs"
        if isinstance(scanBean, XanesScanParameters):
            scriptType = "Xanes"
        scan_unique_id = LoggingScriptController.createUniqueID(scriptType);
        
        # update to terminal
        print "**********************************"
        print "Starting",scriptType,detectorBean.getExperimentType(),"scan over scannable '"+scanBean.getScannableName()+"'..."
        print ""
        print "Output to",xmlFolderName
        print ""

        # give the beans to the xasdatawriter class to help define the folders/filenames 
        beanGroup = self._defineBeanGroup(folderName, validation, controller, xmlFolderName, sampleBean, scanBean, detectorBean, outputBean)
        
        self._doLooping(beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller, scanBean, outputBean)

    def _doLooping(self,beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller, scanBean, outputBean):
        # Insert sample environment looping logic here by subclassing
        self._doScan(beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller, scanBean, outputBean)
        
    def _doScan(self,beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller, scanBean, outputBean):
        # reset the properties used to control repetition behaviour
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
        
                # create the list of scan points
                points = ()
                if isinstance(beanGroup.getScan(), XanesScanParameters):
                    points = XanesScanPointCreator.calculateEnergies(beanGroup.getScan())
                else:
                    points = ExafsScanPointCreator.calculateEnergies(beanGroup.getScan())
                    
                # create the scannable to control energy and time
                xas_scannable = self._configureScannable(beanGroup)
    
                # send out initial messages for logging and display to user
                outputFolder = beanGroup.getOutput().getAsciiDirectory()+ "/" + beanGroup.getOutput().getAsciiFileName()
                
                initialPercent = str(int((float(repetitionNumber - 1) / float(numRepetitions)) * 100)) + "%" 
                logmsg = XasLoggingMessage(scan_unique_id, scriptType, "Starting "+scriptType+" scan...", str(repetitionNumber), str(numRepetitions), initialPercent,str(0),str(0),beanGroup.getScan(),outputFolder)
                self.loggingcontroller.update(None,logmsg)
                self.loggingcontroller.update(None,ScanStartedMessage(beanGroup.getScan(),beanGroup.getDetector())) # informs parts of the UI about current scan
                loggingbean = XasProgressUpdater(self.loggingcontroller,logmsg,timeRepetitionsStarted)
    
                # work out which detectors to use (they will need to have been configured already by the GUI)
                detectorList = self._getDetectors(beanGroup.getDetector(), beanGroup.getScan()) 
                xas_scannable.setDetectors(detectorList)
                
                # work out extra scannables to include
                signalParameters = self._getSignalList(beanGroup.getOutput())
                
                # run the beamline specific preparers            
                self.detectorPreparer.prepare(beanGroup.getDetector(), beanGroup.getOutput(), xmlFolderName)
                sampleScannables = self.samplePreparer.prepare(beanGroup.getSample())
                initial_energy = scanBean.getInitialEnergy()
                final_energy = scanBean.getFinalEnergy()
                outputScannables = self.outputPreparer.prepare(outputBean, initial_energy, final_energy)
                scanPlotSettings = self.outputPreparer.getPlotSettings(beanGroup)
                #print scanPlotSettings
                # run the before scan script
                self._runScript(beanGroup.getOutput().getBeforeScriptName())
     
                # build the scan command arguments
                args = [xas_scannable, points]
                if sampleScannables != None:
                    args += sampleScannables
                if outputScannables != None:
                    args += outputScannables
                args += detectorList 
                args += signalParameters
                args += [loggingbean]
                
                # run the scan
                try:
                    controller.update(None, ScriptProgressEvent("Running scan"))
                    ScanBase.interrupted = False
                    if numRepetitions > 1:
                        print ""
                        print "Starting repetition", str(repetitionNumber),"of",numRepetitions
                    else:
                        print ""
                        print "Starting "+scriptType+" scan..."
                    thisscan = ConcurrentScan(args)
                    thisscan = self._setUpDataWriter(thisscan,beanGroup)
                    thisscan.setReturnScannablesToOrginalPositions(False)
                    controller.update(None, ScanCreationEvent(thisscan.getName()))
                    if (scanPlotSettings != None):
                        print "Setting the filter for columns to plot..."
                        thisscan.setScanPlotSettings(scanPlotSettings)
                    thisscan.runScan()
                except InterruptedException, e:
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
                        
                #update observers
                controller.update(None, ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK));
                    
                # run the after scan script
                self._runScript(beanGroup.getOutput().getAfterScriptName())
                
                #check if halt after current repetition set to true
                if LocalProperties.get(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY) == "true":
                    print "Paused scan after repetition",str(repetitionNumber),". To resume the scan, press the Start button in the Command Queue view. To abort this scan, press the Skip Task button."
                    Finder.getInstance().find("commandQueueProcessor").pause(500);
                    LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY,"false")
                    ScriptBase.checkForPauses()
                
                #check if the number of repetitions has been altered and we should now end the loop
                numRepsFromProperty = int(LocalProperties.get(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY))
                if numRepsFromProperty != numRepetitions and numRepsFromProperty <= (repetitionNumber):
                    print "The number of repetitions has been reset to",str(numRepsFromProperty), ". As",str(repetitionNumber),"repetitions have been completed this scan will now end."
                    break
                elif numRepsFromProperty <= (repetitionNumber):
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
    
    def _beforeEachRepetition(self,beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller, repNum):
        return

    def _getSignalList(self, outputParameters):
        signalList = []
        for signal in outputParameters.getSignalList():
             scannable = JEPScannable.createJEPScannable(signal.getLabel(), signal.getScannableName(), signal.getDataFormat(), signal.getName(), signal.getExpression())
             signalList.append(scannable)
        return signalList
            
    def _getDetectors(self, detectorBean, scanBean):
        expt_type = detectorBean.getExperimentType()
        detectorList = []
        if expt_type == "Transmission":
            print "This is a transmission scan"
            for group in detectorBean.getDetectorGroups():
                if group.getName() == detectorBean.getTransmissionParameters().getDetectorType():
                    return self._createDetArray(group.getDetector(), scanBean)
        elif expt_type == "XES":
            for group in detectorBean.getDetectorGroups():
                if group.getName() == "XES":
                    return self._createDetArray(group.getDetector(), scanBean)
        else:
            print "This is a fluoresence scan"
            for group in detectorBean.getDetectorGroups():
                if group.getName() == detectorBean.getFluorescenceParameters().getDetectorType():
                    #print detectorBean.getFluorescenceParameters().getDetectorType(), "experiment"
                    return self._createDetArray(group.getDetector(), scanBean)

    """
    Get the relevant datawriter config, create a datawriter and if it of the correct type then give it the config.
    Give the datawriter to the scan.
    """
    def _setUpDataWriter(self,thisscan,beanGroup):
        from gda.data.scan.datawriter import  DefaultDataWriterFactory,ConfigurableAsciiFormat
        asciidatawriterconfig = self.outputPreparer.getAsciiDataWriterConfig(beanGroup)
        if asciidatawriterconfig != None:
            dataWriter = DefaultDataWriterFactory.createDataWriterFromFactory()
            if isinstance(dataWriter,ConfigurableAsciiFormat):
                dataWriter.setConfiguration(asciidatawriterconfig)
            thisscan.setDataWriter(dataWriter)
        return thisscan
