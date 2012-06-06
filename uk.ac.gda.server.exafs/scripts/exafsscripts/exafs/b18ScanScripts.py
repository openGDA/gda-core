from BeamlineParameters import JythonNameSpaceMapping
from gda.data import PathConstructor
from gda.factory import Finder

from gda.configuration.properties import LocalProperties
from gda.data.scan.datawriter import XasAsciiDataWriter, NexusExtraMetadataDataWriter
from gda.exafs.scan import ExafsScanPointCreator, XanesScanPointCreator
from gda.exafs.scan import BeanGroup, BeanGroups
from gda.exafs.scan import ScanStartedMessage
from gda.device.scannable import XasScannable
from gda.device.scannable import XasScannableWithDetectorFramesSetup
from gda.device.scannable import JEPScannable
from gda.jython.scriptcontroller.event import ScanCreationEvent, ScanFinishEvent, ScriptProgressEvent
from gda.scan import ScanBase, ContinuousScan, ConcurrentScan
from gda.jython.commands.GeneralCommands import run
from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.beans.exafs import XanesScanParameters
from gdascripts.messages.handle_messages import simpleLog
from gda.jython.scriptcontroller.logging import XasProgressUpdater


class Scan:
    
    def __init__(self, detectorPreparer, samplePreparer, outputPreparer):
        self.detectorPreparer = detectorPreparer
        self.samplePreparer = samplePreparer
        self.outputPreparer = outputPreparer
        
    def _createDetArray(self, names, scanBean=None):
        dets = []
        numDets = len(names)
        for i in range(0, numDets):
            name = str(names[i])
            exec("thisDetector = JythonNameSpaceMapping()." + names[i])
            if thisDetector == None:
                raise Exception("detector named " + name + " not found!")
            dets.append(thisDetector)
        return dets

    def _createBeans(self, xmlFolderName, sampleFileName, scanFileName, detectorFileName, outputFileName):
        # Create the beans from the file names
        if(sampleFileName == None):
            sampleBean = None
        else:
            sampleBean = BeansFactory.getBeanObject(xmlFolderName, sampleFileName)
        scanBean = BeansFactory.getBeanObject(xmlFolderName, scanFileName)
        detectorBean = BeansFactory.getBeanObject(xmlFolderName, detectorFileName)
        outputBean = BeansFactory.getBeanObject(xmlFolderName, outputFileName)
        return sampleBean, scanBean, detectorBean, outputBean
    
    def _runScript(self, scriptName):
        if scriptName != None and scriptName != "":
            scriptName = scriptName[scriptName.rfind("/") + 1:]
            run(scriptName)

    
class XasScan(Scan):
        
    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, numRepetitions= -1, validation=True):
        ScanBase.interrupted = False
    
        controller = Finder.getInstance().find("ExafsScriptObserver")
        
        # Create the beans from the file names
        xmlFolderName = ExafsEnvironment().getScriptFolder() + folderName + "/"
        print "xmlFolderName", xmlFolderName  
        
        sampleBean, scanBean, detectorBean, outputBean = self._createBeans(xmlFolderName, sampleFileName, scanFileName, detectorFileName, outputFileName)

        # send initial message to the log
        from gda.jython.scriptcontroller.logging import LoggingScriptController
        from gda.jython.scriptcontroller.logging import XasLoggingMessage
        loggingcontroller   = Finder.getInstance().find("XASLoggingScriptController")
        scriptType = "Exafs"
        if isinstance(scanBean, XanesScanParameters):
            scriptType = "Xanes"
        unique_id           = LoggingScriptController.createUniqueID(scriptType);

        # create the scannable which will control energy & time for each point
        useFrames = LocalProperties.check("gda.microfocus.scans.useFrames")
        if(detectorBean.getExperimentType() == "Fluorescence" and detectorBean.getFluorescenceParameters().getDetectorType() == "Germanium" and useFrames):
            xas_scannable = XasScannableWithDetectorFramesSetup()
            xas_scannable.setHarmonicConverterName("auto_mDeg_idGap_mm_converter")
        else:
            xas_scannable = XasScannable()
            useFrames = 0
        xas_scannable.setName("xas_scannable")
        print "Energy control supplied by scannable:", scanBean.getScannableName()
        xas_scannable.setEnergyScannable(Finder.getInstance().find(scanBean.getScannableName()))
        # give the beans to the xasdatawriter class to help define the folders/filenames 
        originalGroup = BeanGroup()
        originalGroup.setController(controller)
        originalGroup.setScriptFolder(xmlFolderName)
        originalGroup.setScannable(Finder.getInstance().find(scanBean.getScannableName())) #TODO
        originalGroup.setExperimentFolderName(folderName)
