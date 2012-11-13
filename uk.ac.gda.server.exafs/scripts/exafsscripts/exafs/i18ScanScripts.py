import pprint

from BeamlineParameters import JythonNameSpaceMapping, FinderNameMapping

from java.io import File
from java.lang import InterruptedException

from gda.configuration.properties import LocalProperties
from gda.data import PathConstructor
from gda.data.scan.datawriter import XasAsciiDataWriter, NexusExtraMetadataDataWriter
from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
from gda.exafs.scan import ExafsScanPointCreator, XanesScanPointCreator
from gda.exafs.scan.ExafsScanRegionCalculator import calculateABC
from gda.exafs.scan import BeanGroup
from gda.exafs.scan import BeanGroups
from gda.exafs.scan import ScanStartedMessage
from gda.exafs.scan import RepetitionsProperties
from gda.device import CounterTimer
from gda.device.detector.countertimer import CounterTimerBase
from gda.device.detector.xspress import XspressDetector
from gda.device.scannable import XasScannable
from gda.device.scannable import XasScannableWithDetectorFramesSetup
from gda.device.scannable import JEPScannable
from gda.factory import Finder
from gda.jython import InterfaceProvider
from gda.jython.commands.GeneralCommands import run
from gda.jython.commands.ScannableCommands import scan, pos, add_default, remove_default
from gda.jython.scriptcontroller.event import ScanCreationEvent, ScanFinishEvent, ScriptProgressEvent
from gda.jython.scriptcontroller.logging import XasProgressUpdater
from gda.jython.scriptcontroller.logging import LoggingScriptController
from gda.jython.scriptcontroller.logging import XasLoggingMessage
from gda.jython import ScriptBase
from gda.scan import ScanBase, ContinuousScan, ConcurrentScan

from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.beans.exafs import XanesScanParameters
from uk.ac.gda.doe import RangeInfo
from uk.ac.gda.beans.exafs import XesScanParameters, SignalParameters

from gdascripts.messages.handle_messages import simpleLog

def qexafs (sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):

   xmlFolderName = ExafsEnvironment().getScriptFolder() + folderName + "/"
   
   # Create the beans from the file names
   sampleBean = BeansFactory.getBeanObject(xmlFolderName, sampleFileName)
   # this should be the qexafs bean
   scanBean = BeansFactory.getBeanObject(xmlFolderName, scanFileName)
   detectorBean = BeansFactory.getBeanObject(xmlFolderName, detectorFileName)
   outputBean = BeansFactory.getBeanObject(xmlFolderName, outputFileName)
    
   finder_mapper = FinderNameMapping()
   jython_mapper = JythonNameSpaceMapping()
   controller = Finder.getInstance().find("ExafsScriptObserver")
    
   # give the beans to the xasdatawriter class to help define the folders/filenames 
   beanGroup = BeanGroup()
   beanGroup.setController(controller)
   beanGroup.setScriptFolder(xmlFolderName)
   beanGroup.setExperimentFolderName(folderName)
   beanGroup.setScanNumber(scanNumber)
   beanGroup.setSample(sampleBean)
   beanGroup.setDetector(detectorBean)
   beanGroup.setOutput(outputBean)
   beanGroup.setScan(scanBean)
   
   XasAsciiDataWriter.setBeanGroup(beanGroup)

   # work out which detectors to use (they will need to have been configured already by the GUI)
   detectorList = getQEXAFSDetectors(beanGroup.getDetector(), beanGroup.getOutput(), beanGroup.getScan()) 
   print "detectors to be used:", str(detectorList)
   
   # set up the sample 
   setup(beanGroup)
   
   # no signal parameters
   if len(outputBean.getCheckedSignalList()) > 0:
       print "Signal parameters not available with QEXAFS"
   
   # energy controller
   energyController = jython_mapper.qexafs_energy
#    if energyController == None:
#        energyController = jython_mapper.qexafs_energy
   if energyController == None:
       raise "No object for controlling energy during QEXAFS found! Expected qexafs_energy (or scannable1 for testing)"
   
   
   initial_energy = scanBean.getInitialEnergy()
   final_energy = scanBean.getFinalEnergy()
   step_size = scanBean.getStepSize()
   import math
   numberPoints = int(math.ceil((final_energy-initial_energy)/step_size))
   
           
   # run the script held in outputparameters
   scriptName = beanGroup.getOutput().getBeforeScriptName()
   if scriptName != None and scriptName != "":
       scriptName = scriptName[scriptName.rfind("/") + 1:]
       run(scriptName)           

   print "running QEXAFS scan:", energyController.getName(), scanBean.getInitialEnergy(), scanBean.getFinalEnergy(), numberPoints, scanBean.getTime(), detectorList
   
   controller.update(None, ScriptProgressEvent("Running QEXAFS scan"))
   thisscan = ContinuousScan(energyController, scanBean.getInitialEnergy(), scanBean.getFinalEnergy(), numberPoints, scanBean.getTime(), detectorList)

   controller.update(None,ScanCreationEvent(thisscan.getName()))
   thisscan.runScan()  
   controller.update(None,ScanFinishEvent(thisscan.getName(),ScanFinishEvent.FinishType.OK))

   scriptName = beanGroup.getOutput().getAfterScriptName()
   if scriptName != None and scriptName != "":
       scriptName = scriptName[scriptName.rfind("/") + 1:]
       run(scriptName)

