from gdascripts.messages.handle_messages import simpleLog

from gda.scan import StaticScan
from gda.data.scan.datawriter import XasAsciiNexusDataWriter
            
class B18DetectorPreparer:
    def __init__(self, energy_scannable, mythen_scannable, sensitivities, sensitivity_units ,offsets, offset_units, ionc_gas_injector_scannables, xspressConfig, vortexConfig):
        self.energy_scannable = energy_scannable
        self.mythen_scannable = mythen_scannable
        #self.ionc_stanford_scannables = ionc_stanford_scannables
        self.sensitivities = sensitivities
        self.sensitivity_units = sensitivity_units
        self.offsets = offsets
        self.offset_units = offset_units
        self.ionc_gas_injector_scannables = ionc_gas_injector_scannables
        self.xspressConfig = xspressConfig
        self.vortexConfig = vortexConfig
        
    def prepare(self, scanBean, detectorBean, outputBean, experimentFullPath):
        if detectorBean.getExperimentType() == "Fluorescence":
            fluoresenceParameters = detectorBean.getFluorescenceParameters()
            if fluoresenceParameters.isCollectDiffractionImages():
                self._control_mythen(fluoresenceParameters, outputBean, experimentFullPath)
            detType = fluoresenceParameters.getDetectorType()
            xmlFileName = experimentFullPath + fluoresenceParameters.getConfigFileName()
            if detType == "Germanium":
                self.xspressConfig.initialize()
                xspressBean = self.xspressConfig.createBeanFromXML(xmlFileName)
                onlyShowFF = xspressBean.isOnlyShowFF()
                showDTRawValues = xspressBean.isShowDTRawValues()
                saveRawSpectrum = xspressBean.isSaveRawSpectrum()
                self.xspressConfig.configure(xmlFileName, onlyShowFF, showDTRawValues, saveRawSpectrum)
            elif detType == "Silicon":
                self.vortexConfig.initialize()
                vortexBean = self.vortexConfig.createBeanFromXML(xmlFileName)
                saveRawSpectrum = vortexBean.isSaveRawSpectrum()
                self.vortexConfig.configure(xmlFileName, saveRawSpectrum)
            self._control_all_ionc(fluoresenceParameters.getIonChamberParameters())
        elif detectorBean.getExperimentType() == "Transmission":
            transmissionParameters = detectorBean.getTransmissionParameters()
            if transmissionParameters.isCollectDiffractionImages():
                self._control_mythen(transmissionParameters, outputBean, experimentFullPath)   
            self._control_all_ionc(transmissionParameters.getIonChamberParameters())

    def completeCollection(self):
        # this will be called at the end of a loop of scans, or after an abort
        pass

    def _control_all_ionc(self, ion_chambers_bean):
        self._control_ionc(ion_chambers_bean, 0)
        self._control_ionc(ion_chambers_bean, 1)
        self._control_ionc(ion_chambers_bean, 2)

    def _control_ionc(self, ion_chambers_bean, ion_chamber_num):
        ion_chamber = ion_chambers_bean[ion_chamber_num]
#         change_sensitivity = ion_chamber.getChangeSensitivity()
        self._setup_amp_sensitivity(ion_chamber, ion_chamber_num)
        self._setup_amp_offset(ion_chamber, ion_chamber_num)
