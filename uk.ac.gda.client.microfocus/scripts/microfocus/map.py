from jarray import array
import time

from exafsscripts.exafs.scan import Scan
from gdascripts.parameters.beamline_parameters import JythonNameSpaceMapping

from gda.configuration.properties import LocalProperties
from gda.data.scan.datawriter import NexusExtraMetadataDataWriter, NexusDataWriter, XasAsciiNexusDataWriter
from gda.device import Detector
from gda.device.scannable import ScannableUtils
from gda.exafs.scan import BeanGroup
from gda.factory import Finder
from gda.jython.commands import ScannableCommands
from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.client.microfocus.scan.datawriter import MicroFocusWriterExtender


class Map(Scan):

    def __init__(self, xspressConfig, vortexConfig, d7a, d7b, counterTimer01, rcpController, ExafsScriptObserver,outputPreparer,detectorPreparer, xScan, yScan):
        self.xspressConfig = xspressConfig
        self.vortexConfig = vortexConfig
        self.d7a=d7a
        self.d7b=d7b
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
        self.beamEnabled = True
        
    def enableBeam(self):
        self.beamEnabled = True
    
    def disableBeam(self):
        self.beamEnabled = False
    
    def getMFD(self):
        return self.mfd
    
    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):
    
        print ""
        print "*********************"
        self.log("Preparing for map...")
        
        origScanPlotSettings = LocalProperties.check("gda.scan.useScanPlotSettings")
        
        experimentFullPath, experimentFolderName = self.determineExperimentPath(folderName)
    
        if(sampleFileName == None or sampleFileName == 'None'):
            sampleBean = None
        else:
            sampleBean  = BeansFactory.getBeanObject(experimentFullPath, sampleFileName)
            
        scanBean = BeansFactory.getBeanObject(experimentFullPath, scanFileName)
        detectorBean = BeansFactory.getBeanObject(experimentFullPath, detectorFileName)
        outputBean   = BeansFactory.getBeanObject(experimentFullPath, outputFileName)
    
        beanGroup = BeanGroup()
        beanGroup.setController(self.ExafsScriptObserver)
        beanGroup.setXmlFolder(experimentFullPath)
        beanGroup.setScannable(self.finder.find(scanBean.getXScannableName())) #TODO
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
    
        xScannable = self.finder.find(scanBean.getXScannableName())
        yScannable = self.finder.find(scanBean.getYScannableName())
        
        if xScannable==None:
            from gdascripts.parameters import beamline_parameters
            jythonNameMap = beamline_parameters.JythonNameSpaceMapping()
            xScannable=jythonNameMap.__getitem__(scanBean.getXScannableName())
            yScannable=jythonNameMap.__getitem__(scanBean.getYScannableName())
        
        nx = ScannableUtils.getNumberSteps(xScannable,scanBean.getXStart(), scanBean.getXEnd(),scanBean.getXStepSize()) + 1
        ny = ScannableUtils.getNumberSteps(yScannable,scanBean.getYStart(), scanBean.getYEnd(),scanBean.getYStepSize()) + 1
        self.log("Number x points: " + str(nx))
        self.log("Number y points: " + str(ny))
        
        energyList = [scanBean.getEnergy()]
        zScannablePos = scanBean.getZValue()
        
        self.detectorPreparer.prepare(scanBean, detectorBean, outputBean, experimentFullPath)
        
        self.detectorBeanFileName = experimentFullPath+detectorBean.getFluorescenceParameters().getConfigFileName()
        self._createMFD(nx, ny, scanBean.getXStepSize(), scanBean.getYStepSize(), detectorList)
