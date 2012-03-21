#@PydevCodeAnalysisIgnore
import pprint

from BeamlineParameters import JythonNameSpaceMapping, FinderNameMapping

from java.io import File
from gda.configuration.properties import LocalProperties
from gda.data import PathConstructor
from gda.data.scan.datawriter import XasAsciiDataWriter, NexusExtraMetadataDataWriter
from gda.exafs.scan import ExafsScanPointCreator, XanesScanPointCreator
from gda.exafs.scan.ExafsScanRegionCalculator import calculateABC
from gda.exafs.scan import BeanGroup
from gda.exafs.scan import BeanGroups
from gda.device import CounterTimer
from gda.device.detector.countertimer import CounterTimerBase
from gda.device.detector.xspress import XspressDetector
from gda.device.scannable import XasScannable
from gda.device.scannable import XasScannableWithDetectorFramesSetup
from gda.device.scannable import JEPScannable
from gda.factory import Finder
from gda.jython import InterfaceProvider
from gda.jython.commands.ScannableCommands import scan, pos, add_default, remove_default
from gda.jython.scriptcontroller.event import ScanCreationEvent, ScanFinishEvent, ScriptProgressEvent
from gda.scan import ScanBase, ContinuousScan, ConcurrentScan
from gda.jython.commands.GeneralCommands import run
from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.beans.exafs import XanesScanParameters
from uk.ac.gda.doe import RangeInfo
from uk.ac.gda.beans.exafs import XesScanParameters, SignalParameters
from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
from gdascripts.messages.handle_messages import simpleLog
from exafsscripts.exafs.setupBeamline import setup,finish
from gda.factory import Finder

def estimateXas(scanFileName, nosOfScans=1, timeEstimation=False):
    """
    Returns how many points would result from a given XAS XML file.
    """
    scanBean = BeansFactory.getBeanObject(ExafsEnvironment().getScriptFolder(), scanFileName)
    points = ExafsScanPointCreator.calculateEnergies(scanBean)
    # TODO Get time from points if timeEstimation==True
    return len(points)


def estimateXanes(scanFileName, nosOfScans=1, timeEstimation=False):
    """
    Returns how many points would result from a given XANES XML file.
    """
    scanBean = BeansFactory.getBeanObject(ExafsEnvironment().getScriptFolder(), scanFileName)
    points = XanesScanPointCreator.calculateEnergies(scanBean)
    # TODO Get time from points if timeEstimation==True
    return len(points)

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
    temp = setup(beanGroup)
    
    # no signal parameters
    if len(outputBean.getCheckedSignalList()) > 0:
        print "Signal parameters not available with QEXAFS"
    
    
    # energy controller
    energyController = jython_mapper.qexafs_energy
#    if energyController == None:
#        energyController = jython_mapper.qexafs_energy
    if energyController == None:
        raise "No object for controlling energy during QEXAFS found! Expected qexafs_energy (or scannable1 for testing)"
    
    # work out the number of frames to collect
    numberPoints = 0
    if detectorBean.getExperimentType() == "Fluorescence" and detectorBean.getFluorescenceParameters().getDetectorType() == "Germanium":
        maxPoints = jython_mapper.qexafs_counterTimer01.maximumReadFrames()
        if scanBean.isChooseNumberPoints():
            numberPoints = maxPoints
        else:
            numberPoints = scanBean.getNumberPoints()
            if numberPoints > maxPoints:
                raise "Too many frames for the given detector configuration." 
    # for ion chambers only use a default value
    elif scanBean.isChooseNumberPoints():
        print "using default number of frames: 1000"
        numberPoints = 1000 # a default value as user has selected max possible but is only using the ion chambers (so max will be very high)
    # have a limit anyway of 4096
    else:
        numberPoints = scanBean.getNumberPoints()
        if numberPoints > 4096:
                raise "Too many frames for the given detector configuration." 
            
    # run the script held in outputparameters
    scriptName = beanGroup.getOutput().getBeforeScriptName()
    if scriptName != None and scriptName != "":
        scriptName = scriptName[scriptName.rfind("/") + 1:]
        run(scriptName)

    print "running QEXAFS scan:", energyController.getName(), scanBean.getInitialEnergy(), scanBean.getFinalEnergy(), numberPoints, scanBean.getTotalTime(), detectorList
    
    controller.update(None, ScriptProgressEvent("Running QEXAFS scan"))
    thisscan = ContinuousScan(energyController, scanBean.getInitialEnergy(), scanBean.getFinalEnergy(), numberPoints, scanBean.getTotalTime(), detectorList)

    controller.update(None,ScanCreationEvent(thisscan.getName()))
    thisscan.runScan()  
    controller.update(None,ScanFinishEvent(thisscan.getName(),ScanFinishEvent.FinishType.OK));

    
    scriptName = beanGroup.getOutput().getAfterScriptName()
    if scriptName != None and scriptName != "":
        scriptName = scriptName[scriptName.rfind("/") + 1:]
        run(scriptName)
    
    #remove added metadata from default metadata list to avoid multiple instances of the same metadata
    original_header=jython_mapper.original_header[:]
    Finder.getInstance().find("datawriterconfig").setHeader(original_header)
    
    
