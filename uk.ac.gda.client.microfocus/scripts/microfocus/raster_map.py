from uk.ac.gda.client.microfocus.scan.datawriter import MicroFocusWriterExtender
from uk.ac.gda.beans import BeansFactory
from gda.factory import Finder
from gda.exafs.scan import BeanGroup
from gda.jython.commands.ScannableCommands import scan
from java.io import File
from java.lang import System
from gda.configuration.properties import LocalProperties
from jarray import array
from gda.data import PathConstructor
from gda.data.scan.datawriter import XasAsciiDataWriter
from exafsscripts.exafs.configFluoDetector import configFluoDetector
from gda.device.detector.xspress import XspressDetector, ResGrades
from gda.jython.commands import ScannableCommands
from gdascripts.messages import handle_messages
from gda.device.scannable import ScannableUtils
from gda.data.scan.datawriter import XasAsciiNexusDatapointCompletingDataWriter
from map import Map
from microfocus_elements import showElementsList, getElementNamesfromIonChamber
from gda.scan import ContinuousScan
import time
from java.lang import String
from gda.device import Detector

class RasterMap(Map):
    
    def __init__(self, d7a, d7b, counterTimer01, trajectoryX, raster_counterTimer01, raster_xmap, realX, datadir):
        self.d7a=d7a
        self.d7b=d7b
        self.counterTimer01=counterTimer01
        self.finder = Finder.getInstance()
        self.trajectoryX=trajectoryX
        self.raster_counterTimer01=raster_counterTimer01
        self.raster_xmap=raster_xmap
        self.realX=realX
        self.datadir=datadir
        
    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):

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
    
        print "about to setup"
        self.setupForRaster(beanGroup)
        print "after setup of raster"
        dataWriter = self.finder.find("DataWriterFactory")
    
        nx = ScannableUtils.getNumberSteps(self.finder.find(scanBean.getXScannableName()),scanBean.getXStart(), scanBean.getXEnd(),scanBean.getXStepSize()) + 1
        ny = ScannableUtils.getNumberSteps(self.finder.find(scanBean.getYScannableName()),scanBean.getYStart(), scanBean.getYEnd(),scanBean.getYStepSize()) + 1
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
                mfd.setDetectors(array(detectorList, Detector))
            else:   
                detectorType = detectorBean.getFluorescenceParameters().getDetectorType()
                if(folderName != None):
                    detectorBeanFileName =self.datadir+File.separator +folderName +File.separator+detectorBean.getFluorescenceParameters().getConfigFileName()
                else:
                    detectorBeanFileName =self.datadir+detectorBean.getFluorescenceParameters().getConfigFileName()
                print detectorBeanFileName
                elements = showElementsList(detectorBeanFileName)
                selectedElement = elements[0]
                mfd.setRoiNames(array(elements, String))
                mfd.setDetectorBeanFileName(detectorBeanFileName)
                bean = BeansFactory.getBean(File(detectorBeanFileName))   
                detector = self.finder.find(bean.getDetectorName())   
                firstDetector = detectorList[0]
                detectorList=[]
                detectorList.append(self.finder.find("counterTimer01"))
                detectorList.append(detector)  
                mfd.setDetectors(array(detectorList, Detector))     
                mfd.setSelectedElement(selectedElement)
                mfd.getWindowsfromBean()
            mfd.setEnergyValue(energy)
            mfd.setZValue(zScannablePos)
            xScannable = self.finder.find(scanBean.getXScannableName())
            yScannable = self.finder.find(scanBean.getYScannableName())
            useFrames = False
            energyScannable = self.finder.find(scanBean.getEnergyScannableName())
            zScannable = self.finder.find(scanBean.getZScannableName())
            print "energy is ", str(energy)
            print detectorList
            energyScannable.moveTo(energy) 
            zScannable.moveTo(zScannablePos)
            scanStart = time.asctime()
            
            self.redefineNexusMetadataForMaps(beanGroup)
            
            try:
                numberPoints = abs(scanBean.getXEnd()- scanBean.getXStart())/scanBean.getXStepSize()
                
                detectorType = detectorBean.getFluorescenceParameters().getDetectorType()
                
                if(detectorType == "Silicon"):                
                    cs = ContinuousScan(self.trajectoryX, scanBean.getXStart(), scanBean.getXEnd(), nx, scanBean.getRowTime(), [self.raster_counterTimer01, self.raster_xmap]) 
                    xmapRasterscan = ScannableCommands.createConcurrentScan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),cs,self.realX])
                    xmapRasterscan.getScanPlotSettings().setIgnore(1)
                    xasWriter = XasAsciiNexusDatapointCompletingDataWriter()
                    xasWriter.addDataWriterExtender(mfd)
                    xmapRasterscan.setDataWriter(xasWriter)
                    xmapRasterscan.runScan()
                else:
                    print "Xspress Raster Scan"
                    xspressRasterscan = ScannableCommands.createConcurrentScan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),ContinuousScan(self.trajectoryX, scanBean.getXStart(), scanBean.getXEnd(), nx, scanBean.getRowTime(), [self.raster_counterTimer01, self.raster_xspress]),self.realX])
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
                self.finish()
    
    def setupForRaster(self, beanGroup):
        
        print "setting up raster scan"
        rasterscan = beanGroup.getScan()
        print "collection time is " , str(rasterscan.getRowTime())
        collectionTime = rasterscan.getRowTime()
        print "1setting collection time to" , str(collectionTime)  
        command_server = self.finder.find("command_server")    
        topupMonitor1 = command_server.getFromJythonNamespace("topupMonitor", None)    
        beam1 = command_server.getFromJythonNamespace("beam", None)
        detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
        trajBeamMonitor1 = command_server.getFromJythonNamespace("trajBeamMonitor", None)
        print "setting collection time to" , str(collectionTime)        
        if(not (topupMonitor1 == None)):
            topupMonitor1.setPauseBeforePoint(False)
            topupMonitor1.setPauseBeforeLine(True)
            topupMonitor1.setCollectionTime(collectionTime)
        if(not (beam1 == None)):
            self.finder.find("command_server").addDefault(beam1);
            beam1.setPauseBeforePoint(False)
            beam1.setPauseBeforeLine(True)
        if(beanGroup.getDetector().getExperimentType() == "Fluorescence" and beanGroup.getDetector().getFluorescenceParameters().getDetectorType() == "Germanium"and not (detectorFillingMonitor == None)):
            self.finder.find("command_server").addDefault(detectorFillingMonitor);
            detectorFillingMonitor.setPauseBeforePoint(False)
            detectorFillingMonitor.setPauseBeforeLine(True)
            detectorFillingMonitor.setCollectionTime(collectionTime)
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
        self.d7a(att1.getSelectedPosition())
        self.d7b(att2.getSelectedPosition())
        configFluoDetector(beanGroup)
        print "setting up raster scan"
        print "beangroup:", beanGroup
        print "dtector:", beanGroup.getDetector()
        print "experimenttype:", beanGroup.getDetector().getExperimentType()
      
        LocalProperties.set("gda.scan.useScanPlotSettings", "true")
        if not (trajBeamMonitor1 == None):
            trajBeamMonitor1.setActive(True)
        self.finder.find("RCPController").openPesrpective("uk.ac.gda.microfocus.ui.MicroFocusPerspective")