#        originalGroup.setScanNumber(scanNumber)
        if (sampleBean != None):
            originalGroup.setSample(sampleBean)
        originalGroup.setDetector(detectorBean)
        outputBean.setAsciiFileName(sampleBean.getName())
        originalGroup.setOutput(outputBean)
        originalGroup.setValidate(validation)
        originalGroup.setScan(scanBean)

# RJW May2012 remove the DOE stuff and replace with the number of repetitions        
        # Use BeanGroups to generate the DOE experiments.
#        groups = BeanGroups.expand(originalGroup)
#        infos = BeanGroups.getInfo(originalGroup)
#        print "DoE group size", len(groups)
        
        for repetitionNumber in range(0, numRepetitions):
            originalGroup.setScanNumber(repetitionNumber)
            beanGroup = originalGroup   #groups[i];
            XasAsciiDataWriter.setBeanGroup(beanGroup)
    
            # create the list of scan points
            points = ()
            if isinstance(beanGroup.getScan(), XanesScanParameters):
                points = XanesScanPointCreator.calculateEnergies(beanGroup.getScan())
                if useFrames:
                    xas_scannable.setExafsScanRegionTimes(XanesScanPointCreator.getScanTimes(beanGroup.getScan()))
            else:
                points = ExafsScanPointCreator.calculateEnergies(beanGroup.getScan())
                if useFrames:
                    xas_scannable.setExafsScanRegionTimes(ExafsScanPointCreator.getScanTimes(beanGroup.getScan()))

            # send out initial messages
            logmsg = XasLoggingMessage(unique_id, scriptType, "Starting "+scriptType+" scan...", str(repetitionNumber), str("0%"),str(0),scanBean)
            loggingcontroller.update(None,logmsg)
            loggingcontroller.update(None,ScanStartedMessage(scanBean,detectorBean))
            loggingbean = XasProgressUpdater(loggingcontroller,logmsg)

            
            # This step adds information to the sample parameters description 
            # if we are doing a multiple run.
#            if len(groups) > 1:
#                rangeInfo = infos[i]
#                beanGroup.getSample().addDescription("");
#                beanGroup.getSample().addDescription("***");
#                beanGroup.getSample().addDescription("Design of experiments study run '" + str(i + 1) + "' of '" + str(len(groups)) + "'. With following parameter(s):")
#                beanGroup.getSample().addDescription(rangeInfo.getHeader())
#                beanGroup.getSample().addDescription(rangeInfo.getValues())
#                beanGroup.getSample().addDescription("***");
#                simpleLog("Loop of experiments: run " + str(i + 1) + " of " + str(len(groups)))
#                simpleLog("Variable(s) being looped over:" + str(rangeInfo.getHeader()))
#                simpleLog("Current value(s):" + str(rangeInfo.getValues()))
    
            # work out which detectors to use (they will need to have been configured already by the GUI)
            detectorList = self._getDetectors(beanGroup.getDetector(), beanGroup.getScan()) 
            xas_scannable.setDetectors(detectorList)
            
            # set up the sample 
            self.detectorPreparer.prepare(detectorBean, outputBean, xmlFolderName)
            temperatureController = self.samplePreparer.prepare(sampleBean)
            self.outputPreparer.prepare(outputBean)
    
            # extract any signal parameters to add to the scan command
            signalParameters = self._getSignalList(beanGroup.getOutput())
           
            self._runScript(beanGroup.getOutput().getBeforeScriptName())

            # run the scans
            print "detectors:", str(detectorList)
            if len(signalParameters) > 0:
                print "signal list:", str(signalParameters)
            print "number of steps:", len(points)
            args = [xas_scannable, points]
            if temperatureController != None:
                print "adding to the scan", temperatureController
                args += [temperatureController]
            args += detectorList 
            args += signalParameters
            args += [loggingbean]
            
            simpleLog("about to scan")
            
            ScanBase.checkForInterrupts()
    
            try:
                controller.update(None, ScriptProgressEvent("Running scan"))
                thisscan = ConcurrentScan(args)
                controller.update(None, ScanCreationEvent(thisscan.getName()))
                thisscan.runScan()
            finally:
            # clear the extra metadata which would have been added to the Nxues file (rebuilt everytime in setup)
                controller.update(None, ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK));
                NexusExtraMetadataDataWriter.removeAllMetadataEntries()
                LocalProperties.set("gda.scan.useScanPlotSettings", "false")
                LocalProperties.set("gda.plot.ScanPlotSettings.fromUserList", "false")
     
            self._runScript(beanGroup.getOutput().getAfterScriptName())
            
            #remove added metadata from default metadata list to avoid multiple instances of the same metadata
            jython_mapper = JythonNameSpaceMapping()
            original_header=jython_mapper.original_header[:]
            Finder.getInstance().find("datawriterconfig").setHeader(original_header)
    
    def _getSignalList(self, outputParameters):
        signalList = []
        for signal in outputParameters.getCheckedSignalList():
             scannable = JEPScannable.createJEPScannable(signal.getLabel(), signal.getScannableName(), signal.getDataFormat(), signal.getName(), signal.getExpression())
             signalList.append(scannable)
        return signalList
            
    def _getDetectors(self, detectorBean, scanBean):
        expt_type = detectorBean.getExperimentType()
        detectorList = []
        if expt_type == "Transmission":
            for group in detectorBean.getDetectorGroups():
                if group.getName() == detectorBean.getTransmissionParameters().getDetectorType():
                    return self._createDetArray(group.getDetector(), scanBean)
        elif expt_type == "Soft X-Rays":
            for group in detectorBean.getDetectorGroups():
                if group.getName() == detectorBean.getSoftXRaysParameters().getDetectorType():
                    return self._createDetArray(group.getDetector(), scanBean)    
        elif expt_type == "XES":
            for group in detectorBean.getDetectorGroups():
                if group.getName() == "XES":
                    # TO DO also need to include the I1 detector and create a new I1/FF detector object
                    return self._createDetArray(group.getDetector(), scanBean)
        else:
            for group in detectorBean.getDetectorGroups():
                if group.getName() == detectorBean.getFluorescenceParameters().getDetectorType():
                    print detectorBean.getFluorescenceParameters().getDetectorType(), "experiment"
                    return self._createDetArray(group.getDetector(), scanBean)

   