#         if change_sensitivity == True:
#             name = ion_chamber.getName()
#             simpleLog("Setting " + name + " stanford")
#             name = ion_chamber.getName()
#             gain = ion_chamber.getGain()
#             self.ionc_stanford_scannables[ion_chamber_num](gain)
        autoGas = ion_chamber.getAutoFillGas()
        gas_fill1_pressure = str(ion_chamber.getPressure() * 1000.0)
        gas_fill1_period = str(ion_chamber.getGas_fill1_period_box())
        gas_fill2_pressure = str(ion_chamber.getTotalPressure() * 1000.0)
        gas_fill2_period = str(ion_chamber.getGas_fill2_period_box())
        flushString = str(ion_chamber.getFlush())
        purge_pressure = "25.0"
        purge_period = "120.0"
        gas_select_val = "0"
        if autoGas == True:
            self.ionc_gas_injector_scannables[ion_chamber_num]([purge_pressure, purge_period, gas_fill1_pressure, gas_fill1_period, gas_fill2_pressure, gas_fill2_period, gas_select_val, flushString])

    def _setup_amp_sensitivity(self, ionChamberParams, index):
        if ionChamberParams.getChangeSensitivity():
            ionChamberName = ionChamberParams.getName()
            if ionChamberParams.getGain() == None or ionChamberParams.getGain() == "":
                return
            sensitivity, units = ionChamberParams.getGain().split()
            try:
                print "Changing sensitivity of",ionChamberName,"to",ionChamberParams.getGain()
                self.sensitivities[index](sensitivity)
                self.sensitivity_units[index](units)
            except Exception, e:
                print "Exception while trying to change the sensitivity of ion chamber",ionChamberName
                print "Set the ion chamber sensitivity manually, uncheck the box in the Detector Parameters editor and restart the scan"
                print "Please report this problem to Data Acquisition"
                raise e
            
    def _setup_amp_offset(self, ionChamberParams, index):
        if ionChamberParams.getChangeSensitivity():
            ionChamberName = ionChamberParams.getName()
            if ionChamberParams.getOffset() == None or ionChamberParams.getOffset() == "":
                return
            offset, units = ionChamberParams.getOffset().split()
            try:
                print "Changing amp offset of",ionChamberName,"to",ionChamberParams.getOffset()
                self.offsets[index](offset)
                self.offset_units[index](units)
            except Exception, e:
                print "Exception while trying to change the sensitivity of ion chamber",ionChamberName
                print "Set the ion chamber sensitivity manually, uncheck the box in the Detector Parameters editor and restart the scan"
                print "Please report this problem to Data Acquisition"
                raise e

    def _control_mythen(self, bean, outputBean, experimentFullPath):
        
        experimentFolderName = experimentFullPath[experimentFullPath.find("xml")+4:]
        nexusSubFolder = experimentFolderName +"/" + outputBean.getNexusDirectory()
        asciiSubFolder = experimentFolderName +"/" + outputBean.getAsciiDirectory()
        
        print "Moving DCM for Mythen image..."
        energyForMythen = bean.getMythenEnergy()
        self.energy_scannable(energyForMythen)
        
        collectionTime = bean.getMythenTime()
        self.mythen_scannable.setCollectionTime(collectionTime)

        self.mythen_scannable.setSubDirectory(experimentFolderName)
        dataWriter = XasAsciiNexusDataWriter();
        dataWriter.setRunFromExperimentDefinition(False)
        dataWriter.setNexusFileNameTemplate(nexusSubFolder+"/%d-mythen.nxs")
        dataWriter.setAsciiFileNameTemplate(asciiSubFolder+"/%d-mythen.dat")
  
        staticscan = StaticScan([self.mythen_scannable])
        staticscan.setDataWriter(dataWriter)
        print "Collecting a diffraction image..."
        staticscan.run()
        #the following two lines are to temporarily fix a bug where the jython server still thinks the mythen is in a scan after it has written data.
        # FIXME is this still necessary in 8.36???
        
        from gda.jython import JythonServerFacade
        JythonServerFacade.getInstance().setScanStatus(0)
        print "Diffraction scan complete."
#         
#         
# 
#     def _setUpDataWriter(self,thisscan,scanBean,detectorBean,sampleBean,outputBean,sampleName,descriptions,repetition,experimentFolderName,experimentFullPath):
#         nexusSubFolder = experimentFolderName +"/" + outputBean.getNexusDirectory()
#         asciiSubFolder = experimentFolderName +"/" + outputBean.getAsciiDirectory()
#         nexusFileNameTemplate = nexusSubFolder +"/"+ sampleName+"_%d_"+str(repetition)+".nxs"
#         asciiFileNameTemplate = asciiSubFolder +"/"+ sampleName+"_%d_"+str(repetition)+".dat"
#         if LocalProperties.check(NexusDataWriter.GDA_NEXUS_BEAMLINE_PREFIX):
#             nexusFileNameTemplate = nexusSubFolder +"/%d_"+ sampleName+"_"+str(repetition)+".nxs"
#             asciiFileNameTemplate = asciiSubFolder +"/%d_"+ sampleName+"_"+str(repetition)+".dat"
# 
#         # create XasAsciiNexusDataWriter object and give it the parameters
#         dataWriter = XasAsciiNexusDataWriter()
#         dataWriter.setRunFromExperimentDefinition(True);
#         dataWriter.setScanBean(scanBean);
#         dataWriter.setDetectorBean(detectorBean);
#         dataWriter.setSampleBean(sampleBean);
#         dataWriter.setOutputBean(outputBean);
#         dataWriter.setSampleName(sampleName);
#         dataWriter.setXmlFolderName(experimentFullPath)
#         dataWriter.setXmlFileName(self._determineDetectorFilename(detectorBean))
#         dataWriter.setDescriptions(descriptions);
#         dataWriter.setNexusFileNameTemplate(nexusFileNameTemplate);
#         dataWriter.setAsciiFileNameTemplate(asciiFileNameTemplate);
#         # get the ascii file format configuration (if not set here then will get it from the Finder inside the Java class
#         asciidatawriterconfig = self.outputPreparer.getAsciiDataWriterConfig(scanBean)
#         if asciidatawriterconfig != None :
#             dataWriter.setConfiguration(asciidatawriterconfig)
#         thisscan.setDataWriter(dataWriter)
#         return thisscan