def xanes (sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):
    xas(sampleFileName, scanFileName, detectorFileName, outputFileName, folderName, scanNumber, validation)
    return

def finish():
    command_server = Finder.getInstance().find("command_server")
    beam = command_server.getFromJythonNamespace("beam", None)
    detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
    remove_default(beam)
    remove_default(detectorFillingMonitor)

def xas (sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, numRepetitions= 1, validation=True):
    """
    main xas data collection command. 
    usage:-
    
    xas sampleFileName scanFileName detectorFileName outputFileName [experiment name (_script)] [scan index (1)] [validation required (True)]
    
    """
    ScanBase.interrupted = False
    
    if False:# Turn on debugging here
        print "Values sent to script:"
        print "sampleFileName", sampleFileName
        print "scanFileName", scanFileName
        print "detectorFileName", detectorFileName
        print "outputFileName", outputFileName
        print "folderName", folderName
        print "numRepetitions", numRepetitions
        print "validation", validation

    finder_mapper = FinderNameMapping()
    jython_mapper = JythonNameSpaceMapping()
    controller = Finder.getInstance().find("ExafsScriptObserver")
    loggingcontroller = Finder.getInstance().find("XASLoggingScriptController")

    # Create the beans from the file names
    xmlFolderName = ExafsEnvironment().getScriptFolder() + folderName + "/"
#    print "xmlFolderName", xmlFolderName  
    if(sampleFileName == None):
        sampleBean = None
    else:
        sampleBean = BeansFactory.getBeanObject(xmlFolderName, sampleFileName)
    scanBean = BeansFactory.getBeanObject(xmlFolderName, scanFileName)
    detectorBean = BeansFactory.getBeanObject(xmlFolderName, detectorFileName)
    outputBean = BeansFactory.getBeanObject(xmlFolderName, outputFileName)
     
    # create the scannable which will control energy & time for each point
    useFrames = LocalProperties.check("gda.microfocus.scans.useFrames")
    if(detectorBean.getExperimentType() == "Fluorescence" and detectorBean.getFluorescenceParameters().getDetectorType() == "Germanium" and useFrames):
        xas_scannable = XasScannableWithDetectorFramesSetup()
        xas_scannable.setHarmonicConverterName("auto_mDeg_idGap_mm_converter")
    else:
        xas_scannable = XasScannable()
        useFrames = 0
    xas_scannable.setName("xas_scannable")
#    print "Energy control supplied by scannable:", scanBean.getScannableName()
    xas_scannable.setEnergyScannable(Finder.getInstance().find(scanBean.getScannableName()))
     
    # give the beans to the xasdatawriter class to help define the folders/filenames 
    originalGroup = BeanGroup()
    originalGroup.setController(controller)
    originalGroup.setScriptFolder(xmlFolderName)
    originalGroup.setScannable(Finder.getInstance().find(scanBean.getScannableName())) #TODO
    originalGroup.setExperimentFolderName(folderName)
    originalGroup.setScanNumber(numRepetitions)
    if (sampleBean != None):
        originalGroup.setSample(sampleBean)
    originalGroup.setDetector(detectorBean)
    originalGroup.setOutput(outputBean)
    originalGroup.setValidate(validation)
    originalGroup.setScan(scanBean)
    
    # Use BeanGroups to generate the DOE experiments.
#    groups = BeanGroups.expand(originalGroup)
#    infos = BeanGroups.getInfo(originalGroup)
#    print "DoE group size", len(groups)
    
