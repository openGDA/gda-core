#@PydevCodeAnalysisIgnore
from uk.ac.gda.client.microfocus.scan.datawriter import MicroFocusWriterExtender
from uk.ac.gda.beans import BeansFactory
from gda.factory import Finder
from gda.exafs.scan import BeanGroup
from exafsscripts.exafs.i18ScanScripts import getDetectors
from gda.jython.commands.ScannableCommands import scan, add_default, remove_default
from java.io import File
from java.lang import System
from gda.configuration.properties import LocalProperties
from jarray import array
from gda.data import PathConstructor
from gda.data.scan.datawriter import XasAsciiDataWriter
from gda.factory import Finder
from exafsscripts.exafs.configFluoDetector import configFluoDetector
from uk.ac.gda.beans import BeansFactory
from java.io import File
from gda.device.detector.xspress import XspressDetector
from gda.device.detector.xspress import ResGrades
from gdascripts.messages import handle_messages
from gda.jython.commands import ScannableCommands
from BeamlineParameters import JythonNameSpaceMapping
from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
from gda.device.scannable import ScannableUtils
rootnamespace = {}
def map (sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):

    origScanPlotSettings = LocalProperties.check("gda.scan.useScanPlotSettings")
    
    xmlFolderName = MicroFocusEnvironment().getScriptFolder() + folderName + "/"

    if(sampleFileName == None or sampleFileName == 'None'):
        sampleBean = None
    else:
        sampleBean  = BeansFactory.getBeanObject(xmlFolderName, sampleFileName)
        
    scanBean     = BeansFactory.getBeanObject(xmlFolderName, scanFileName)
    if(scanBean.isRaster()):
        rastermap(sampleFileName, scanFileName, detectorFileName, outputFileName, folderName, scanNumber, validation)
        return
    detectorBean = BeansFactory.getBeanObject(xmlFolderName, detectorFileName)
    outputBean   = BeansFactory.getBeanObject(xmlFolderName, outputFileName)

    beanGroup = BeanGroup()
    beanGroup.setController(Finder.getInstance().find("ExafsScriptObserver"))
    beanGroup.setScriptFolder(xmlFolderName)
    beanGroup.setScannable(Finder.getInstance().find(scanBean.getXScannableName())) #TODO
    beanGroup.setExperimentFolderName(folderName)
    beanGroup.setScanNumber(scanNumber)
    if(sampleBean != None):
        beanGroup.setSample(sampleBean)
    beanGroup.setDetector(detectorBean)
    beanGroup.setOutput(outputBean)
    beanGroup.setValidate(validation)
    beanGroup.setScan(scanBean)
    XasAsciiDataWriter.setBeanGroup(beanGroup)
    handle_messages.simpleLog("XasAsciiDataWriter.setBeanGroup(beanGroup)")
      
    detectorList = getDetectors(detectorBean, outputBean, None) 
    handle_messages.simpleLog("detectorList")
    print detectorList

    setupForMap(beanGroup)
    handle_messages.simpleLog("setupForMap")
    
    finder = Finder.getInstance()
    dataWriter = finder.find("DataWriterFactory")

    scannablex = Finder.getInstance().find(scanBean.getXScannableName())
    scannabley = Finder.getInstance().find(scanBean.getYScannableName())
    
    if scannablex==None:
        from gdascripts.parameters import beamline_parameters
        jythonNameMap = beamline_parameters.JythonNameSpaceMapping()
        scannablex=jythonNameMap.__getitem__(scanBean.getXScannableName())
        scannabley=jythonNameMap.__getitem__(scanBean.getYScannableName())
    
    nx = ScannableUtils.getNumberSteps(scannablex,scanBean.getXStart(), scanBean.getXEnd(),scanBean.getXStepSize()) + 1
    ny = ScannableUtils.getNumberSteps(scannabley,scanBean.getYStart(), scanBean.getYEnd(),scanBean.getYStepSize()) + 1

    print "number of x points is ", str(nx)
    print "number of y points is ", str(ny)
    energyList = [scanBean.getEnergy()]
    zScannablePos = scanBean.getZValue()
    for energy in energyList:
        mfd = MicroFocusWriterExtender(nx, ny, scanBean.getXStepSize(), scanBean.getYStepSize())
        zScannable = Finder.getInstance().find(scanBean.getZScannableName())
        globals()["microfocusScanWriter"] = mfd
        mfd.setPlotName("MapPlot")
        print " the detector is " 
        print detectorList
        if(detectorBean.getExperimentType() == "Transmission"):
            elements =getElementNamesfromIonChamber(detectorBean)
            mfd.setRoiNames(array(elements, java.lang.String))
            mfd.setSelectedElement("I0")
            mfd.setDetectors(array(detectorList, gda.device.Detector))
        else:   
            detectorType = detectorBean.getFluorescenceParameters().getDetectorType()
            if(folderName != None):
                detectorBeanFileName =MicroFocusEnvironment().getScriptFolder()+File.separator +folderName +File.separator+detectorBean.getFluorescenceParameters().getConfigFileName()
            else:
                detectorBeanFileName =MicroFocusEnvironment().getScriptFolder()+detectorBean.getFluorescenceParameters().getConfigFileName()
            print detectorBeanFileName
            elements = showElementsList(detectorBeanFileName)
            selectedElement = elements[0]
            mfd.setRoiNames(array(elements, java.lang.String))
            mfd.setDetectorBeanFileName(detectorBeanFileName)
            bean = BeansFactory.getBean(File(detectorBeanFileName))   
            detector = finder.find(bean.getDetectorName())   
            firstDetector = detectorList[0]
            detectorList=[]
            if (detectorBean.getFluorescenceParameters().getDetectorType() == "Germanium" ):
                detectorList.append(finder.find("counterTimer02"))
            else:
                detectorList.append(finder.find("counterTimer03"))
            detectorList.append(finder.find("counterTimer01"))
            detectorList.append(detector) 
           
            print " the detector second time is " 
            print detectorList 
            mfd.setDetectors(array(detectorList, gda.device.Detector))     
            mfd.setSelectedElement(selectedElement)
            mfd.getWindowsfromBean()
        mfd.setEnergyValue(energy)
        if(zScannablePos == None):
            mfd.setZValue(zScannable.getPosition())
        else:
            mfd.setZValue(zScannablePos)
        dataWriter.addDataWriterExtender(mfd)
        xScannable = finder.find(scanBean.getXScannableName())
        if xScannable is None:
            xScannable =   globals()[scanBean.getXScannableName()]
        yScannable = finder.find(scanBean.getYScannableName())
        if yScannable is None:
            yScannable = globals()[scanBean.getYScannableName()]
        useFrames = LocalProperties.check("gda.microfocus.scans.useFrames")
        print "using frames ", str(useFrames)
        energyScannable = Finder.getInstance().find(scanBean.getEnergyScannableName())
        
        print "energy is ", str(energy)

        print detectorList
        energyScannable.moveTo(energy) 
        print zScannable
        if(zScannablePos != None):
            zScannable.moveTo(zScannablePos)
        scanBean.setCollectionTime(scanBean.getCollectionTime())
        args=[yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),  xScannable, scanBean.getXStart(), scanBean.getXEnd(),  scanBean.getXStepSize()]
        
        if(detectorBean.getExperimentType() == "Fluorescence" and useFrames):
            args+= detectorList
            counterTimer01.clearFrameSets()
            print "setting the collection time for frames as ", str(scanBean.getCollectionTime()*1000.0)
            counterTimer01.addFrameSet(int(nx),1.0E-4,scanBean.getCollectionTime()*1000.0,0,7,-1,0)
        else:
            for detector in detectorList:
                args.append(detector)              
                args.append(scanBean.getCollectionTime())
        print args
        scanStart = time.asctime()
        
        redefineNexusMetadataForMaps(beanGroup)
        
        try:
            mapscan= ScannableCommands.createConcurrentScan(args)
            print mapscan.getScanDataPointQueueLength()
            mapscan.getScanPlotSettings().setIgnore(1)
            mapscan.runScan()
            
        finally:
            scanEnd = time.asctime()
            if(origScanPlotSettings):
                LocalProperties.set("gda.scan.useScanPlotSettings", "true")
            else:
                LocalProperties.set("gda.scan.useScanPlotSettings", "false")
            handle_messages.simpleLog("map start time " + str(scanStart))
            handle_messages.simpleLog("map end time " + str(scanEnd)) 
            dataWriter.removeDataWriterExtender(mfd)
            finish()

