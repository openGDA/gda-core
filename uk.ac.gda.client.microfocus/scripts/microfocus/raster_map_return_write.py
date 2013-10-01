from uk.ac.gda.client.microfocus.scan.datawriter import TwoWayMicroFocusWriterExtender
from uk.ac.gda.beans import BeansFactory
from gda.exafs.scan import BeanGroup
from java.io import File
from jarray import array
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

import java.lang.Exception

class RasterMapReturnWrite(Map):
    
    def __init__(self, d7a, d7b, counterTimer01, raster_xmap, traj1PositionReader, traj3PositionReader, traj1tfg, traj1xmap,traj3tfg, traj3xmap, traj1SampleX, traj3SampleX, raster_xspress, rcpController, ExafsScriptObserver):
        self.d7a=d7a
        self.d7b=d7b
        self.counterTimer01=counterTimer01
        self.finder = Finder.getInstance()
        self.raster_xmap=raster_xmap
        self.traj1SampleX=traj1SampleX
        self.traj3SampleX=traj3SampleX
        self.trajSampleX=traj1SampleX
        self.traj1PositionReader=traj1PositionReader
        self.traj3PositionReader=traj3PositionReader
        self.trajPositionReader=traj1PositionReader
        self.traj1tfg=traj1tfg
        self.traj3tfg=traj3tfg
        self.trajtfg=traj1tfg
        self.traj1xmap=traj1xmap
        self.traj3xmap=traj3xmap
        self.trajxmap=traj1xmap
        self.mfd = None
        self.detectorBeanFileName=""
        self.raster_xspress = raster_xspress
        self.rcpController = rcpController
        self.beamEnabled = True
        self.ExafsScriptObserver = ExafsScriptObserver
        
    def enableBeam(self):
        self.beamEnabled = True
    
    def disableBeam(self):
        self.beamEnabled = False
    
    def setStage(self, stage):
        if stage==1:
            self.trajSampleX = self.traj1SampleX
            self.trajPositionReader = self.traj1PositionReader
            self.trajtfg=self.traj1tfg
            self.trajtfg.setTtlSocket(1)
            self.trajxmap=self.traj1xmap
        elif stage==3:
            self.trajSampleX = self.traj3SampleX
            self.trajPositionReader = self.traj3PositionReader
            self.trajtfg=self.traj3tfg
            self.trajtfg.setTtlSocket(2)
            self.trajxmap=self.traj3xmap
        else:
            print "please enter 1 or 3 as a parameter where 1 is the small stage and 3 is the large stage"
    
    def getMFD(self):
        return self.mfd
    
    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):

        print detectorFileName
        origScanPlotSettings = LocalProperties.check("gda.scan.useScanPlotSettings")
        
        xmlFolderName = folderName + "/"
        folderName = folderName[folderName.find("xml")+4:]
        if(sampleFileName == None or sampleFileName == 'None'):
            sampleBean = None
        else:
            sampleBean   = BeansFactory.getBeanObject(xmlFolderName, sampleFileName)
            
        scanBean     = BeansFactory.getBeanObject(xmlFolderName, scanFileName)
        detectorBean = BeansFactory.getBeanObject(xmlFolderName, detectorFileName)
        outputBean   = BeansFactory.getBeanObject(xmlFolderName, outputFileName)
    
        beanGroup = BeanGroup()
        beanGroup.setController(self.ExafsScriptObserver)
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
    
        detectorList = self.getDetectors(detectorBean, outputBean) 
       
        self.setup(beanGroup)
        
        finder = self.finder
        nx = ScannableUtils.getNumberSteps(finder.find(scanBean.getXScannableName()),scanBean.getXStart(), scanBean.getXEnd(),scanBean.getXStepSize()) + 1
        ny = ScannableUtils.getNumberSteps(finder.find(scanBean.getYScannableName()),scanBean.getYStart(), scanBean.getYEnd(),scanBean.getYStepSize()) + 1
        
        print "number of x points is ", str(nx)
        print "number of y points is ", str(ny)
       
        energyList = [scanBean.getEnergy()]
        zScannablePos = scanBean.getZValue()
        
        for energy in energyList:
            self.mfd = TwoWayMicroFocusWriterExtender(nx, ny, scanBean.getXStepSize(), scanBean.getYStepSize())
            globals()["microfocusScanWriter"] = self.mfd # TODO I think this can be removed but check
            self.mfd.setPlotName("MapPlot")
            print " the detector is " 
            print detectorList
            if(detectorBean.getExperimentType() == "Transmission"):
                self.mfd.setSelectedElement("I0")
                self.mfd.setDetectors(array(detectorList, Detector))
            else:   
                detectorType = detectorBean.getFluorescenceParameters().getDetectorType()
                #should get the bean file name from detector parametrs
                if(folderName != None):
                    self.detectorBeanFileName =xmlFolderName+detectorBean.getFluorescenceParameters().getConfigFileName()
                else:
                    self.detectorBeanFileName =xmlFolderName+detectorBean.getFluorescenceParameters().getConfigFileName()
                print self.detectorBeanFileName
                elements = showElementsList(self.detectorBeanFileName)
                ##this should be the element selected in the gui
                selectedElement = elements[0]
                self.mfd.setRoiNames(array(elements, String))
    
                self.mfd.setDetectorBeanFileName(self.detectorBeanFileName)
                bean = BeansFactory.getBean(File(self.detectorBeanFileName))   
                detector = finder.find(bean.getDetectorName())
                detectorList=[]
                detectorList.append(self.counterTimer01)
                detectorList.append(detector)  
                self.mfd.setDetectors(array(detectorList, Detector))
                self.mfd.setSelectedElement(selectedElement)
                self.mfd.getWindowsfromBean()
            self.mfd.setEnergyValue(energy)
            self.mfd.setZValue(zScannablePos)
            
            yScannable = finder.find(scanBean.getYScannableName())
            energyScannable = finder.find(scanBean.getEnergyScannableName())
            zScannable = finder.find(scanBean.getZScannableName())
            print "energy is ", str(energy)
            print "energy scannable is " 
            print energyScannable  
            print detectorList
            energyScannable.moveTo(energy) 
            zScannable.moveTo(zScannablePos)
    
            self.redefineNexusMetadataForMaps(beanGroup)
    
            scanStart = time.asctime()
            try:
                if(detectorType == "Silicon"):
                    point_collection_time = scanBean.getRowTime() / nx
                    self.trajtfg.setIntegrateBetweenPoints(True)
                    self.trajxmap.setIntegrateBetweenPoints(True)
                    self.trajtfg.setCollectionTime(point_collection_time)
                    self.trajxmap.setCollectionTime(point_collection_time)
                    self.trajxmap.setScanNumberOfPoints(nx)
                    sptw= ScanPositionsTwoWay(self.trajSampleX,scanBean.getXStart(), scanBean.getXEnd(), scanBean.getXStepSize())
                    tsl = TrajectoryScanLine([self.trajSampleX, sptw,  self.trajtfg, self.trajxmap, scanBean.getRowTime()/(nx)] )
                    tsl.setScanDataPointQueueLength(10000)
                    tsl.setPositionCallableThreadPoolSize(10)
                    xmapRasterscan = ScannableCommands.createConcurrentScan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),tsl, self.trajPositionReader])
                    xmapRasterscan.getScanPlotSettings().setIgnore(1)
                    xasWriter = XasAsciiNexusDatapointCompletingDataWriter()
                    rowR = TwoDScanRowReverser()
                    rowR.setNoOfColumns(nx)
                    rowR.setNoOfRows(ny)
                    rowR.setReverseOdd(True)
                    xasWriter.setIndexer(rowR)
                    xasWriter.addDataWriterExtender(self.mfd)
                    xmapRasterscan.setDataWriter(xasWriter)
                    self.finder.find("elementListScriptController").update(None, self.detectorBeanFileName);
                    xmapRasterscan.runScan()
    
            except (Exception, java.lang.Exception), scan_exception:
                print "Handling exception raised during scan"
                if(origScanPlotSettings):
                    LocalProperties.set("gda.scan.useScanPlotSettings", "true")
                else:
                    LocalProperties.set("gda.scan.useScanPlotSettings", "false")
                try:
                    self.finish()
                except (Exception, java.lang.Exception), e:
                    print "Exception while calling finish() during cleanup. Catching this inorder to reraise the *original* Exception"
                    print "Caught exception:", e
                    print "Raising original exception"
                raise scan_exception
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
        if(not (beam == None) and self.beamEnabled==True):
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
        self.rcpController.openPerspective("uk.ac.gda.microfocus.ui.MicroFocusPerspective")
