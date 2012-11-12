#@PydevCodeAnalysisIgnore
from uk.ac.gda.client.microfocus.scan.datawriter import MicroFocusWriterExtender
from uk.ac.gda.beans import BeansFactory
from gda.factory import Finder
from gda.exafs.scan import BeanGroup
from exafsscripts.exafs.xas_scans import getDetectors
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
from gda.jython.commands import ScannableCommands
from gdascripts.messages import handle_messages
import time
from fast_scan import ScanPositionsTwoWay
from gda.device.scannable import ScannableUtils
from gda.data.scan.datawriter import TwoDScanRowReverser
from gda.data.scan.datawriter import XasAsciiNexusDatapointCompletingDataWriter
from gda.device.scannable import ScannableUtils

rootnamespace = {}

def rastermap (sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):

    origScanPlotSettings = LocalProperties.check("gda.scan.useScanPlotSettings")
    print detectorFileName
    origScanPlotSettings = LocalProperties.check("gda.scan.useScanPlotSettings")
 
    xmlFolderName = MicroFocusEnvironment().getScriptFolder() + folderName + "/"
    if(sampleFileName == None or sampleFileName == 'None'):
        sampleBean = None
    else:
        sampleBean   = BeansFactory.getBeanObject(xmlFolderName, sampleFileName)
        
    scanBean     = BeansFactory.getBeanObject(xmlFolderName, scanFileName)
    detectorBean = BeansFactory.getBeanObject(xmlFolderName, detectorFileName)
    #FOR NORMAL TRAJECTORY SCAN WITH VORTEX COMMENT THE FOLLOWING THREE LINES
    # 24July2012 three lines commented out by RW 
    #if(detectorBean.getFluorescenceParameters().getDetectorType() == "Silicon"):
    #    vortexRastermap(sampleFileName, scanFileName, detectorFileName, outputFileName, folderName, scanNumber, validation)
    #    return
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
    
    detectorList = getDetectors(detectorBean, outputBean, None) 

    print "about to setup"
    setupForRaster(beanGroup)
    print "after setup of raster"
    dataWriter = finder.find("DataWriterFactory")

    nx = ScannableUtils.getNumberSteps(Finder.getInstance().find(scanBean.getXScannableName()),scanBean.getXStart(), scanBean.getXEnd(),scanBean.getXStepSize()) + 1
    ny = ScannableUtils.getNumberSteps(Finder.getInstance().find(scanBean.getYScannableName()),scanBean.getYStart(), scanBean.getYEnd(),scanBean.getYStepSize()) + 1
    print "number of x points is ", str(nx)
    print "number of y points is ", str(ny)
    energyList = [scanBean.getEnergy()]
    zScannablePos = scanBean.getZValue()
    for energy in energyList:
        mfd = MicroFocusWriterExtender(nx, ny, scanBean.getXStepSize(), scanBean.getYStepSize())
        globals()["microfocusScanWriter"] = mfd
        mfd.setPlotName("MapPlot")
        print " the detector is " 
        print detectorList
        if(detectorBean.getExperimentType() == "Transmission"):
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
            detectorList.append(finder.find("counterTimer01"))
            detectorList.append(detector)  
            mfd.setDetectors(array(detectorList, gda.device.Detector))     
            mfd.setSelectedElement(selectedElement)
            mfd.getWindowsfromBean()
        mfd.setEnergyValue(energy)
        mfd.setZValue(zScannablePos)
        xScannable = finder.find(scanBean.getXScannableName())
        yScannable = finder.find(scanBean.getYScannableName())
        useFrames = False
        energyScannable = Finder.getInstance().find(scanBean.getEnergyScannableName())
        zScannable = Finder.getInstance().find(scanBean.getZScannableName())
        print "energy is ", str(energy)
        print detectorList
        energyScannable.moveTo(energy) 
        zScannable.moveTo(zScannablePos)
        scanStart = time.asctime()
        try:
            numberPoints = abs(scanBean.getXEnd()- scanBean.getXStart())/scanBean.getXStepSize()
            
            detectorType = detectorBean.getFluorescenceParameters().getDetectorType()
            
            if(detectorType == "Silicon"):                
                cs = ContinuousScan(trajectoryX, scanBean.getXStart(), scanBean.getXEnd(), nx, scanBean.getRowTime(), [raster_counterTimer01, raster_xmap]) 
                xmapRasterscan = ScannableCommands.createConcurrentScan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),cs,realX])
                xmapRasterscan.getScanPlotSettings().setIgnore(1)
                xasWriter = XasAsciiNexusDatapointCompletingDataWriter()
                xasWriter.addDataWriterExtender(mfd)
                xmapRasterscan.setDataWriter(xasWriter)
                xmapRasterscan.runScan()
            else:
                print "Xspress Raster Scan"
                xspressRasterscan = ScannableCommands.createConcurrentScan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),ContinuousScan(trajectoryX, scanBean.getXStart(), scanBean.getXEnd(), nx, scanBean.getRowTime(), [raster_counterTimer01, raster_xspress]),realX])
                xspressRasterscan.getScanPlotSettings().setIgnore(1)
                xasWriter = XasAsciiNexusDatapointCompletingDataWriter()
                xasWriter.addDataWriterExtender(mfd)
                xspressRasterscan.setDataWriter(xasWriter)
                xspressRasterscan.runScan()

        finally:
            scanEnd = time.asctime()
            if(origScanPlotSettings):
                LocalProperties.set("gda.scan.useScanPlotSettings", "true")
            else:
                LocalProperties.set("gda.scan.useScanPlotSettings", "false")
            handle_messages.simpleLog("map start time " + str(scanStart))
            handle_messages.simpleLog("map end time " + str(scanEnd))
            finish()