#    for i in range(0, len(groups)):

    # create unique ID for this scan (all repetitions will share the same ID)
    scriptType = "Exafs"
    if isinstance(scanBean, XanesScanParameters):
        scriptType = "Xanes"
    scan_unique_id = LoggingScriptController.createUniqueID(scriptType)
        
    # update to terminal
    print "Starting",scriptType,detectorBean.getExperimentType(),"scan over scannable '"+scanBean.getScannableName()+"'..."
    print ""
    print "Output to",xmlFolderName
    print ""

    beanGroup = originalGroup

    # reset the properties used to control repetition behaviour
    LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY,"false")
    LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
    LocalProperties.set(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY,str(numRepetitions))
    repetitionNumber = 0
        
    try:
        while True:
            repetitionNumber+= 1
            beanGroup.setScanNumber(repetitionNumber)
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

            # work out which detectors to use (they will need to have been configured already by the GUI)
            detectorList = getDetectors(beanGroup.getDetector(), beanGroup.getOutput(), beanGroup.getScan()) 
            xas_scannable.setDetectors(detectorList)

            # send out initial messages for logging and display to user
            outputFolder = outputBean.getAsciiDirectory()+ "/" + outputBean.getAsciiFileName()
            logmsg = XasLoggingMessage(scan_unique_id, scriptType, "Starting "+scriptType+" scan...", str(repetitionNumber), str("0%"),str(0),scanBean,outputFolder)
            loggingcontroller.update(None,logmsg)
            loggingcontroller.update(None,ScanStartedMessage(scanBean,detectorBean)) # informs parts of the UI about current scan
            loggingbean = XasProgressUpdater(loggingcontroller,logmsg)
   
            # set up the sample 
            extraColumns = setup(beanGroup)

            # extract any signal parameters to add to the scan command
            signalParameters = getSignalList(beanGroup.getOutput())
        
            # run the script held in outputparameters
            scriptName = beanGroup.getOutput().getBeforeScriptName()
            if scriptName != None and scriptName != "":
                scriptName = scriptName[scriptName.rfind("/") + 1:]
                run(scriptName)

            # run the scans
            print "detectors:", str(detectorList)
            if len(signalParameters) > 0:
                print "signal list:", str(signalParameters)
            print "number of steps:", len(points)
            args = [xas_scannable, points]
            if extraColumns != None:
                print "adding to the scan", extraColumns
                args += [extraColumns]
            args += detectorList 
            args += signalParameters
            args += [loggingbean]
            
            #simpleLog("about to scan")
        
            ScanBase.checkForInterrupts()

            try:
                if numRepetitions > 1:
                    print ""
                    print "Starting repetition", str(repetitionNumber),"of",numRepetitions
                controller.update(None, ScriptProgressEvent("Running scan"))
                thisscan = ConcurrentScan(args)
                controller.update(None, ScanCreationEvent(thisscan.getName()))
                thisscan.runScan()  
                #scan(args)
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
            
            controller.update(None, ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK))

            scriptName = beanGroup.getOutput().getAfterScriptName()
            if scriptName != None and scriptName != "":
                scriptName = scriptName[scriptName.rfind("/") + 1:]
                run(scriptName)

            #check if halt after current repetition set to true
            if numRepetitions > 1 and LocalProperties.get(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY) == "true":
                    print "Paused scan after repetition",str(repetitionNumber),". To resume the scan, press the Start button in the Command Queue view. To abort this scan, press the Skip Task button."
                    LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY,"false")
                    Finder.getInstance().find("commandQueueProcessor").pause(500)
                    ScriptBase.checkForPauses()
                
            #check if the number of repetitions has been altered and we should now end the loop
            numRepsFromProperty = int(LocalProperties.get(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY))
            if numRepsFromProperty != numRepetitions and numRepsFromProperty <= (repetitionNumber):
                print "The number of repetitions has been reset to",str(numRepsFromProperty), ". As",str(repetitionNumber),"repetitions have been completed this scan will now end."
                break
            elif numRepsFromProperty <= (repetitionNumber):
                break

    finally:
    # clear the extra metadata which would have been added to the Nxues file (rebuilt everytime in setup)
        NexusExtraMetadataDataWriter.removeAllMetadataEntries()
        LocalProperties.set("gda.scan.useScanPlotSettings", "false")
        LocalProperties.set("gda.plot.ScanPlotSettings.fromUserList", "false")
        finish(beanGroup)
 
    #remove added metadata from default metadata list to avoid multiple instances of the same metadata
    # undefined in I20 - has this been set elsewhere on a specific beamline?
    #original_header=jython_mapper.original_header[:]
    #Finder.getInstance().find("datawriterconfig").setHeader(original_header)
    # End Loop
    
def getSignalList(outputParameters):
    signalList = []
    for signal in outputParameters.getCheckedSignalList():
         scannable = JEPScannable.createJEPScannable(signal.getLabel(), signal.getScannableName(), signal.getDataFormat(), signal.getName(), signal.getExpression())
         signalList.append(scannable)
    return signalList

def getQEXAFSDetectors(detectorBean, outputBean, scanBean):
    expt_type = detectorBean.getExperimentType()

    detectorList = []
    if expt_type == "Transmission":
        return createDetArray(["qexafs_counterTimer01"], outputBean, scanBean)
            
    # fluorescence - assume xspress for the moment
    else:
        print "NOTE: Xspress is the only option for fluorescence QEXAFS at the moment"
        return createDetArray(["qexafs_counterTimer01", "qexafs_xspress", "QexafsFFI0"], outputBean, scanBean)
        #return createDetArray(["qexafs_counterTimer01","qexafs_xspress"], outputBean,scanBean)

