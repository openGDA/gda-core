from jarray import array
import time

from exafsscripts.exafs.scan import Scan
from gdascripts.metadata.metadata_commands import meta_add
from gdascripts.parameters.beamline_parameters import JythonNameSpaceMapping

from gda.configuration.properties import LocalProperties
from gda.data.scan.datawriter import NexusDataWriter, XasAsciiNexusDataWriter
from gda.data.scan.datawriter import XasAsciiNexusDatapointCompletingDataWriter
from gda.device import Detector
from gda.device.scannable import ScannableUtils
from gda.exafs.scan import BeanGroup
from gda.factory import Finder
from gda.jython.commands import ScannableCommands
from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.client.microfocus.scan.datawriter import MicroFocusWriterExtender


class Map(Scan):

    #runPreparer methods that called the prepare method in output, sample and detector preparers. Here only detectorPreparer.prepare is called 
    #outputpreparer.prepare should be called to be able to add metadata in both ASCII and Nexus files. Maybe runPreparers method should move 
    #from XasScan to Scan class? 
    def __init__(self, xspressConfig, vortexConfig, d7a, d7b, kb_vfm_x, counterTimer01, rcpController, ExafsScriptObserver,outputPreparer,detectorPreparer, xScan, yScan):
        self.xspressConfig = xspressConfig
        self.vortexConfig = vortexConfig
        self.d7a = d7a
        self.d7b = d7b
        self.kb_vfm_x = kb_vfm_x
        self.counterTimer01=counterTimer01
        self.rcpController = rcpController
        self.ExafsScriptObserver=ExafsScriptObserver
        self.outputPreparer = outputPreparer
        self.detectorPreparer = detectorPreparer
        self.xScan = xScan
        self.yScan = yScan

        self.finder = Finder.getInstance()
        self.mfd = None
        self.detectorBeanFileName = ""
        self.sampleFilename= None
        self.scanFilename= None
        self.detectorFilename= None
        self.outputFilename= None
        
    def setEnergyScannables(self,energyWithGap, energyNoGap):
        self.energyWithGap = energyWithGap
        self.energyNoGap = energyNoGap
        self.energyScannable = self.energyWithGap
        
    def setUseNoGapEnergy(self):
        self.energyScannable = self.energyNoGap

    def setUseWithGapEnergy(self):
        self.energyScannable = self.energyWithGap
        
    def setStageScannables(self,stage1_x,stage1_y,stage1_z,stage3_x,stage3_y,stage3_z):
        self.stage1_x = stage1_x
        self.stage1_y = stage1_y
        self.stage1_z = stage1_z
        self.stage3_x = stage3_x
        self.stage3_y = stage3_y
        self.stage3_z = stage3_z
        
        self.stage_x = stage1_x
        self.stage_y = stage1_y
        self.stage_z = stage1_z
        
    def setStage(self, stage):
        if stage==1:
            self.stage_x = self.stage1_x
            self.stage_y = self.stage1_y
            self.stage_z = self.stage1_z
        elif stage==3:
            self.stage_x = self.stage3_x
            self.stage_y = self.stage3_y
            self.stage_z = self.stage3_z
        else:
            print "please enter 1 or 3 as a parameter where 1 is the small stage and 3 is the large stage"
    
    def getMFD(self):
        return self.mfd
    
    def setCmos(self,cmos):
        self.cmos = cmos

    
    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):
    
        print ""
        print "*********************"
        self.log("Preparing for map...")
        
        origScanPlotSettings = LocalProperties.check("gda.scan.useScanPlotSettings")
        
        experimentFullPath, experimentFolderName = self.determineExperimentPath(folderName)
        self.setXmlFileNames(sampleFileName, scanFileName, detectorFileName, outputFileName)
    
        if(sampleFileName == None or sampleFileName == 'None'):
            sampleBean = None
        else:
            sampleBean  = BeansFactory.getBeanObject(experimentFullPath, sampleFileName)
            
        scanBean = BeansFactory.getBeanObject(experimentFullPath, scanFileName)
        detectorBean = BeansFactory.getBeanObject(experimentFullPath, detectorFileName)
        outputBean   = BeansFactory.getBeanObject(experimentFullPath, outputFileName)
        self._runScript(outputBean.getBeforeScriptName())
        
        # sanity check
        if detectorBean.getFluorescenceParameters().getConfigFileName() == None or detectorBean.getFluorescenceParameters().getConfigFileName() == "":
            raise Exception(" No Fluoresence parameters file supplied: have you selected the Fluoresence option in Detector Parameters?")
    
        beanGroup = BeanGroup()
        beanGroup.setController(self.ExafsScriptObserver)
        beanGroup.setXmlFolder(experimentFullPath)
        beanGroup.setScannable(self.stage_x)
        beanGroup.setExperimentFolderName(experimentFolderName)
        beanGroup.setScanNumber(scanNumber)
        if(sampleBean != None):
            beanGroup.setSample(sampleBean)
        beanGroup.setDetector(detectorBean)
        beanGroup.setOutput(outputBean)
        beanGroup.setValidate(validation)
        beanGroup.setScan(scanBean)

        detectorList = self.getDetectors(detectorBean, scanBean)
        self.log("Detectors: " + str(detectorList))
        
        # *************** DIFFERENT
        self._setupForMap(beanGroup)
        
        nx = ScannableUtils.getNumberSteps(self.stage_x,scanBean.getXStart(), scanBean.getXEnd(),scanBean.getXStepSize()) + 1
        ny = ScannableUtils.getNumberSteps(self.stage_y,scanBean.getYStart(), scanBean.getYEnd(),scanBean.getYStepSize()) + 1
        self.log("Number x points: " + str(nx))
        self.log("Number y points: " + str(ny))
        
        energyList = [scanBean.getEnergy()]
        zScannablePos = scanBean.getZValue()
        
        self.detectorPreparer.prepare(scanBean, detectorBean, outputBean, experimentFullPath)
        
        self.detectorBeanFileName = experimentFullPath+detectorBean.getFluorescenceParameters().getConfigFileName()
        self._createMFD(nx, ny, scanBean.getXStepSize(), scanBean.getYStepSize(), detectorList)