class QexafsScan(Scan):
    
    def __init__(self, detectorPreparer, samplePreparer, outputPreparer, energy_scannable, ion_chambers_scannable):
        Scan.__init__(self, detectorPreparer, samplePreparer, outputPreparer)
        self.energy_scannable = energy_scannable
        self.ion_chambers_scannable = ion_chambers_scannable
    
    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, numRepetitions= -1, validation=True):
        xmlFolderName = ExafsEnvironment().getScriptFolder() + folderName + "/"
        sampleBean, scanBean, detectorBean, outputBean = self._createBeans(xmlFolderName, sampleFileName, scanFileName, detectorFileName, outputFileName) 
        controller = Finder.getInstance().find("ExafsScriptObserver")
        XasAsciiDataWriter.setBeanGroup(self._createBeanGroup(controller, xmlFolderName, folderName, sampleBean, detectorBean, outputBean, scanBean))
        outputBean.setAsciiFileName(sampleBean.getName())
        # work out which detectors to use (they will need to have been configured already by the GUI)
        detectorList = self._getQEXAFSDetectors(detectorBean, outputBean, scanBean) 
        print "detectors to be used:", str(detectorList)
        
        for repetitionNumber in range(0, numRepetitions):

            # set up the sample 
            self.detectorPreparer.prepare(detectorBean, outputBean, xmlFolderName)
            self.samplePreparer.prepare(sampleBean)
            self.outputPreparer.prepare(outputBean)
        
        # no signal parameters
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
        
            print "running QEXAFS scan:", self.energy_scannable.getName(), scanBean.getInitialEnergy(), scanBean.getFinalEnergy(), numberPoints, scan_time, detectorList
            controller.update(None, ScriptProgressEvent("Running QEXAFS scan"))
            thisscan = ContinuousScan(self.energy_scannable , scanBean.getInitialEnergy(), scanBean.getFinalEnergy(), numberPoints, scan_time, detectorList)
            controller.update(None, ScanCreationEvent(thisscan.getName()))
            thisscan.runScan()  
            controller.update(None, ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK));
        
            self._runScript(outputBean.getAfterScriptName())
        
            #remove added metadata from default metadata list to avoid multiple instances of the same metadata
        jython_mapper = JythonNameSpaceMapping()
        original_header=jython_mapper.original_header[:]
        Finder.getInstance().find("datawriterconfig").setHeader(original_header)
 
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
                return self._createDetArray(["qexafs_counterTimer01", "qexafs_xmap", "QexafsFFI0"], scanBean)
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

                
class ExafsEnvironment:
    testScriptFolder = None
    
    def getScriptFolder(self):
        if ExafsEnvironment.testScriptFolder != None:
            return ExafsEnvironment.testScriptFolder
        dataDirectory = PathConstructor.createFromDefaultProperty()
        return dataDirectory + "/xml/"

    testScannable = None
    
    def getScannable(self):
        if ExafsEnvironment.testScannable != None:
            return ExafsEnvironment.testScannable
        # The scannable name is defined in the XML when not in testing mode.
        # Therefore the scannable argument is omitted from the bean
        return None
