from map import Map

from gdascripts.metadata.metadata_commands import meta_add

from gda.configuration.properties import LocalProperties
from gda.data.scan.datawriter import TwoDScanRowReverser, NexusDataWriter
from gda.data.scan.datawriter import XasAsciiNexusDatapointCompletingDataWriter
from gda.factory import Finder
from gda.jython.commands import ScannableCommands
from gda.scan import TrajectoryScanLine
from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.client.microfocus.util import ScanPositionsTwoWay

from microfocus.raster_map import RasterMap

#
# Faster raster
#
class RasterMapReturnWrite(RasterMap):
    
    def __init__(self, xspressConfig, vortexConfig, d7a, d7b, kb_vfm_x, counterTimer01, rcpController, ExafsScriptObserver,outputPreparer, detectorPreparer, traj_xspress3, traj1tfg, traj1xmap,traj3tfg, traj3xmap, traj1SampleX, traj3SampleX, traj1PositionReader, traj3PositionReader, trajBeamMonitor):
        self.xspressConfig = xspressConfig
        self.vortexConfig = vortexConfig
        self.d7a=d7a
        self.d7b=d7b
        self.kb_vfm_x = kb_vfm_x
        self.counterTimer01=counterTimer01
        self.rcpController = rcpController
        self.ExafsScriptObserver = ExafsScriptObserver
        self.outputPreparer = outputPreparer
        self.detectorPreparer = detectorPreparer

        self.traj1SampleX=traj1SampleX
        self.traj3SampleX=traj3SampleX
        self.trajSampleX=traj1SampleX
        
        self.traj_xspress3 = traj_xspress3
        self.traj1tfg=traj1tfg
        self.traj3tfg=traj3tfg
        self.trajtfg=traj1tfg
        self.traj1xmap=traj1xmap
        self.traj3xmap=traj3xmap
        self.trajxmap=traj1xmap
        
        self.traj1PositionReader = traj1PositionReader
        self.traj3PositionReader = traj3PositionReader
        self.trajPositionReader = traj1PositionReader

        self.trajBeamMonitor = trajBeamMonitor

        self.beamEnabled = True
        self.finder = Finder.getInstance()
        self.mfd = None
        self.detectorBeanFileName=""
        
    def setStage(self, stage):
        Map.setStage(self,stage)
        if stage==1:
            self.trajSampleX = self.traj1SampleX
            self.trajtfg=self.traj1tfg
            self.trajtfg.setTtlSocket(1)
            self.trajxmap=self.traj1xmap
            self.trajPositionReader = self.traj1PositionReader
        elif stage==3:
            self.trajSampleX = self.traj3SampleX
            self.trajtfg=self.traj3tfg
            self.trajtfg.setTtlSocket(1)
            self.trajxmap=self.traj3xmap
            self.trajPositionReader = self.traj3PositionReader
        else:
            print "please enter 1 or 3 as a parameter where 1 is the small stage and 3 is the large stage"

    def _runMap(self,beanGroup, xScannable, yScannable, zScannable, detectorList,scanNumber,experimentFolderName,experimentFullPath,nx,ny):
        scanBean = beanGroup.getScan()
        
        detectorBean = beanGroup.getDetector()
        detectorType = detectorBean.getFluorescenceParameters().getDetectorType()
        
        if detectorBean.getExperimentType() != "Fluorescence": # or detectorType != "Silicon":
            print "*** Faster maps may only be performed using the Xmap Vortex detector! ***"
            print "*** Change detector type in XML or mapping mode by typing map.disableFasterRaster()"
            return
        
        fluoDetector = self.trajxmap
        if detectorType == "Xspress3":
            fluoDetector = self.traj_xspress3
            fluoDetector.setHardwareTriggerProvider(self.trajtfg.getHardwareTriggerProvider())
        else:
            # may not have to do this: check.
            self.trajxmap.setScanNumberOfPoints(nx)
        # NB: no HardwareTriggered version of Xspress2 has been written
        
        point_collection_time = scanBean.getRowTime() / nx
        self.trajtfg.setIntegrateBetweenPoints(True)
#         fluoDetector.setIntegrateBetweenPoints(True)
        self.trajtfg.setCollectionTime(point_collection_time)
#         fluoDetector.setCollectionTime(point_collection_time)
#         fluoDetector.setScanNumberOfPoints(nx)
        sptw = ScanPositionsTwoWay(self.trajSampleX,scanBean.getXStart(), scanBean.getXEnd(), scanBean.getXStepSize())
        tsl  = TrajectoryScanLine([self.trajSampleX, sptw,  self.trajtfg, scanBean.getRowTime()/(nx)] )