#         self.mfd = MicroFocusWriterExtender(nx, ny, scanBean.getXStepSize(), scanBean.getYStepSize(),self.detectorBeanFileName, array(detectorList, Detector))

        for energy in energyList:
            self.log("Energy: " + str(energy))
            self.energyScannable.moveTo(energy) 
            self.mfd.setEnergyValue(energy)
            
            self.mfd.setZValue(zScannablePos)
            if(zScannablePos != None):
                self.log("Moving " + self.stage_z.getName() + " to " + str(zScannablePos))
                self.stage_z.moveTo(zScannablePos)
            
            scanStart = time.asctime()
            
        #    self.redefineNexusMetadataForMaps(beanGroup)
            
            try:
                self._runMap(beanGroup, self.stage_x, self.stage_y, self.stage_z, detectorList,scanNumber,experimentFolderName,experimentFullPath,nx,ny)
            finally:
                scanEnd = time.asctime()
                if(origScanPlotSettings):
                    LocalProperties.set("gda.scan.useScanPlotSettings", "true")
                else:
                    LocalProperties.set("gda.scan.useScanPlotSettings", "false")
                self.log("Map start time " + str(scanStart))
                self.log("Map end time " + str(scanEnd))
                self.finish()
                self._runScript(beanGroup.getOutput().getAfterScriptName())# run the after scan script
                
    def _createMFD(self, nx, ny, xStepSize, yStepSize, detectorList):
        self.mfd = MicroFocusWriterExtender(nx, ny, xStepSize, yStepSize,self.detectorBeanFileName, array(detectorList, Detector))
    
    def _runMap(self,beanGroup, xScannable, yScannable, zScannable, detectorList,scanNumber,experimentFolderName,experimentFullPath,nx,ny):
        
        detectorBean = beanGroup.getDetector()
        scanBean = beanGroup.getScan()
        sampleBean = beanGroup.getSample()
        outputBean=beanGroup.getOutput()
        
        scanBean.setCollectionTime(scanBean.getCollectionTime())
        args=[yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),  xScannable, scanBean.getXStart(), scanBean.getXEnd(),  scanBean.getXStepSize(), zScannable]
        
        self.counterTimer01.setCollectionTime(scanBean.getCollectionTime())
        
        # what does this do? why is it not in raster map? Adding this to raster map does not set live time.
        useFrames = LocalProperties.check("gda.microfocus.scans.useFrames")
        self.log("Using frames: " + str(useFrames))
        if(detectorBean.getExperimentType() == "Fluorescence" and useFrames):
            args+= detectorList
            self.counterTimer01.clearFrameSets()
            self.log("Frame collection time: " + str(scanBean.getCollectionTime()))
            self.counterTimer01.addFrameSet(int(nx),1.0E-4,scanBean.getCollectionTime()*1000.0,0,7,-1,0)
        else:
            for detector in detectorList:
                args.append(detector)              
                args.append(scanBean.getCollectionTime())            
        mapscan= ScannableCommands.createConcurrentScan(args)
        sampleName = sampleBean.getName()
        descriptions = sampleBean.getDescriptions()
        mapscan = self._setUpDataWriter(mapscan,scanBean,detectorBean,sampleBean,outputBean,sampleName,descriptions,scanNumber,experimentFolderName,experimentFullPath)
        mapscan.getScanPlotSettings().setIgnore(1)
        self.finder.find("elementListScriptController").update(None, self.detectorBeanFileName);
        self.log("Starting step map...")
        mapscan.runScan()
    
    # should merge with method in xas_scan but keeping here while developing to see what differences required
    # Here _setUpData method is a repetition of the same method in the class Scan except for the name of the nexus and ascii files
    
    def _setUpDataWriter(self,thisscan,scanBean,detectorBean,sampleBean,outputBean,sampleName,descriptions,repetition,experimentFolderName,experimentFullPath):
        nexusSubFolder = experimentFolderName +"/" + outputBean.getNexusDirectory()
        asciiSubFolder = experimentFolderName +"/" + outputBean.getAsciiDirectory()
        
        nexusFileNameTemplate = nexusSubFolder +"/"+ sampleName+"_%d_"+str(repetition)+".nxs"
        asciiFileNameTemplate = asciiSubFolder +"/"+ sampleName+"_%d_"+str(repetition)+".dat"
        if LocalProperties.check(NexusDataWriter.GDA_NEXUS_BEAMLINE_PREFIX):
            nexusFileNameTemplate = nexusSubFolder +"/%d_"+ sampleName+"_"+str(repetition)+".nxs"
            asciiFileNameTemplate = asciiSubFolder +"/%d_"+ sampleName+"_"+str(repetition)+".dat"

        # Create DataWriter object and give it the parameters.
        # Use XasAsciiNexusDatapointCompletingDataWriter as we will use a PositionCallable for raster 
        # scans to return the real motor positions which are not available until the end of each row.
        twoDWriter = XasAsciiNexusDatapointCompletingDataWriter()
        twoDWriter.addDataWriterExtender(self.mfd)
        dataWriter = twoDWriter.getXasDataWriter()
        
        
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
            self.logger.info("Metashop not found")
            
               
        dataWriter.setFolderName(experimentFullPath)
        dataWriter.setScanParametersName(self.scanFileName)
        dataWriter.setDetectorParametersName(self.detectorFileName)
        dataWriter.setSampleParametersName(self.sampleFileName)
        dataWriter.setOutputParametersName(self.outputFileName)
        dataWriter.setRunFromExperimentDefinition(True);
        dataWriter.setDescriptions(descriptions);
        dataWriter.setNexusFileNameTemplate(nexusFileNameTemplate);
        dataWriter.setAsciiFileNameTemplate(asciiFileNameTemplate);
       
        # get the ascii file format configuration (if not set here then will get it from the Finder inside the Java class
        asciidatawriterconfig = self.outputPreparer.getAsciiDataWriterConfig(scanBean)
        if asciidatawriterconfig != None :
            dataWriter.setConfiguration(asciidatawriterconfig)
        thisscan.setDataWriter(twoDWriter)
        return thisscan

    def _setupFromSampleParameters(self, beanGroup):
        outputBean=beanGroup.getOutput()
        sampleParameters = beanGroup.getSample()
        outputBean.setAsciiFileName(sampleParameters.getName())
        self.log("Ascii file prefix: " ,sampleParameters.getName())
        att1 = sampleParameters.getAttenuatorParameter1()
        att2 = sampleParameters.getAttenuatorParameter2()
        self.log("Moving: " + self.d7a.getName() + " to " + att1.getSelectedPosition())
        self.log("Moving: " + self.d7b.getName() + " to " + att2.getSelectedPosition())
        self.d7a(att1.getSelectedPosition())
        self.d7b(att2.getSelectedPosition())
        
        if sampleParameters.isVfmxActive():
            self.log( "Moving kb_vfm_x to:" + str(sampleParameters.getVfmx()))
            self.kb_vfm_x(sampleParameters.getVfmx())
        
        LocalProperties.set("gda.scan.useScanPlotSettings", "true")

    def _setupForMap(self, beanGroup):

        if (LocalProperties.get("gda.mode") == 'live'):
            collectionTime = beanGroup.getScan().getCollectionTime()
            command_server = self.finder.find("command_server")    
            topupMonitor = command_server.getFromJythonNamespace("topupMonitor", None)    
            beamMonitor = command_server.getFromJythonNamespace("beamMonitor", None)
            detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
            
            if topupMonitor != None:
                topupMonitor.setPauseBeforePoint(True)
                topupMonitor.setCollectionTime(collectionTime)
                topupMonitor.setPauseBeforeLine(False)
            
            if beamMonitor != None:
                beamMonitor.setPauseBeforePoint(True)
                beamMonitor.setPauseBeforeLine(True)
            
            if(beanGroup.getDetector().getExperimentType() == "Fluorescence" and beanGroup.getDetector().getFluorescenceParameters().getDetectorType() == "Germanium"):
                self.finder.find("command_server").addDefault(detectorFillingMonitor);
                detectorFillingMonitor.setPauseBeforePoint(True)
                detectorFillingMonitor.setPauseBeforeLine(False)

        self._setupFromSampleParameters(beanGroup)
        
        self.rcpController.openPerspective("uk.ac.gda.microfocus.ui.MicroFocusPerspective")      

    def getDetectors(self, detectorBean, scanBean):
        expt_type = detectorBean.getExperimentType()
        if expt_type == "Transmission":
            for group in detectorBean.getDetectorGroups():
                if group.getName() == detectorBean.getTransmissionParameters().getDetectorType():
                    return self._createDetArray(group.getDetector(), scanBean)
        else:
            for group in detectorBean.getDetectorGroups():
                if group.getName() == detectorBean.getFluorescenceParameters().getDetectorType():
                    detArray = self._createDetArray(group.getDetector(), scanBean)
 
                    if detectorBean.getFluorescenceParameters().isCollectDiffractionImages():
                        if self.cmos != None:
                            print "Using cmos"
                            detArray += [self.cmos]
                    
                    return detArray
        raise Exception("Detector list not found!")

 
    def finish(self):
        command_server = self.finder.find("command_server")
        beam = command_server.getFromJythonNamespace("beam", None)
        detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
        self.finder.find("command_server").removeDefault(beam);
        self.finder.find("command_server").removeDefault(detectorFillingMonitor);
        if self.mfd != None:
            self.mfd.closeWriter()