def getDetectors(detectorBean, outputBean, scanBean):
    expt_type = detectorBean.getExperimentType()

    detectorList = []
    if expt_type == "Transmission":
        for group in detectorBean.getDetectorGroups():
            if group.getName() == detectorBean.getTransmissionParameters().getDetectorType():
                return createDetArray(group.getDetector(), None, scanBean)
            
    elif expt_type == "XES":
        for group in detectorBean.getDetectorGroups():
            if group.getName() == "XES":
                # TO DO also need to include the I1 detector and create a new I1/FF detector object
                return createDetArray(group.getDetector(), None, scanBean)
    else:
        for group in detectorBean.getDetectorGroups():
            if group.getName() == detectorBean.getFluorescenceParameters().getDetectorType():
                print detectorBean.getFluorescenceParameters().getDetectorType(), "experiment"
                return createDetArray(group.getDetector(), outputBean, scanBean)

def createDetArray(names, outputBean=None, scanBean=None):
    jython_mapper = JythonNameSpaceMapping()
    dets = []
    numDets = len(names)
    for i in range(0, numDets):
#        if outputBean != None and outputBean.isExtraData() == False:
#            if names[i]=="xmapMca":
#                continue

        name = str(names[i])
        exec("thisDetector = JythonNameSpaceMapping()." + names[i])
        if thisDetector == None:
            raise Exception("detector named " + name + " not found!")
        dets.append(thisDetector)
    return dets
              
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

def setup(beanGroup):
    if beanGroup.getDetector().getExperimentType() == "Fluorescence":
        if (beanGroup.getDetector().getFluorescenceParameters().getDetectorType() == "Germanium"):
            fullFileName = beanGroup.getScriptFolder() + beanGroup.getDetector().getFluorescenceParameters().getConfigFileName()
            bean = BeansFactory.getBean(File(fullFileName))
            bean.setReadoutMode(XspressDetector.READOUT_MCA)
            bean.setResGrade(ResGrades.NONE)
            elements = bean.getDetectorList()
            for element in elements: 
                rois = element.getRegionList()
                element.setWindow(rois.get(0).getRegionStart(), rois.get(0).getRegionEnd())
            BeansFactory.saveBean(File(fullFileName), bean)
        configFluoDetector(beanGroup)
    scan = beanGroup.getScan()
    collectionTime = 0.0
    if isinstance(scan, XanesScanParameters):
        regions = scan.getRegions()        
        for region in regions:
            if(collectionTime < region.getTime()):
                collectionTime = region.getTime()
    elif isinstance(scan, QEXAFSParameters):
        pass
    else:
        collectionTime = scan.getExafsTime()
        if(scan.getExafsToTime() > collectionTime):
            collectionTime = scan.getExafsToTime()
    print "setting collection time to" , str(collectionTime)
    command_server = Finder.getInstance().find("command_server")    
    
    topupMonitor = command_server.getFromJythonNamespace("topupMonitor", None)    
    topupMonitor.setPauseBeforePoint(True)
    topupMonitor.setPauseBeforeLine(False)
    topupMonitor.setCollectionTime(collectionTime)

    beam = command_server.getFromJythonNamespace("beam", None)
    beam.setPauseBeforePoint(True)
    beam.setPauseBeforeLine(True)
    add_default(beam)
    
    detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
    
    trajBeamMonitor = command_server.getFromJythonNamespace("trajBeamMonitor", None)
    
    if(beanGroup.getDetector().getExperimentType() == "Fluorescence" and beanGroup.getDetector().getFluorescenceParameters().getDetectorType() == "Germanium"): 
        add_default(detectorFillingMonitor)
        detectorFillingMonitor.setPauseBeforePoint(True)
        detectorFillingMonitor.setPauseBeforeLine(False)
        detectorFillingMonitor.setCollectionTime(collectionTime)
    trajBeamMonitor.setActive(False)
    
    outputBean=beanGroup.getOutput()
    sampleParameters = beanGroup.getSample()
    outputBean.setAsciiFileName(sampleParameters.getName())
    print "Setting the ascii file name as " ,sampleParameters.getName()
    stage = sampleParameters.getSampleStageParameters()
    att1 = sampleParameters.getAttenuatorParameter1()
    att2 = sampleParameters.getAttenuatorParameter2()
    pos([rootnamespace['sc_MicroFocusSampleX'], stage.getX(), rootnamespace['sc_MicroFocusSampleY'], stage.getY(), rootnamespace['sc_sample_z'], stage.getZ()])
    pos([rootnamespace['D7A'], att1.getSelectedPosition(), rootnamespace['D7B'], att2.getSelectedPosition()])
    Finder.getInstance().find("RCPController").openPesrpective("org.diamond.exafs.ui.PlottingPerspective")