def setupForRaster(beanGroup):
    
    print "setting up raster scan"
    rasterscan = beanGroup.getScan()
    print "collection time is " , str(rasterscan.getRowTime())
    collectionTime = rasterscan.getRowTime()
    print "1setting collection time to" , str(collectionTime)  
    command_server = Finder.getInstance().find("command_server")    
    topupMonitor1 = command_server.getFromJythonNamespace("topupMonitor", None)    
    beam1 = command_server.getFromJythonNamespace("beam", None)
    detectorFillingMonitor1 = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
    trajBeamMonitor1 = command_server.getFromJythonNamespace("trajBeamMonitor", None)
    print "setting collection time to" , str(collectionTime)        
    if(not (topupMonitor1 == None)):
        topupMonitor1.setPauseBeforePoint(False)
        topupMonitor1.setPauseBeforeLine(True)
        topupMonitor1.setCollectionTime(collectionTime)
    if(not (beam1 == None)):
        add_default(beam1)
        beam1.setPauseBeforePoint(False)
        beam1.setPauseBeforeLine(True)
    if(beanGroup.getDetector().getExperimentType() == "Fluorescence" and beanGroup.getDetector().getFluorescenceParameters().getDetectorType() == "Germanium"and not (detectorFillingMonitor == None)):
        add_default(detectorFillingMonitor1)
        detectorFillingMonitor1.setPauseBeforePoint(False)
        detectorFillingMonitor1.setPauseBeforeLine(True)
        detectorFillingMonitor1.setCollectionTime(collectionTime)
        print "setting up raster scan 3"
        fullFileName = beanGroup.getScriptFolder() + beanGroup.getDetector().getFluorescenceParameters().getConfigFileName()
        bean = BeansFactory.getBean(File(fullFileName));
        bean.setReadoutMode(XspressDetector.READOUT_MCA);
        bean.setResGrade(ResGrades.NONE);
        elements = bean.getDetectorList();
        for element in elements: 
            rois = element.getRegionList();
            element.setWindow(rois.get(0).getRegionStart(), rois.get(0).getRegionEnd())
        BeansFactory.saveBean(File(fullFileName), bean)
    outputBean=beanGroup.getOutput()
    sampleParameters = beanGroup.getSample()
    outputBean.setAsciiFileName(sampleParameters.getName())
    print "Setting the ascii file name as " ,sampleParameters.getName()    
    att1 = sampleParameters.getAttenuatorParameter1()
    att2 = sampleParameters.getAttenuatorParameter2()
    pos([rootnamespace['D7A'], att1.getSelectedPosition(), rootnamespace['D7B'], att2.getSelectedPosition()])
    configFluoDetector(beanGroup)
    print "setting up raster scan"
    print "beangroup:", beanGroup
    print "dtector:", beanGroup.getDetector()
    print "experimenttype:", beanGroup.getDetector().getExperimentType()
  
    LocalProperties.set("gda.scan.useScanPlotSettings", "true")
    if not (trajBeamMonitor1 == None):
        trajBeamMonitor1.setActive(True)
    redefineNexusMetadataForMaps(beanGroup)
    finder.find("RCPController").openPesrpective("uk.ac.gda.microfocus.ui.MicroFocusPerspective")
    

def finish():
    command_server = Finder.getInstance().find("command_server")
    beam = command_server.getFromJythonNamespace("beam", None)
    detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
    remove_default(beam)
    remove_default(detectorFillingMonitor)

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