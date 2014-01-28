import time
from jarray import array

from map import Map
from microfocus_elements import showElementsList

from uk.ac.gda.client.microfocus.scan.datawriter import MicroFocusWriterExtender
from uk.ac.gda.beans import BeansFactory
from gda.configuration.properties import LocalProperties
from gda.factory import Finder
from gda.data.scan.datawriter import NexusDataWriter, XasAsciiNexusDataWriter
from gda.device import Detector
from gda.device.detector.xspress import XspressDetector, ResGrades
from gda.device.scannable import ScannableUtils
from gda.exafs.scan import BeanGroup
from gda.jython.commands import ScannableCommands
from gda.scan import ContinuousScan, ScanBase

from java.io import File
from java.lang import String


class RasterMap(Map):
    def __init__(self, xspressConfig, vortexConfig, d7a, d7b, counterTimer01, traj1ContiniousX, traj3ContiniousX, raster_counterTimer01, raster_xmap, traj1PositionReader, traj3PositionReader, raster_xspress, rcpController,outputPreparer):
        self.xspressConfig = xspressConfig
        self.vortexConfig = vortexConfig
        self.d7a=d7a
        self.d7b=d7b
        self.counterTimer01=counterTimer01
        self.finder = Finder.getInstance()
        
        self.traj1ContiniousX=traj1ContiniousX
        self.traj3ContiniousX=traj3ContiniousX
        self.trajContiniousX=traj1ContiniousX
        
        self.raster_counterTimer01=raster_counterTimer01
        self.raster_xmap=raster_xmap
        
        self.traj1PositionReader = traj1PositionReader
        self.traj3PositionReader = traj3PositionReader
        self.trajPositionReader = traj1PositionReader
        
        self.raster_xspress=raster_xspress
        self.mfd = None
#         self.detectorBeanFileName=""
        self.rcpController = rcpController
        
        self.beamEnabled = True

        self.outputPreparer = outputPreparer
        
    def enableBeam(self):
        self.beamEnabled = True
    
    def disableBeam(self):
        self.beamEnabled = False
    
    def setStage(self, stage):
        if stage==1:
            self.trajContiniousX = self.traj1ContiniousX
            self.trajPositionReader = self.traj1PositionReader
            self.raster_counterTimer01.setTtlSocket(1)
        elif stage==3:
            self.trajContiniousX = self.traj3ContiniousX
            self.trajPositionReader = self.traj3PositionReader
            self.raster_counterTimer01.setTtlSocket(2)
            
        else:
            print "please enter 1 or 3 as a parameter where 1 is the small stage and 3 is the large stage"
    
    def getMFD(self):
        return self.mfd
    
    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):

        ScanBase.interrupted = False

        origScanPlotSettings = LocalProperties.check("gda.scan.useScanPlotSettings")
        
        experimentFullPath, experimentFolderName = self.determineExperimentPath(folderName)
        
        if(sampleFileName == None or sampleFileName == 'None'):
            sampleBean = None
        else:
            sampleBean   = BeansFactory.getBeanObject(experimentFullPath, sampleFileName)
            
        scanBean     = BeansFactory.getBeanObject(experimentFullPath, scanFileName)
        detectorBean = BeansFactory.getBeanObject(experimentFullPath, detectorFileName)
      
        outputBean   = BeansFactory.getBeanObject(experimentFullPath, outputFileName)
    
        beanGroup = BeanGroup()
        beanGroup.setController(self.finder.find("ExafsScriptObserver"))
        beanGroup.setXmlFolder(experimentFullPath)
        beanGroup.setScannable(self.finder.find(scanBean.getXScannableName())) #TODO
        beanGroup.setExperimentFolderName(folderName)
        beanGroup.setScanNumber(scanNumber)
        if(sampleBean != None):
            beanGroup.setSample(sampleBean)
        beanGroup.setDetector(detectorBean)
        beanGroup.setOutput(outputBean)
        beanGroup.setValidate(validation)
        beanGroup.setScan(scanBean)
        
        detectorList = self.getDetectors(detectorBean, scanBean) 
    
        self.setupForRaster(beanGroup)
    
        nx = ScannableUtils.getNumberSteps(self.finder.find(scanBean.getXScannableName()),scanBean.getXStart(), scanBean.getXEnd(),scanBean.getXStepSize()) + 1
        ny = ScannableUtils.getNumberSteps(self.finder.find(scanBean.getYScannableName()),scanBean.getYStart(), scanBean.getYEnd(),scanBean.getYStepSize()) + 1
        self.log("Number x points: " + str(nx))
        self.log("Number y points: " + str(ny))
        energyList = [scanBean.getEnergy()]
        zScannablePos = scanBean.getZValue()
        self.detectorBeanFileName =experimentFullPath+detectorBean.getFluorescenceParameters().getConfigFileName()
        self.mfd = MicroFocusWriterExtender(nx, ny, scanBean.getXStepSize(), scanBean.getYStepSize(),self.detectorBeanFileName, array(detectorList, Detector))
        for energy in energyList:
            self.mfd.setEnergyValue(energy)
            zScannable = self.finder.find(scanBean.getZScannableName())
            if(zScannablePos == None):
                self.mfd.setZValue(zScannable.getPosition())
            else:
                self.mfd.setZValue(zScannablePos)

            yScannable = self.finder.find(scanBean.getYScannableName())
            
            energyScannable = self.finder.find(scanBean.getEnergyScannableName())
            self.log("Energy: " + str(energy))
            energyScannable.moveTo(energy) 
            zScannable.moveTo(zScannablePos)
            scanStart = time.asctime()
            
            self.redefineNexusMetadataForMaps(beanGroup)
            
            try:
                numberPoints = abs(scanBean.getXEnd()- scanBean.getXStart())/scanBean.getXStepSize() + 1.0
                
                detectorType = detectorBean.getFluorescenceParameters().getDetectorType()
 
                collectionTime = float(scanBean.getRowTime())/float(numberPoints)
                
                self.counterTimer01.setCollectionTime(collectionTime)
                
                self.log("Row time: " + str(float(scanBean.getRowTime())))
                self.log("Number points: "+ str(float(numberPoints)))

                #TODO the if/else below looks remarkably like something that can be turned into a method that takes a single parameter.
                if(detectorType == "Silicon"):
                    cs = ContinuousScan(self.trajContiniousX, scanBean.getXStart(), scanBean.getXEnd(), nx, scanBean.getRowTime(), [self.raster_counterTimer01, self.raster_xmap]) 
                    # NB: cannot use trajPositionReader in ContinuousScan and is not a priority for I18. Seems unclear how this would be used as the RealPositionReader interface is not used outside of Epics