def xes (sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):
    
    # Create the beans from the file names
    xmlFolderName = ExafsEnvironment().getScriptFolder() + folderName + "/"
    sampleBean = BeansFactory.getBeanObject(xmlFolderName, sampleFileName)
    # this should be the xes bean
    xesScanBean = BeansFactory.getBeanObject(xmlFolderName, scanFileName)
    detectorBean = BeansFactory.getBeanObject(xmlFolderName, detectorFileName)
    outputBean = BeansFactory.getBeanObject(xmlFolderName, outputFileName)
     
    finder_mapper = FinderNameMapping()
    jython_mapper = JythonNameSpaceMapping()
     
    # give the beans to the xasdatawriter class to help define the folders/filenames 
    beanGroup = BeanGroup()
    beanGroup.setController(Finder.getInstance().find("ExafsScriptObserver"))
    beanGroup.setScriptFolder(xmlFolderName)
    beanGroup.setExperimentFolderName(folderName)
    beanGroup.setScanNumber(scanNumber)
    beanGroup.setSample(sampleBean)
    beanGroup.setDetector(detectorBean)
    beanGroup.setOutput(outputBean)

    beanGroup.setScan(xesScanBean)
    XasAsciiDataWriter.setBeanGroup(beanGroup)

    # work out which detectors to use (they will need to have been configured already by the GUI)
    detectorList = getDetectors(beanGroup.getDetector(), beanGroup.getOutput(), beanGroup.getScan()) 
    print "detectors to be used:", str(detectorList)
    
    # set up the sample 
    setup(beanGroup)
    
    # extract any signal parameters to add to the scan command
    # TODO need to add signal parameters to the qexafs scan, if possible
    signalParameters = getSignalList(outputBean)
    
    # run the scan
    if len(signalParameters) > 0:
        print "signal list:",str(signalParameters)
    
    # get the relevant objects from the namespace
    xes_energy = jython_mapper.XESEnergy
    mono_energy = jython_mapper.bragg1
    analyserAngle = jython_mapper.XESBragg
    #L = jython_mapper.xtal_x
    
    scanType = xesScanBean.getScanType()
    args = []
    
    originalDataFormat = LocalProperties.get("gda.data.scan.datawriter.dataFormat")
    
    from gda.exafs.xes.XesUtils import XesMaterial
    type = 1
    if xesScanBean.getAnalyserType() == str("Si"):
        type = 0
    xes_energy.setMaterialType(type)
    xes_energy.setCut1Val(xesScanBean.getAnalyserCut0())
    xes_energy.setCut2Val(xesScanBean.getAnalyserCut1())
    xes_energy.setCut3Val(xesScanBean.getAnalyserCut2())

    if scanType == XesScanParameters.SCAN_XES_FIXED_MONO:
        print "Scanning the analyser scan with fixed mono"
        print "switching data output format to XesAsciiNexusDataWriter"
        LocalProperties.set("gda.data.scan.datawriter.dataFormat","XesAsciiNexusDataWriter")
        args += [xes_energy, xesScanBean.getXesInitialEnergy(), xesScanBean.getXesFinalEnergy(), xesScanBean.getXesStepSize(), mono_energy, xesScanBean.getMonoEnergy()]
    
    elif scanType == XesScanParameters.SCAN_XES_SCAN_MONO:
        print "Scanning over the analyser and mono energies"
        print "switching data output format to XesAsciiNexusDataWriter"
        LocalProperties.set("gda.data.scan.datawriter.dataFormat","XesAsciiNexusDataWriter")
        args += [mono_energy, xesScanBean.getMonoInitialEnergy(), xesScanBean.getMonoFinalEnergy(), xesScanBean.getMonoStepSize(), xes_energy, xesScanBean.getXesInitialEnergy(), xesScanBean.getXesFinalEnergy(), xesScanBean.getXesStepSize()]

        # create scannable which will control 2D plotting in this mode
        jython_mapper.twodplotter.setX_colName(xes_energy.getInputNames()[0])
        jython_mapper.twodplotter.setY_colName(mono_energy.getInputNames()[0])
        jython_mapper.twodplotter.setZ_colName(finder_mapper.xmapMca.getExtraNames()[0])
        # note that users will have to open a 'plot 1' view or use the XESPlot perspective for this to work

        detectorList = [jython_mapper.twodplotter] + detectorList
    
    elif scanType == XesScanParameters.FIXED_XES_SCAN_XAS:
        print "Doing an EXAFS scan with a fixed analyser energy"
        # add xes_energy, analyserAngle to the defaults and then call the xas command
        xas_scanfilename = xesScanBean.getScanFileName()

        print "moving XES analyser stage to collect at", xesScanBean.getXesEnergy()
        xes_energy(xesScanBean.getXesEnergy())
        add_default(xes_energy)
        add_default(analyserAngle)

        try:
            xas(sampleBean, xas_scanfilename, detectorBean, outputBean, folderName, scanNumber, validation)
        finally:
            print "cleaning up scan defaults"
            remove_default(xes_energy)
            remove_default(analyserAngle)
        return
    
    elif scanType == XesScanParameters.FIXED_XES_SCAN_XANES:
        print "Doing a XANES scan with a fixed analyser energy"
        # add xes_energy, analyserAngle, to the signal parameters bean and then call the xanes command
        xanes_scanfilename = xesScanBean.getScanFileName()

        xes_energy(xesScanBean.getXesEnergy())
        print "moving XES analyser stage to collect at", xesScanBean.getXesEnergy()
        add_default(xes_energy)
        add_default(analyserAngle)

        try:
            xanes(sampleBean, xanes_scanfilename, detectorBean, outputBean, folderName, scanNumber, validation)
        finally:
            print "cleaning up scan defaults"
            remove_default(xes_energy)
            remove_default(analyserAngle)
        return
    else:
        raise "scan type in XES Scan Parameters bean/xml not acceptable"


    # run the script held in outputparameters
    scriptName = beanGroup.getOutput().getBeforeScriptName()
    if scriptName != None and scriptName != "":
        InterfaceProvider.getCommandRunner().runScript(File(scriptName), None);

    args += [analyserAngle]
    args += detectorList 
    args += [xesScanBean.getXesIntegrationTime()]
    if len(signalParameters) > 0:
        args += signalParameters
    print args 
    
    try:
        scan(args)
    finally:
        LocalProperties.set("gda.data.scan.datawriter.dataFormat", originalDataFormat)