def finish():
    command_server = Finder.getInstance().find("command_server")
    beam = command_server.getFromJythonNamespace("beam", None)
    detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
    remove_default(beam)
    remove_default(detectorFillingMonitor)

def setupForMap(beanGroup):
    if beanGroup.getDetector().getExperimentType() == "Fluorescence":
        if (beanGroup.getDetector().getFluorescenceParameters().getDetectorType() == "Germanium" ):
            fullFileName = beanGroup.getScriptFolder() + beanGroup.getDetector().getFluorescenceParameters().getConfigFileName()
            bean = BeansFactory.getBean(File(fullFileName));
            bean.setReadoutMode(XspressDetector.READOUT_MCA);
            bean.setResGrade(ResGrades.NONE);
            elements = bean.getDetectorList();
            for element in elements: 
                rois = element.getRegionList();
                element.setWindow(rois.get(0).getRegionStart(), rois.get(0).getRegionEnd())
            BeansFactory.saveBean(File(fullFileName), bean)
        configFluoDetector(beanGroup)
    scan = beanGroup.getScan()

    collectionTime = scan.getCollectionTime()
    command_server = Finder.getInstance().find("command_server")    
    topupMonitor = command_server.getFromJythonNamespace("topupMonitor", None)    
    beam = command_server.getFromJythonNamespace("beam", None)
    detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
    trajBeamMonitor = command_server.getFromJythonNamespace("trajBeamMonitor", None)
    print "setting collection time to" , str(collectionTime)        
    topupMonitor.setPauseBeforePoint(True)
    topupMonitor.setCollectionTime(collectionTime)
    add_default(beam)
    beam.setPauseBeforePoint(True)
    topupMonitor.setPauseBeforePoint(True)
    topupMonitor.setPauseBeforeLine(False)
    topupMonitor.setCollectionTime(collectionTime)
    beam.setPauseBeforePoint(True)
    beam.setPauseBeforeLine(True)
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
    att1 = sampleParameters.getAttenuatorParameter1()
    att2 = sampleParameters.getAttenuatorParameter2()
    pos([rootnamespace['D7A'], att1.getSelectedPosition(), rootnamespace['D7B'], att2.getSelectedPosition()])
    LocalProperties.set("gda.scan.useScanPlotSettings", "true")
    finder.find("RCPController").openPesrpective("uk.ac.gda.microfocus.ui.MicroFocusPerspective")
    
