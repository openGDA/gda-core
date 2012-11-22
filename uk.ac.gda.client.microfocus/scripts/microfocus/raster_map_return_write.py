from uk.ac.gda.client.microfocus.scan.datawriter import TwoWayMicroFocusWriterExtender
from uk.ac.gda.beans import BeansFactory
from gda.exafs.scan import BeanGroup
from java.io import File
from jarray import array
from gda.data import PathConstructor
from gda.data.scan.datawriter import XasAsciiDataWriter
from gda.factory import Finder
from gda.scan import TrajectoryScanLine
from uk.ac.gda.client.microfocus.util import ScanPositionsTwoWay
from gda.device.scannable import ScannableUtils
from gda.data.scan.datawriter import TwoDScanRowReverser
from gda.data.scan.datawriter import XasAsciiNexusDatapointCompletingDataWriter
import time
from gda.configuration.properties import LocalProperties
from map import Map
from microfocus_elements import showElementsList
from exafsscripts.exafs.configFluoDetector import configFluoDetector
from gda.jython.commands import ScannableCommands
from java.lang import String
from gda.device import Detector
from gda.scan import ContinuousScan

class RasterMapReturnWrite(Map):
    
    def __init__(self, d7a, d7b, counterTimer01, trajectoryX, raster_counterTimer01, raster_xmap, realX, HTScaler, HTXmapMca, datadir):
        self.d7a=d7a
        self.d7b=d7b
        self.counterTimer01=counterTimer01
        self.finder = Finder.getInstance()
        self.trajectoryX=trajectoryX
        self.raster_counterTimer01=raster_counterTimer01
        self.raster_xmap=raster_xmap
        self.realX=realX
        self.HTScaler=HTScaler
        self.HTXmapMca=HTXmapMca
        self.datadir=datadir
        
    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):

        print detectorFileName
        origScanPlotSettings = LocalProperties.check("gda.scan.useScanPlotSettings")
        
        xmlFolderName = self.datadir + folderName + "/"
    
        if(sampleFileName == None or sampleFileName == 'None'):
            sampleBean = None
        else:
            sampleBean   = BeansFactory.getBeanObject(xmlFolderName, sampleFileName)
            
        scanBean     = BeansFactory.getBeanObject(xmlFolderName, scanFileName)
        detectorBean = BeansFactory.getBeanObject(xmlFolderName, detectorFileName)
        outputBean   = BeansFactory.getBeanObject(xmlFolderName, outputFileName)
    
        beanGroup = BeanGroup()
        beanGroup.setController(self.finder.find("ExafsScriptObserver"))
        beanGroup.setScriptFolder(xmlFolderName)
        beanGroup.setScannable(self.finder.find(scanBean.getXScannableName())) #TODO
        beanGroup.setExperimentFolderName(folderName)
        beanGroup.setScanNumber(scanNumber)
        if(sampleBean != None):
            beanGroup.setSample(sampleBean)
        beanGroup.setDetector(detectorBean)
        beanGroup.setOutput(outputBean)
        beanGroup.setValidate(validation)
        beanGroup.setScan(scanBean)
        XasAsciiDataWriter.setBeanGroup(beanGroup)
    
        detectorList = self.getDetectors(detectorBean, outputBean, None) 
       
        self.setup(beanGroup)
        
        finder = self.finder
        dataWriter = finder.find("DataWriterFactory")
        nx = ScannableUtils.getNumberSteps(self.finder.find(scanBean.getXScannableName()),scanBean.getXStart(), scanBean.getXEnd(),scanBean.getXStepSize()) + 1
        ny = ScannableUtils.getNumberSteps(self.finder.find(scanBean.getYScannableName()),scanBean.getYStart(), scanBean.getYEnd(),scanBean.getYStepSize()) + 1
        
        print "number of x points is ", str(nx)
        print "number of y points is ", str(ny)
       
        energyList = [scanBean.getEnergy()]
        zScannablePos = scanBean.getZValue()
        for energy in energyList:
            mfd = TwoWayMicroFocusWriterExtender(nx, ny, scanBean.getXStepSize(), scanBean.getYStepSize())
            globals()["microfocusScanWriter"] = mfd
            mfd.setPlotName("MapPlot")
            print " the detector is " 
            print detectorList
            if(detectorBean.getExperimentType() == "Transmission"):
                mfd.setSelectedElement("I0")
                mfd.setDetectors(array(detectorList, Detector))
            else:   
                detectorType = detectorBean.getFluorescenceParameters().getDetectorType()
                #should get the bean file name from detector parametrs
                if(folderName != None):
                    detectorBeanFileName =self.datadir+File.separator +folderName +File.separator+detectorBean.getFluorescenceParameters().getConfigFileName()
                else:
                    detectorBeanFileName =self.datadir+detectorBean.getFluorescenceParameters().getConfigFileName()
                print detectorBeanFileName
                elements = showElementsList(detectorBeanFileName)
                ##this should be the element selected in the gui
                selectedElement = elements[0]
                mfd.setRoiNames(array(elements, String))
    
                mfd.setDetectorBeanFileName(detectorBeanFileName)
                bean = BeansFactory.getBean(File(detectorBeanFileName))   
                detector = finder.find(bean.getDetectorName())   
                firstDetector = detectorList[0]
                detectorList=[]
                detectorList.append(finder.find("counterTimer01"))
                detectorList.append(detector)  
                mfd.setDetectors(array(detectorList, Detector))     
                mfd.setSelectedElement(selectedElement)
                mfd.getWindowsfromBean()
            mfd.setEnergyValue(energy)
            mfd.setZValue(zScannablePos)
            
            xScannable = finder.find(scanBean.getXScannableName())
            yScannable = finder.find(scanBean.getYScannableName())
            useFrames = False
            energyScannable = self.finder.find(scanBean.getEnergyScannableName())
            zScannable = self.finder.find(scanBean.getZScannableName())
            print "energy is ", str(energy)
            print "energy scannable is " 
            print energyScannable  
            print detectorList
            energyScannable.moveTo(energy) 
            zScannable.moveTo(zScannablePos)
    
            self.redefineNexusMetadataForMaps(beanGroup)
    
            scanStart = time.asctime()
            try:
                numberPoints = abs(scanBean.getXEnd()- scanBean.getXStart())/scanBean.getXStepSize()
                if(detectorType == "Silicon"):
                    self.HTScaler.setIntegrateBetweenPoints(True)
                    self.HTXmapMca.setIntegrateBetweenPoints(True)
                    self.HTScaler.setCollectionTime(scanBean.getRowTime() / nx)
                    self.HTXmapMca.setCollectionTime(scanBean.getRowTime() / nx)
                    self.HTScaler.setScanNumberOfPoints(nx)
                    self.HTXmapMca.setScanNumberOfPoints(nx)                
    
                    tsl = TrajectoryScanLine([self.continuousSampleX, ScanPositionsTwoWay(self.continuousSampleX,scanBean.getXStart(), scanBean.getXEnd(), scanBean.getXStepSize()),  self.HTScaler, self.HTXmapMca, scanBean.getRowTime()/(nx)] )
                    tsl.setScanDataPointQueueLength(10000);tsl.setPositionCallableThreadPoolSize(10)
    
                    xmapRasterscan = ScannableCommands.createConcurrentScan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),tsl, self.realX])
                    xmapRasterscan.getScanPlotSettings().setIgnore(1)
                    
                    xasWriter = XasAsciiNexusDatapointCompletingDataWriter()
                    rowR = TwoDScanRowReverser()
                    rowR.setNoOfColumns(nx)
                    rowR.setNoOfRows(ny)
                    rowR.setReverseOdd(True)
                    xasWriter.setIndexer(rowR)
                    xasWriter.addDataWriterExtender(mfd)
                    xmapRasterscan.setDataWriter(xasWriter)
                    xmapRasterscan.runScan()
                else:
                    xspressRasterscan = ScannableCommands.createConcurrentScan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),ContinuousScan(self.trajectoryX, scanBean.getXStart(), scanBean.getXEnd(), nx, scanBean.getRowTime(), [self.raster_counterTimer01, self.raster_xspress]),self.realX])
                    xspressRasterscan.getScanPlotSettings().setIgnore(1)
                    xspressRasterscan.runScan()
    
            finally:
                scanEnd = time.asctime()
                if(origScanPlotSettings):
                    LocalProperties.set("gda.scan.useScanPlotSettings", "true")
                else:
                    LocalProperties.set("gda.scan.useScanPlotSettings", "false")
                print "map start time " + str(scanStart)
                print "map end time " + str(scanEnd)
                self.finish()
        
    def setup(self, beanGroup):
        rasterscan = beanGroup.getScan()
        print "collection time is " , str(rasterscan.getRowTime())   
        collectionTime = rasterscan.getRowTime()
        print "1setting collection time to" , str(collectionTime)  
        command_server = self.finder.find("command_server")    
        topupMonitor = command_server.getFromJythonNamespace("topupMonitor", None)    
        beam = command_server.getFromJythonNamespace("beam", None)
        detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
        trajBeamMonitor = command_server.getFromJythonNamespace("trajBeamMonitor", None)
        print "setting collection time to" , str(collectionTime)
        if(not (topupMonitor == None)):        
            topupMonitor.setPauseBeforePoint(False)
            topupMonitor.setPauseBeforeLine(True)
            topupMonitor.setCollectionTime(collectionTime)
        if(not (beam == None)):
            self.finder.find("command_server").addDefault(beam);
            beam.setPauseBeforePoint(False)
            beam.setPauseBeforeLine(True)
        if(beanGroup.getDetector().getExperimentType() == "Fluorescence" and beanGroup.getDetector().getFluorescenceParameters().getDetectorType() == "Germanium" and not (detectorFillingMonitor == None)):
            self.finder.find("command_server").addDefault(detectorFillingMonitor);
            detectorFillingMonitor.setPauseBeforePoint(False)
            detectorFillingMonitor.setPauseBeforeLine(True)
            detectorFillingMonitor.setCollectionTime(collectionTime)
        if(not (trajBeamMonitor == None)):
            trajBeamMonitor.setActive(True)
            
        outputBean=beanGroup.getOutput()
        sampleParameters = beanGroup.getSample()
        outputBean.setAsciiFileName(sampleParameters.getName())
        print "Setting the ascii file name as " ,sampleParameters.getName()    
        att1 = sampleParameters.getAttenuatorParameter1()
        att2 = sampleParameters.getAttenuatorParameter2()
        self.d7a(att1.getSelectedPosition())
        self.d7b(att2.getSelectedPosition())
        configFluoDetector(beanGroup)
        LocalProperties.set("gda.scan.useScanPlotSettings", "true")
        self.finder.find("RCPController").openPesrpective("uk.ac.gda.microfocus.ui.MicroFocusPerspective")