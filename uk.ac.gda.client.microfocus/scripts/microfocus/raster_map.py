import time
from jarray import array

from exafsscripts.exafs.config_fluoresence_detectors import XspressConfig, VortexConfig
from gdascripts.messages import handle_messages
from map import Map
from microfocus_elements import showElementsList

from uk.ac.gda.client.microfocus.scan.datawriter import MicroFocusWriterExtender
from uk.ac.gda.beans import BeansFactory
from gda.configuration.properties import LocalProperties
from gda.factory import Finder
from gda.data.scan.datawriter import XasAsciiDataWriter
from gda.data.scan.datawriter import XasAsciiNexusDatapointCompletingDataWriter
from gda.data.scan.datawriter import NexusExtraMetadataDataWriter, XasAsciiDataWriter, NexusDataWriter, XasAsciiNexusDataWriter
from gda.device import Detector
from gda.device.detector.xspress import XspressDetector, ResGrades
from gda.device.scannable import ScannableUtils
from gda.exafs.scan import BeanGroup
from gda.jython.commands import ScannableCommands
from gda.scan import ContinuousScan

from java.io import File
from java.lang import String


class RasterMap(Map):
    # TODO why are counterTimer01 and raster_counterTimer01 both used
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
        self.detectorBeanFileName=""
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

        origScanPlotSettings = LocalProperties.check("gda.scan.useScanPlotSettings")
     
        #xmlFolderName = folderName + "/"
        #folderName = folderName[folderName.find("xml")+4:]
        
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
       # XasAsciiDataWriter.setBeanGroup(beanGroup)
        
        detectorList = self.getDetectors(detectorBean, scanBean) 
    
        self.setupForRaster(beanGroup)
    
        nx = ScannableUtils.getNumberSteps(self.finder.find(scanBean.getXScannableName()),scanBean.getXStart(), scanBean.getXEnd(),scanBean.getXStepSize()) + 1
        ny = ScannableUtils.getNumberSteps(self.finder.find(scanBean.getYScannableName()),scanBean.getYStart(), scanBean.getYEnd(),scanBean.getYStepSize()) + 1
        print "number of x points is ", str(nx)
        print "number of y points is ", str(ny)
        energyList = [scanBean.getEnergy()]
        zScannablePos = scanBean.getZValue()
        self.mfd = MicroFocusWriterExtender(nx, ny, scanBean.getXStepSize(), scanBean.getYStepSize())
        self.mfd.setPlotName("MapPlot")
        for energy in energyList:
            print " the detector is " 
            print detectorList
            if(detectorBean.getExperimentType() == "Transmission"):
                self.mfd.setSelectedElement("I0")
                self.mfd.setDetectors(array(detectorList, Detector))
            else:   
                detectorType = detectorBean.getFluorescenceParameters().getDetectorType()
                if(folderName != None):
                    self.detectorBeanFileName =experimentFullPath+detectorBean.getFluorescenceParameters().getConfigFileName()
                else:
                    self.detectorBeanFileName =experimentFullPath+detectorBean.getFluorescenceParameters().getConfigFileName()
                print self.detectorBeanFileName
                elements = showElementsList(self.detectorBeanFileName)
                selectedElement = elements[0]
                self.mfd.setRoiNames(array(elements, String))
                self.mfd.setDetectorBeanFileName(self.detectorBeanFileName)
                bean = BeansFactory.getBean(File(self.detectorBeanFileName))   
                detector = self.finder.find(bean.getDetectorName())   
                detectorList=[]
                detectorList.append(self.finder.find("counterTimer01"))
                detectorList.append(detector)  
                self.mfd.setDetectors(array(detectorList, Detector))     
                self.mfd.setSelectedElement(selectedElement)
                self.mfd.getWindowsfromBean()
            self.mfd.setEnergyValue(energy)
            self.mfd.setZValue(zScannablePos)
            yScannable = self.finder.find(scanBean.getYScannableName())
            
            energyScannable = self.finder.find(scanBean.getEnergyScannableName())
            zScannable = self.finder.find(scanBean.getZScannableName())
            print "energy is ", str(energy)
            print detectorList
            energyScannable.moveTo(energy) 
            zScannable.moveTo(zScannablePos)
            scanStart = time.asctime()
            
            self.redefineNexusMetadataForMaps(beanGroup)
            
            try:
                numberPoints = abs(scanBean.getXEnd()- scanBean.getXStart())/scanBean.getXStepSize() + 1.0
                
                detectorType = detectorBean.getFluorescenceParameters().getDetectorType()
 
                collectionTime = float(scanBean.getRowTime())/float(numberPoints)
                
                self.counterTimer01.setCollectionTime(collectionTime)
                print "row time = ", float(scanBean.getRowTime())
                print "no. points = ", float(numberPoints)

                #TODO the if/else below looks remarkably like something that can be turned into a method that takes a single parameter.
                if(detectorType == "Silicon"):
                    print "Vortex raster scan"
                    cs = ContinuousScan(self.trajContiniousX, scanBean.getXStart(), scanBean.getXEnd(), nx, scanBean.getRowTime(), [self.raster_counterTimer01, self.raster_xmap]) 
                    xmapRasterscan = ScannableCommands.createConcurrentScan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),cs,self.trajPositionReader])
                    xmapRasterscan.getScanPlotSettings().setIgnore(1)
                    
                    #xasWriter = XasAsciiNexusDatapointCompletingDataWriter()
                    #xasWriter.addDataWriterExtender(self.mfd)
                    #xmapRasterscan.setDataWriter(xasWriter)
                    sampleName = sampleBean.getName()
                    descriptions = sampleBean.getDescriptions()
                    xmapRasterscan = self._setUpDataWriter(xmapRasterscan,scanBean,detectorBean,sampleBean,outputBean,sampleName,descriptions,scanNumber,experimentFolderName,experimentFullPath)

                    self.finder.find("elementListScriptController").update(None, self.detectorBeanFileName);
                    xmapRasterscan.runScan()
                else:
                    print "Xspress Raster Scan"
                    cs = ContinuousScan(self.trajContiniousX, scanBean.getXStart(), scanBean.getXEnd(), nx, scanBean.getRowTime(), [self.raster_counterTimer01, self.raster_xspress])
                    xspressRasterscan = ScannableCommands.createConcurrentScan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),cs,self.trajPositionReader])
                    xspressRasterscan.getScanPlotSettings().setIgnore(1)
                    
                    #xasWriter = XasAsciiNexusDatapointCompletingDataWriter()
                    #xasWriter.addDataWriterExtender(self.mfd)
                    #xspressRasterscan.setDataWriter(xasWriter)
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
                handle_messages.simpleLog("map start time " + str(scanStart))
                handle_messages.simpleLog("map end time " + str(scanEnd))
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
        #dataWriter.setXmlFileName(self._determineDetectorFilename(detectorBean))
        dataWriter.setDescriptions(descriptions);
        dataWriter.setNexusFileNameTemplate(nexusFileNameTemplate);
        dataWriter.setAsciiFileNameTemplate(asciiFileNameTemplate);
        # get the ascii file format configuration (if not set here then will get it from the Finder inside the Java class
        asciidatawriterconfig = self.outputPreparer.getAsciiDataWriterConfig(scanBean)
        if asciidatawriterconfig != None :
            dataWriter.setConfiguration(asciidatawriterconfig)
            
        print "adding mfd to raster map"
        dataWriter.addDataWriterExtender(self.mfd)
        
        thisscan.setDataWriter(dataWriter)
        return thisscan

    def setupForRaster(self, beanGroup):
        rasterscan = beanGroup.getScan()
        collectionTime = rasterscan.getRowTime()
        command_server = self.finder.find("command_server")    
        
        if (LocalProperties.get("gda.mode") == 'live'):
            topupMonitor1 = command_server.getFromJythonNamespace("topupMonitor", None)    
            beam = command_server.getFromJythonNamespace("beam", None)
            detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
            trajBeamMonitor = command_server.getFromJythonNamespace("trajBeamMonitor", None)
            if not (trajBeamMonitor == None):
                trajBeamMonitor.setActive(True)
            print "setting collection time to" , str(collectionTime)        
            if(not (topupMonitor1 == None)):
                topupMonitor1.setPauseBeforePoint(False)
                topupMonitor1.setPauseBeforeLine(True)
                topupMonitor1.setCollectionTime(collectionTime)
            if(not (beam == None) and self.beamEnabled==True):
                #self.finder.find("command_server").addDefault(beam);
                beam.setPauseBeforePoint(False)
                beam.setPauseBeforeLine(True)
            if(beanGroup.getDetector().getExperimentType() == "Fluorescence" and beanGroup.getDetector().getFluorescenceParameters().getDetectorType() == "Germanium"and not (detectorFillingMonitor == None)):
                #self.finder.find("command_server").addDefault(detectorFillingMonitor);
                detectorFillingMonitor.setPauseBeforePoint(False)
                detectorFillingMonitor.setPauseBeforeLine(True)
                detectorFillingMonitor.setCollectionTime(collectionTime)
           
                fullFileName = beanGroup.getXmlFolder() + beanGroup.getDetector().getFluorescenceParameters().getConfigFileName()
                print fullFileName
                bean = BeansFactory.getBean(File(fullFileName));
                print dir(bean)
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
        print "Setting the ascii file name as " ,sampleParameters.getName()    
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