#         tsl  = TrajectoryScanLine([self.trajSampleX, sptw,  self.trajtfg, fluoDetector, scanBean.getRowTime()/(nx)] )
        tsl.setScanDataPointQueueLength(10000)
        tsl.setPositionCallableThreadPoolSize(10)
        
        concurrentScanArgs = [yScannable, scanBean.getYStart(), scanBean.getYEnd(), scanBean.getYStepSize(), self.trajBeamMonitor, tsl]
        if self.includeRealPositionReader :
            concurrentScanArgs = concurrentScanArgs + [self.trajPositionReader]
        xmapRasterscan = ScannableCommands.createConcurrentScan(concurrentScanArgs)
        xmapRasterscan.getScanPlotSettings().setIgnore(1)
        
        self._setUpTwoDDataWriter(xmapRasterscan, nx, ny, beanGroup, experimentFullPath, experimentFolderName,scanNumber)
        self.finder.find("elementListScriptController").update(None, self.detectorBeanFileName);
        self.log("Starting two-directional raster map...")
        xmapRasterscan.runScan()

    def _setUpTwoDDataWriter(self,xmapRasterscan, nx, ny, beanGroup, experimentFullPath, experimentFolderName, scanNumber):

        
        detectorBean = beanGroup.getDetector()
        scanBean = beanGroup.getScan()
        sampleBean = beanGroup.getSample()
        outputBean=beanGroup.getOutput()
        
        sampleName = sampleBean.getName()
        descriptions = sampleBean.getDescriptions()
 
        rowR = TwoDScanRowReverser()
        rowR.setNoOfColumns(nx)
        rowR.setNoOfRows(ny)
        rowR.setReverseOdd(True)
        
        nexusSubFolder = experimentFolderName +"/" + outputBean.getNexusDirectory()
        asciiSubFolder = experimentFolderName +"/" + outputBean.getAsciiDirectory()
        
        nexusFileNameTemplate = nexusSubFolder +"/"+ sampleName+"_%d_"+str(scanNumber)+".nxs"
        asciiFileNameTemplate = asciiSubFolder +"/"+ sampleName+"_%d_"+str(scanNumber)+".dat"
        if LocalProperties.check(NexusDataWriter.GDA_NEXUS_BEAMLINE_PREFIX):
            nexusFileNameTemplate = nexusSubFolder +"/%d_"+ sampleName+"_"+str(scanNumber)+".nxs"
            asciiFileNameTemplate = asciiSubFolder +"/%d_"+ sampleName+"_"+str(scanNumber)+".dat"

        twoDWriter = XasAsciiNexusDatapointCompletingDataWriter()
        twoDWriter.setIndexer(rowR)
        twoDWriter.addDataWriterExtender(self.mfd)
        # XAS-162 try this:
#         if (Finder.getInstance().find("FileRegistrar") != None):
#             twoDWriter.addDataWriterExtender(Finder.getInstance().find("FileRegistrar"))
        
        xasWriter = twoDWriter.getXasDataWriter()
        
        xasWriter.setFolderName(experimentFullPath)
        xasWriter.setScanParametersName(self.scanFileName)
        xasWriter.setDetectorParametersName(self.detectorFileName)
        xasWriter.setSampleParametersName(self.sampleFileName)
        xasWriter.setOutputParametersName(self.outputFileName)
        xasWriter.setRunFromExperimentDefinition(True);
        xasWriter.setDescriptions(descriptions);
        xasWriter.setNexusFileNameTemplate(nexusFileNameTemplate);
        xasWriter.setAsciiFileNameTemplate(asciiFileNameTemplate);
        
        
        if (Finder.getInstance().find("metashop") != None):
            meta_add(self.detectorFileName, BeansFactory.getXMLString(detectorBean))
            meta_add(self.outputFileName, BeansFactory.getXMLString(outputBean))
            meta_add(self.sampleFileName, BeansFactory.getXMLString(sampleBean))
            meta_add(self.scanFileName, BeansFactory.getXMLString(scanBean))
            meta_add("xmlFolderName", experimentFullPath)
            xmlFilename = self._determineDetectorFilename(detectorBean)
            if ((xmlFilename != None) and (experimentFullPath != None)):
                detectorConfigurationBean = BeansFactory.getBeanObject(experimentFullPath, xmlFilename)
                meta_add("DetectorConfigurationParameters", BeansFactory.getXMLString(detectorConfigurationBean)) 
        else: 
            self.log("Metashop not found")
            
               
        xasWriter.setFolderName(experimentFullPath)
        xasWriter.setScanParametersName(self.scanFileName)
        xasWriter.setDetectorParametersName(self.detectorFileName)
        xasWriter.setSampleParametersName(self.sampleFileName)
        xasWriter.setOutputParametersName(self.outputFileName)
        
        # add the detector configuration file to the metadata
#         xasWriter.setXmlFileName(self._determineDetectorFilename(detectorBean))
        xasWriter.setDescriptions(descriptions);
        xasWriter.setNexusFileNameTemplate(nexusFileNameTemplate);
        xasWriter.setAsciiFileNameTemplate(asciiFileNameTemplate);
        
        
        xmapRasterscan.setDataWriter(twoDWriter)
        
        

