from gdascripts.messages.handle_messages import simpleLog

from gda.data.scan.datawriter import XasAsciiNexusDataWriter
from gda.jython import ScriptBase
from gda.scan import StaticScan
            
class B18DetectorPreparer:
    def __init__(self, energy_scannable, mythen_scannable, sensitivities, sensitivity_units ,offsets, offset_units, ionc_gas_injector_scannables, xspressConfig, vortexConfig, xspress3Config):
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
        self.xspress3Config = xspress3Config
        
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
            elif detType == "Xspress3":
                self.xspress3Config.initialize()
                self.xspress3Config.configure(xmlFileName,None)
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

    def _control_ionc(self, ion_chambers_bean_array, ion_chamber_num):
        ionChamberParams = ion_chambers_bean_array[ion_chamber_num]
        ScriptBase.checkForPauses()
        self._setup_amp_sensitivity(ionChamberParams, ion_chamber_num)
        ScriptBase.checkForPauses()
        self._setup_amp_offset(ionChamberParams, ion_chamber_num)
        ScriptBase.checkForPauses()
        self._setup_ionc_gas(ionChamberParams, ion_chamber_num)
        
    def _setup_ionc_gas(self, ionChamberParams, ion_chamber_num):
        gas_scannable = self.ionc_gas_injector_scannables[ion_chamber_num]
        
        autoGas = ionChamberParams.getAutoFillGas()
        if autoGas == True:

            gas_fill1_pressure = str(ionChamberParams.getPressure() * 1000.0)
            gas_fill1_period = str(ionChamberParams.getGas_fill1_period_box())
            gas_fill2_pressure = str(ionChamberParams.getTotalPressure() * 1000.0)
            gas_fill2_period = str(ionChamberParams.getGas_fill2_period_box())
            flushString = str(ionChamberParams.getFlush())
            purge_pressure = "25.0"
            purge_period = "120.0"
            
            gas_select = ionChamberParams.getGasType()
            gas_select_val = "-1"
            gas_report_string = "He"
            if gas_select == "Kr":
                gas_select_val = "0"
                gas_report_string = "He + Kr"
            elif gas_select == "N":
                gas_select_val = "1"
                gas_report_string = "He + N2"
            elif gas_select == "Ar":
                gas_select_val = "2"
                gas_report_string = "He + Ar"

            print "Changing gas of",ionChamberParams.getName(),"to",gas_report_string,"for",ionChamberParams.getPercentAbsorption(),"% absorption"
            gas_scannable([purge_pressure, purge_period, gas_fill1_pressure, gas_fill1_period, gas_fill2_pressure, gas_fill2_period, gas_select_val, flushString])

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
        
        energyForMythen = bean.getMythenEnergy()
        print "Moving DCM for Mythen image to", energyForMythen
        self.energy_scannable(energyForMythen)
        
        collectionTime = bean.getMythenTime()
        print "Setting Mythen collection time to",collectionTime
        self.mythen_scannable.setCollectionTime(collectionTime)

        self.mythen_scannable.setSubDirectory(experimentFolderName)
        dataWriter = XasAsciiNexusDataWriter();
        dataWriter.setRunFromExperimentDefinition(False)
        dataWriter.setNexusFileNameTemplate(nexusSubFolder+"/%d-mythen.nxs")
        dataWriter.setAsciiFileNameTemplate(asciiSubFolder+"/%d-mythen.dat")
  
        staticscan = StaticScan([self.mythen_scannable, self.energy_scannable])
        staticscan.setDataWriter(dataWriter)
        print "Collecting a diffraction image..."
        staticscan.run()
        print "Diffraction scan complete."