def xanes (sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):
    xas(sampleFileName, scanFileName, detectorFileName, outputFileName, folderName, scanNumber, validation)
    return

def xas (sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):
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
        print "scanNumber", scanNumber
        print "validation", validation

    finder_mapper = FinderNameMapping()
    jython_mapper = JythonNameSpaceMapping()
    controller = Finder.getInstance().find("ExafsScriptObserver")

    # Create the beans from the file names
    xmlFolderName = ExafsEnvironment().getScriptFolder() + folderName + "/"
    print "xmlFolderName", xmlFolderName  
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
    print "Energy control supplied by scannable:", scanBean.getScannableName()
    xas_scannable.setEnergyScannable(Finder.getInstance().find(scanBean.getScannableName()))
     
    # give the beans to the xasdatawriter class to help define the folders/filenames 
    originalGroup = BeanGroup()
    originalGroup.setController(controller)
    originalGroup.setScriptFolder(xmlFolderName)
    originalGroup.setScannable(Finder.getInstance().find(scanBean.getScannableName())) #TODO
    originalGroup.setExperimentFolderName(folderName)
    originalGroup.setScanNumber(scanNumber)
    if (sampleBean != None):
        originalGroup.setSample(sampleBean)
    originalGroup.setDetector(detectorBean)
    originalGroup.setOutput(outputBean)
    originalGroup.setValidate(validation)
    originalGroup.setScan(scanBean)
    
    # Use BeanGroups to generate the DOE experiments.
    groups = BeanGroups.expand(originalGroup)
    infos = BeanGroups.getInfo(originalGroup)
    print "DoE group size", len(groups)
    
    for i in range(0, len(groups)):
        
        beanGroup = groups[i];
        
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
        
        # This step adds information to the sample parameters description 
        # if we are doing a multiple run.
        if len(groups) > 1:
            rangeInfo = infos[i]
            beanGroup.getSample().addDescription("");
            beanGroup.getSample().addDescription("***");
            beanGroup.getSample().addDescription("Design of experiments study run '" + str(i + 1) + "' of '" + str(len(groups)) + "'. With following parameter(s):")
            beanGroup.getSample().addDescription(rangeInfo.getHeader())
            beanGroup.getSample().addDescription(rangeInfo.getValues())
            beanGroup.getSample().addDescription("***");
            simpleLog("Loop of experiments: run " + str(i + 1) + " of " + str(len(groups)))
            simpleLog("Variable(s) being looped over:" + str(rangeInfo.getHeader()))
            simpleLog("Current value(s):" + str(rangeInfo.getValues()))

        # work out which detectors to use (they will need to have been configured already by the GUI)
        detectorList = getDetectors(beanGroup.getDetector(), beanGroup.getOutput(), beanGroup.getScan()) 
        xas_scannable.setDetectors(detectorList)
        
        
        
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
        
        simpleLog("about to scan")
        
        ScanBase.checkForInterrupts()

        try:
            controller.update(None, ScriptProgressEvent("Running scan"))
            thisscan = ConcurrentScan(args)
            controller.update(None, ScanCreationEvent(thisscan.getName()))
            thisscan.runScan()  
            #scan(args)
        finally:
        # clear the extra metadata which would have been added to the Nxues file (rebuilt everytime in setup)
            controller.update(None, ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK));
            NexusExtraMetadataDataWriter.removeAllMetadataEntries()
            LocalProperties.set("gda.scan.useScanPlotSettings", "false")
            LocalProperties.set("gda.plot.ScanPlotSettings.fromUserList", "false")
            finish(beanGroup)
 
        scriptName = beanGroup.getOutput().getAfterScriptName()
        if scriptName != None and scriptName != "":
            scriptName = scriptName[scriptName.rfind("/") + 1:]
            run(scriptName)

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