#         self.mfd = MicroFocusWriterExtender(nx, ny, scanBean.getXStepSize(), scanBean.getYStepSize(),self.detectorBeanFileName, array(detectorList, Detector))

        for energy in energyList:
            
            
            energyScannable = self.finder.find(scanBean.getEnergyScannableName())
            self.log("Energy: " + str(energy))
            energyScannable.moveTo(energy) 
            self.mfd.setEnergyValue(energy)
            
            
            zScannable = self.finder.find(scanBean.getZScannableName())
            self.mfd.setZValue(zScannablePos)
            self.log("Using: " + scanBean.getXScannableName() + ", " + scanBean.getYScannableName() +", " + zScannable.getName())
            if(zScannablePos != None):
                zScannable.moveTo(zScannablePos)
            
            scanStart = time.asctime()
            
            self._redefineNexusMetadataForMaps(beanGroup)
            
            try:
                self._runMap(beanGroup, xScannable, yScannable, zScannable, detectorList,scanNumber,experimentFolderName,experimentFullPath,nx,ny)
            finally:
                scanEnd = time.asctime()
                if(origScanPlotSettings):
                    LocalProperties.set("gda.scan.useScanPlotSettings", "true")
                else:
                    LocalProperties.set("gda.scan.useScanPlotSettings", "false")
                self.log("Map start time " + str(scanStart))
                self.log("Map end time " + str(scanEnd))
                self.finish()
                
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
        
        # add the detector configuration file to the metadata
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
        LocalProperties.set("gda.scan.useScanPlotSettings", "true")

    def _setupForMap(self, beanGroup):

        if (LocalProperties.get("gda.mode") == 'live'):
            collectionTime = beanGroup.getScan().getCollectionTime()
            command_server = self.finder.find("command_server")    
            topupMonitor = command_server.getFromJythonNamespace("topupMonitor", None)    
            beam = command_server.getFromJythonNamespace("beam", None)
            detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
            trajBeamMonitor = command_server.getFromJythonNamespace("trajBeamMonitor", None)
            
            topupMonitor.setPauseBeforePoint(True)
            topupMonitor.setCollectionTime(collectionTime)
            topupMonitor.setPauseBeforeLine(False)
    
            beam.setPauseBeforePoint(True)
            beam.setPauseBeforeLine(True)
            
            if self.beamEnabled :
                self.finder.find("command_server").addDefault(beam);
                beam.setPauseBeforePoint(False)
            
            if(beanGroup.getDetector().getExperimentType() == "Fluorescence" and beanGroup.getDetector().getFluorescenceParameters().getDetectorType() == "Germanium"):
                #self.finder.find("command_server").addDefault(detectorFillingMonitor);
                detectorFillingMonitor.setPauseBeforePoint(True)
                detectorFillingMonitor.setPauseBeforeLine(False)
                detectorFillingMonitor.setCollectionTime(collectionTime)
            trajBeamMonitor.setActive(False)

        self._setupFromSampleParameters(beanGroup)
        
        self.rcpController.openPerspective("uk.ac.gda.microfocus.ui.MicroFocusPerspective")
        
    def _redefineNexusMetadataForMaps(self, beanGroup):
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
    
        # attenuators
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("D7A", str(jython_mapper.D7A()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXattenuator, "Attenuators"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("D7B", str(jython_mapper.D7B()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXattenuator, "Attenuators"))
    
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("energy", str(jython_mapper.energy()), EntryTypes.NXinstrument, NXinstrumentSubTypes.NXmonochromator, "DCM_energy"))

    def getDetectors(self, detectorBean, scanBean):
        expt_type = detectorBean.getExperimentType()
        if expt_type == "Transmission":
            for group in detectorBean.getDetectorGroups():
                if group.getName() == detectorBean.getTransmissionParameters().getDetectorType():
                    return self._createDetArray(group.getDetector(), scanBean)
        else:
            for group in detectorBean.getDetectorGroups():
                if group.getName() == detectorBean.getFluorescenceParameters().getDetectorType():
                    return self._createDetArray(group.getDetector(), scanBean)
 
    def finish(self):
        command_server = self.finder.find("command_server")
        beam = command_server.getFromJythonNamespace("beam", None)
        detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
        self.finder.find("command_server").removeDefault(beam);
        self.finder.find("command_server").removeDefault(detectorFillingMonitor);
        if self.mfd != None:
            self.mfd.closeWriter()
