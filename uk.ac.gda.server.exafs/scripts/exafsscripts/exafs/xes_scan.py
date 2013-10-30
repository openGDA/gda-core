import array
from java.lang import InterruptedException
from java.lang import System
from xas_scan import XasScan
from exafsscripts.exafs.i20.I20SampleIterators import XASXANES_Roomtemp_Iterator, XES_Roomtemp_Iterator, XASXANES_Cryostat_Iterator
from gda.configuration.properties import LocalProperties
from gda.data.scan.datawriter import  XasAsciiDataWriter
from gda.device import DeviceException
from gda.exafs.scan import BeanGroup, BeanGroups
from gda.exafs.scan import RepetitionsProperties,ScanStartedMessage
from gda.exafs.scan import ExafsScanPointCreator,XanesScanPointCreator
from gda.jython import ScriptBase
from gda.jython.commands import ScannableCommands
from gda.jython.scriptcontroller.event import ScanCreationEvent, ScanFinishEvent, ScriptProgressEvent
from gda.jython.scriptcontroller.logging import LoggingScriptController
from gda.jython.scriptcontroller.logging import XasLoggingMessage
from gda.jython.scriptcontroller.logging import XasProgressUpdater
from gda.scan import ScanBase, ContinuousScan, ConcurrentScan
from gda.util import Element
from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.beans.exafs import XanesScanParameters
from uk.ac.gda.beans.exafs import XesScanParameters
from uk.ac.gda.beans.exafs.i20 import I20SampleParameters, CryostatParameters
from uk.ac.gda.doe import DOEUtils