#                    xmapRasterscan = ScannableCommands.createConcurrentScan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),cs,self.trajPositionReader])
                    xmapRasterscan = ScannableCommands.createConcurrentScan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),cs])
                    xmapRasterscan.getScanPlotSettings().setIgnore(1)

                    sampleName = sampleBean.getName()
                    descriptions = sampleBean.getDescriptions()
                    xmapRasterscan = self._setUpDataWriter(xmapRasterscan,scanBean,detectorBean,sampleBean,outputBean,sampleName,descriptions,scanNumber,experimentFolderName,experimentFullPath)

                    self.finder.find("elementListScriptController").update(None, self.detectorBeanFileName);
                    xmapRasterscan.runScan()
                else:
                    cs = ContinuousScan(self.trajContiniousX, scanBean.getXStart(), scanBean.getXEnd(), nx, scanBean.getRowTime(), [self.raster_counterTimer01, self.raster_xspress])
                    # NB: cannot use trajPositionReader in ContinuousScan and is not a priority for I18. Seems unclear how this would be used as the RealPositionReader interface is not used outside of Epics
#                    xspressRasterscan = ScannableCommands.createConcurrentScan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),cs,self.trajPositionReader])
                    xspressRasterscan = ScannableCommands.createConcurrentScan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),cs])
                    xspressRasterscan.getScanPlotSettings().setIgnore(1)

                    sampleName = sampleBean.getName()
                    descriptions = sampleBean.getDescriptions()
                    xspressRasterscan = self._setUpDataWriter(xspressRasterscan,scanBean,detectorBean,sampleBean,outputBean,sampleName,descriptions,scanNumber,experimentFolderName,experimentFullPath)

                    self.finder.find("elementListScriptController").update(None, self.detectorBeanFileName);
                    xspressRasterscan.runScan()
    
            finally:
                scanEnd = time.asctime()
                if(origScanPlotSettings):
                    LocalProperties.set("gda.scan.useScanPlotSettings", "true")
                else:
                    LocalProperties.set("gda.scan.useScanPlotSettings", "false")
                self.log("Map start time " + str(scanStart))
                self.log("Map end time " + str(scanEnd))
                self.finish()
                
                
    def _setUpDataWriter(self,thisscan,scanBean,detectorBean,sampleBean,outputBean,sampleName,descriptions,repetition,experimentFolderName,experimentFullPath):
        nexusSubFolder = experimentFolderName +"/" + outputBean.getNexusDirectory()
        asciiSubFolder = experimentFolderName +"/" + outputBean.getAsciiDirectory()
        
        nexusFileNameTemplate = nexusSubFolder +"/"+ sampleName+"_%d_"+str(repetition)+".nxs"
        asciiFileNameTemplate = asciiSubFolder +"/"+ sampleName+"_%d_"+str(repetition)+".dat"
        if LocalProperties.check(NexusDataWriter.GDA_NEXUS_BEAMLINE_PREFIX):
            nexusFileNameTemplate = nexusSubFolder +"/%d_"+ sampleName+"_"+str(repetition)+".nxs"
            asciiFileNameTemplate = asciiSubFolder +"/%d_"+ sampleName+"_"+str(repetition)+".dat"

        # create XasAsciiNexusDataWriter object and give it the parameters
        dataWriter = XasAsciiNexusDataWriter()
        dataWriter.setRunFromExperimentDefinition(True);
        dataWriter.setScanBean(scanBean);
        dataWriter.setDetectorBean(detectorBean);
        dataWriter.setSampleBean(sampleBean);
        dataWriter.setOutputBean(outputBean);
        dataWriter.setSampleName(sampleName);
        dataWriter.setXmlFolderName(experimentFullPath)
        dataWriter.setXmlFileName(self._determineDetectorFilename(detectorBean))
        dataWriter.setDescriptions(descriptions);
        dataWriter.setNexusFileNameTemplate(nexusFileNameTemplate);
        dataWriter.setAsciiFileNameTemplate(asciiFileNameTemplate);
        # get the ascii file format configuration (if not set here then will get it from the Finder inside the Java class
        asciidatawriterconfig = self.outputPreparer.getAsciiDataWriterConfig(scanBean)
        if asciidatawriterconfig != None :
            dataWriter.setConfiguration(asciidatawriterconfig)
            
        dataWriter.addDataWriterExtender(self.mfd)
        
        thisscan.setDataWriter(dataWriter)
        return thisscan

    def setupForRaster(self, beanGroup):
        rasterscan = beanGroup.getScan()
        collectionTime = rasterscan.getRowTime()
        command_server = self.finder.find("command_server")
        
        self.log("Starting raster scan...")
        
        if (LocalProperties.get("gda.mode") == 'live'):
            topupMonitor1 = command_server.getFromJythonNamespace("topupMonitor", None)    
            beam = command_server.getFromJythonNamespace("beam", None)
            detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
            trajBeamMonitor = command_server.getFromJythonNamespace("trajBeamMonitor", None)
            if not (trajBeamMonitor == None):
                trajBeamMonitor.setActive(True)
            if(not (topupMonitor1 == None)):
                topupMonitor1.setPauseBeforePoint(False)
                topupMonitor1.setPauseBeforeLine(True)
                topupMonitor1.setCollectionTime(collectionTime)
            if(not (beam == None) and self.beamEnabled==True):
                beam.setPauseBeforePoint(False)
                beam.setPauseBeforeLine(True)
            if(beanGroup.getDetector().getExperimentType() == "Fluorescence" and beanGroup.getDetector().getFluorescenceParameters().getDetectorType() == "Germanium"and not (detectorFillingMonitor == None)):
                detectorFillingMonitor.setPauseBeforePoint(False)
                detectorFillingMonitor.setPauseBeforeLine(True)
                detectorFillingMonitor.setCollectionTime(collectionTime)
           
                fullFileName = beanGroup.getXmlFolder() + beanGroup.getDetector().getFluorescenceParameters().getConfigFileName()
                bean = BeansFactory.getBean(File(fullFileName));
                bean.setReadoutMode(XspressDetector.READOUT_MCA);
                bean.setResGrade(ResGrades.NONE);
                elements = bean.getDetectorList();
                for element in elements: 
                    rois = element.getRegionList();
                    element.setWindow(rois.get(0).getRoiStart(), rois.get(0).getRoiEnd())
                BeansFactory.saveBean(File(fullFileName), bean)
        outputBean=beanGroup.getOutput()
        sampleParameters = beanGroup.getSample()
        outputBean.setAsciiFileName(sampleParameters.getName())
        att1 = sampleParameters.getAttenuatorParameter1()
        att2 = sampleParameters.getAttenuatorParameter2()
        self.d7a(att1.getSelectedPosition())
        self.d7b(att2.getSelectedPosition())
#         if(beanGroup.getDetector().getExperimentType() == "Fluorescence"):
#             configFluoDetector(beanGroup)
        # TODO should this not simply use the detector preparer? This code comes from there.
        if beanGroup.getDetector().getExperimentType() == "Fluorescence":
            fluoresenceParameters = beanGroup.getDetector().getFluorescenceParameters()
            detType = fluoresenceParameters.getDetectorType()
            xmlFileName = beanGroup.getXmlFolder() + fluoresenceParameters.getConfigFileName()
            if detType == "Germanium":
                self.xspressConfig.initialize()
                xspressBean = self.xspressConfig.createBeanFromXML(xmlFileName)
                onlyShowFF = xspressBean.isOnlyShowFF()
                showDTRawValues = xspressBean.isShowDTRawValues()
                saveRawSpectrum = xspressBean.isSaveRawSpectrum()
                self.xspressConfig.configure(xmlFileName, onlyShowFF, showDTRawValues, saveRawSpectrum)

        LocalProperties.set("gda.scan.useScanPlotSettings", "true")
       
        self.rcpController.openPerspective("uk.ac.gda.microfocus.ui.MicroFocusPerspective")