def redefineNexusMetadataForMaps(beanGroup):
    from gda.data.scan.datawriter import NexusFileMetadata
    from gda.data.scan.datawriter.NexusFileMetadata import EntryTypes, NXinstrumentSubTypes
    
    jython_mapper = JythonNameSpaceMapping()
    
    if (LocalProperties.get("gda.mode") == 'dummy'):
        return
    
    NexusExtraMetadataDataWriter.removeAllMetadataEntries()
    
    # primary slits
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("s1ygap", str(jython_mapper.s1ygap()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXaperture, "primary_slits"))
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("s1xgap", str(jython_mapper.s1xgap()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXaperture, "primary_slits"))
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("s1ypos", str(jython_mapper.s1ypos()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXaperture, "primary_slits"))
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("s1xpos", str(jython_mapper.s1xpos()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXaperture, "primary_slits"))

    # secondary slits
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("s2ygap", str(jython_mapper.s2ygap()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXaperture, "secondary_slits"))
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("s2xgap", str(jython_mapper.s2xgap()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXaperture, "secondary_slits"))
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("s2ypos", str(jython_mapper.s2ypos()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXaperture, "secondary_slits"))
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("s2xpos", str(jython_mapper.s2xpos()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXaperture, "secondary_slits"))

    # post DCM slits
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("s3ygap", str(jython_mapper.s3ygap()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXaperture, "postDCM_slits"))
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("s3xgap", str(jython_mapper.s3xgap()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXaperture, "postDCM_slits"))
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("s3ypos", str(jython_mapper.s3ypos()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXaperture, "postDCM_slits"))
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("s3xpos", str(jython_mapper.s3xpos()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXaperture, "postDCM_slits"))
    
    # Sample Stage
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("sc_sample_z", str(jython_mapper.sc_sample_z()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXsample_stage, "Sample_Stage"))
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("sc_sample_thetacoarse", str(jython_mapper.sc_sample_thetacoarse()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXsample_stage, "Sample_Stage"))
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("sc_sample_thetafine", str(jython_mapper.sc_sample_thetafine()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXsample_stage, "Sample_Stage"))

    #attenustors
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("D7A", str(jython_mapper.D7A()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXattenuator, "Attenuators"))
    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("D7B", str(jython_mapper.D7B()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXattenuator, "Attenuators"))

    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("energy", str(jython_mapper.energy()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXmonochromator, "DCM_energy"))
  
class MicroFocusEnvironment:
    testScriptFolder=None
    def getScriptFolder(self):
        if MicroFocusEnvironment.testScriptFolder != None:
            return MicroFocusEnvironment.testScriptFolder
        dataDirectory = PathConstructor.createFromDefaultProperty()
        return dataDirectory + "/xml/"

    testScannable=None
    def getScannable(self):
        if MicroFocusEnvironment.testScannable != None:
            return MicroFocusEnvironment.testScannable
        return None