class I20XesScan(XasScan):
    
    def __init__(self, detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, ExafsScriptObserver, XASLoggingScriptController, datawriterconfig, original_header, energy_scannable, ionchambers, sample_x, sample_y, sample_z, sample_rot, sample_fine_rot, twodplotter, I1,XESEnergy,XESBragg, xes_offsets):
        XasScan.__init__(self,detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, ExafsScriptObserver, XASLoggingScriptController, datawriterconfig, original_header, energy_scannable, ionchambers, configXspressDeadtime=False, moveMonoToStartBeforeScan=True, useItterator=True, handleGapConverter=False)
        self.sample_x=sample_x
        self.sample_y=sample_y
        self.sample_z=sample_z
        self.sample_rot=sample_rot
        self.sample_fine_rot=sample_fine_rot
        self.twodplotter = twodplotter
        self.I1 = I1
        self.XESEnergy = XESEnergy
        self.XESBragg = XESBragg
        self.xes_offsets = xes_offsets
        self.xes_args = []
        self.isXESScanArgs = True
        
    def __call__ (self,sampleFileName, scanFileName, detectorFileName, outputFileName, experimentFullPath, numRepetitions= 1, validation=True):
        self.experimentFullPath, self.experimentFolderName = self.determineExperimentPath(experimentFullPath)
        self.experimentFullPath = self.experimentFullPath + "/"
        self.sampleBean = BeansFactory.getBeanObject(self.experimentFullPath, sampleFileName)
        self.scanBean = BeansFactory.getBeanObject(self.experimentFullPath, scanFileName)
        self.detectorBean = BeansFactory.getBeanObject(self.experimentFullPath, detectorFileName)
        self.outputBean = BeansFactory.getBeanObject(self.experimentFullPath, outputFileName)
        scriptType = "Xes"
        scan_unique_id = LoggingScriptController.createUniqueID(scriptType);
        self.beanGroup = BeanGroup()
        self.beanGroup.setController(self.ExafsScriptObserver)
        self.beanGroup.setXmlFolder(self.experimentFolderName)
        self.beanGroup.setExperimentFolderName(self.experimentFullPath)
        self.beanGroup.setSample(self.sampleBean)
        self.beanGroup.setDetector(self.detectorBean)
        self.beanGroup.setOutput(self.outputBean)
        self.beanGroup.setScan(self.scanBean)
        offsetStoreName = self.scanBean.getOffsetsStoreName()
        if offsetStoreName != None and offsetStoreName != "" :
            print "Applying offsets from store named",str(offsetStoreName)
            self.xes_offsets.apply(offsetStoreName)
        else:
            print "Not changing the XES spectrometer calibration settings"
        # if get here then its an XES step scan
        self._runCorrectScanType(numRepetitions, validation,scan_unique_id)
    
    def _run_fixed_xes_scan_xas(self, numRepetitions, validation, scan_unique_id):
        self.log("Starting XAS scan with fixed analyser energy...")
        self._doXASScan(numRepetitions, validation,scan_unique_id)
        return
    
    def _run_fixed_scan_xanes(self, numRepetitions, validation, scan_unique_id):
        self.log("Starting XANES scan with fixed analyser energy...")
        self._doXASScan(numRepetitions, validation,scan_unique_id)
        return
    
    def _run_xes_fixed_mono(self):
        self.log("Starting XES scan with fixed mono...")
        self.log("Output to",self.experimentFolderName)
        self.xes_args = [self.XESEnergy, self.scanBean.getXesInitialEnergy(), self.scanBean.getXesFinalEnergy(), self.scanBean.getXesStepSize(), self.energy_scannable, self.scanBean.getMonoEnergy()]
        self.log("Moving spectrometer to initial position of " + str(self.scanBean.getXesInitialEnergy()))
        self.XESEnergy.moveTo(self.scanBean.getXesInitialEnergy())
        self.log("Move done.")
    
    def _run_xes_scan_mono(self):
        self.log("Starting 2D scan over XES and mono...")
        self.log("Output to",self.experimentFolderName)
        ef_args = [self.XESEnergy, self.scanBean.getXesInitialEnergy(), self.scanBean.getXesFinalEnergy(), self.scanBean.getXesStepSize()]
        e0_args = [self.mono_energy, self.scanBean.getMonoInitialEnergy(), self.scanBean.getMonoFinalEnergy(), self.scanBean.getMonoStepSize()]
        if self.scanBean.getLoopChoice() == XesScanParameters.LOOPOPTIONS[0]:
            self.xes_args = ef_args + e0_args
            self.twodplotter.setX_colName("XESEnergy")
            self.twodplotter.setY_colName("bragg1")
            self.twodplotter.setZ_colName("FFI1")
            self.twodplotter.setXArgs(self.scanBean.getXesInitialEnergy(),self.scanBean.getXesFinalEnergy(),self.scanBean.getXesStepSize())
            self.twodplotter.setYArgs(self.scanBean.getMonoInitialEnergy(),self.scanBean.getMonoFinalEnergy(),self.scanBean.getMonoStepSize())
        else:
            self.xes_args = e0_args + ef_args
            self.twodplotter.setY_colName("XESEnergy")
            self.twodplotter.setX_colName("bragg1")
            self.twodplotter.setZ_colName("FFI1")
            self.twodplotter.setYArgs(self.scanBean.getXesInitialEnergy(),self.scanBean.getXesFinalEnergy(),self.scanBean.getXesStepSize())
            self.twodplotter.setXArgs(self.scanBean.getMonoInitialEnergy(),self.scanBean.getMonoFinalEnergy(),self.scanBean.getMonoStepSize())
        self.xes_args += [self.twodplotter]
        self.log("Moving mono and spectrometer to initial positions...")
        self.XESEnergy(self.scanBean.getXesInitialEnergy())
        self.mono_energy(self.scanBean.getMonoInitialEnergy())
        self.XESEnergy.waitWhileBusy()
        self.mono_energy.waitWhileBusy()
        self.log("Moves done.")
    
    def _runCorrectScanType(self, numRepetitions, validation, scan_unique_id):
        if self.XESBragg.isBusy():
            self.log("XESBragg is moving. Waiting for it to finish...")
            self.XESBragg.waitWhileBusy()
            self.log("XESBragg move completed.")
        scanType = self.beanGroup.getScan().getScanType()
        if scanType == XesScanParameters.FIXED_XES_SCAN_XAS:
            self._run_fixed_xes_scan_xas(numRepetitions, validation, scan_unique_id)
        elif scanType == XesScanParameters.FIXED_XES_SCAN_XANES:
            self._run_fixed_scan_xanes(numRepetitions, validation, scan_unique_id)
        elif scanType == XesScanParameters.SCAN_XES_FIXED_MONO:
            self._run_xes_fixed_mono()
        elif scanType == XesScanParameters.SCAN_XES_SCAN_MONO:
            self._run_xes_scan_mono()
        else:
            raise "scan type in XES Scan Parameters bean/xml not acceptable"
        try:
            self._doLooping(self.beanGroup,"Xes",scan_unique_id, numRepetitions, self.experimentFullPath, self.experimentFolderName, self.XASLoggingScriptController, self.sampleBean, self.scanBean, self.detectorBean, self.outputBean)
        finally:
            self.twodplotter.atScanEnd()# make sure the plotter is switched off
            
    def buildScanArguments(self, scanBean, sampleScannables, outputScannables, detectorList, signalParameters, loggingbean):
        if self.isXESScanArgs==True:
            xas_scannable = self._createAndconfigureXASScannable()
            xas_scannable.setDetectors(detectorList)
            args = list(self.xes_args)
            args = self.addScannableArgs(args, sampleScannables, outputScannables, detectorList, signalParameters, loggingbean)
            return args
        else:
            return XASScan.buildScanArguments()
    
    def _doXASScan(self,numRepetitions, validation,scan_unique_id):
        self.log("Output to",self.outputfolderName)
        self.log("moving XES analyser stage to collect at", self.beanGroup.getScan().getXesEnergy())
        initialXESEnergy = self.XESEnergy()
        self.XESEnergy.waitWhileBusy()
        self.XESEnergy(self.beanGroup.getScan().getXesEnergy())
        ScannableCommands.add_default([self.XESEnergy,self.XESBragg])
        try:
            self.outputPreparer.mode = "xes"
            self.isXESScanArgs=False
            XASScan.__call__(self.beanGroup.getSample(), xas_scanfilename, self.beanGroup.getDetector(), self.beanGroup.getOutput(), self.experimentFullPath, numRepetitions, validation)
        finally:
            self.isXESScanArgs=True
            self.outputPreparer.mode = "xas"
            ScannableCommands.remove_default([self.XESEnergy,self.XESBragg])
            self.XESEnergy(initialXESEnergy)
