from gdascripts.messages.handle_messages import simpleLog
from gda.scan import StaticScan
from gda.factory import Finder
from gda.device.detector.xspress import Xspress2DetectorConfiguration
from gda.device.detector import VortexDetectorConfiguration
            
class B18DetectorPreparer:
    def __init__(self, energy_scannable, mythen_scannable, ionc_stanford_scannables, ionc_gas_injector_scannables):
        self.energy_scannable = energy_scannable
        self.mythen_scannable = mythen_scannable
        self.ionc_stanford_scannables = ionc_stanford_scannables
        self.ionc_gas_injector_scannables = ionc_gas_injector_scannables

    def prepare(self, detectorParameters, outputParameters, scriptFolder):
        if detectorParameters.getExperimentType() == "Fluorescence":
            fluoresenceParameters = detectorParameters.getFluorescenceParameters()
            if fluoresenceParameters.isCollectDiffractionImages():
                self._control_mythen(fluoresenceParameters) 
            self.configFluoDetector(detectorParameters, outputParameters, scriptFolder)
            self._control_all_ionc(fluoresenceParameters.getIonChamberParameters())
        elif detectorParameters.getExperimentType() == "Transmission":
            transmissionParameters = detectorParameters.getTransmissionParameters()
            if transmissionParameters.isCollectDiffractionImages():
                self._control_mythen(transmissionParameters)   
            self._control_all_ionc(transmissionParameters.getIonChamberParameters())

    def _control_all_ionc(self, ion_chambers_bean):
        self._control_ionc(ion_chambers_bean, 0)
        self._control_ionc(ion_chambers_bean, 1)
        self._control_ionc(ion_chambers_bean, 2)

    def _control_ionc(self, ion_chambers_bean, ion_chamber_num):
        ion_chamber = ion_chambers_bean[ion_chamber_num]
        change_sensitivity = ion_chamber.getChangeSensitivity()
        if change_sensitivity == True:
            name = ion_chamber.getName()
            simpleLog("Setting", name, "stanford")
            gain = ion_chamber.getGain()
            self.ionc_stanford_scannables[ion_chamber_num](gain)  
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

    def _control_mythen(self, bean):
        print "Moving DCM..."
        energyForMythen = bean.getMythenEnergy()
        self.energy_scannable(energyForMythen)
        print "Collecting a diffraction image..."
        collectionTime = bean.getMythenTime()
        self.mythen_scannable.setCollectionTime(collectionTime)
        staticscan = StaticScan([self.mythen_scannable])
        staticscan.run()
        print "Diffraction scan complete."
        
    """
    Validates the bean which defines the detectors and then configures the vortex or xspress
    based on the xml file whose name is in the bean.
    """
    def configFluoDetector(self, detectorParameters, outputParameters, scriptFolder):
        detType = detectorParameters.getFluorescenceParameters().getDetectorType()
        fullFileName = scriptFolder + detectorParameters.getFluorescenceParameters().getConfigFileName()
        print "configuring", detType, "detector using", fullFileName
        if detType == "Germanium":
            Xspress2DetectorConfiguration(Finder.getInstance().find("ExafsScriptObserver"), fullFileName, None, outputParameters).configure()
        else:
            VortexDetectorConfiguration(Finder.getInstance().find("ExafsScriptObserver"), fullFileName, None, outputParameters).configure()
