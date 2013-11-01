import array

from java.lang import InterruptedException
from java.lang import System
from xas_scan import XasScan

from xes import calcExpectedPositions, offsetsStore, setOffsets
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
    
    def __init__(self, xas,loggingcontroller, detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, XASLoggingScriptController, ExafsScriptObserver, sample_x, sample_y, sample_z, sample_rot, sample_fine_rot,twodplotter,I1,monoenergy,XESEnergy,XESBragg):
        self.xas = xas
        self.detectorPreparer = detectorPreparer
        self.samplePreparer = samplePreparer
        self.outputPreparer = outputPreparer
        self.loggingcontroller = loggingcontroller
        self.commandQueueProcessor=commandQueueProcessor
        self.XASLoggingScriptController=XASLoggingScriptController
        self.ExafsScriptObserver=ExafsScriptObserver
        self.sample_x=sample_x
        self.sample_y=sample_y
        self.sample_z=sample_z
        self.sample_rot=sample_rot
        self.sample_fine_rot=sample_fine_rot
        self.twodplotter = twodplotter
        self.I1 = I1
        self.mono_energy = monoenergy
        self.xes_energy = XESEnergy
        # variables used in XasScan class which should be set here
        self.energy_scannable = monoenergy
        self.analyserAngle = XESBragg
        self.moveMonoToStartBeforeScan=True
        self.useItterator=True
        self.handleGapConverter=False
        
    def __call__ (self,sampleFileName, scanFileName, detectorFileName, outputFileName, experimentFullPath, numRepetitions= 1, validation=True):

        self.experimentFullPath, self.experimentFolderName = self.determineExperimentPath(experimentFullPath)
        self.experimentFullPath = self.experimentFullPath + "/"

        # Create the beans from the file names
        self.sampleBean = BeansFactory.getBeanObject(self.experimentFullPath, sampleFileName)
        self.scanBean = BeansFactory.getBeanObject(self.experimentFullPath, scanFileName)
        self.detectorBean = BeansFactory.getBeanObject(self.experimentFullPath, detectorFileName)
        self.outputBean = BeansFactory.getBeanObject(self.experimentFullPath, outputFileName)

        # create unique ID for this scan (all repetitions will share the same ID)
        scriptType = "Xes"
        scan_unique_id = LoggingScriptController.createUniqueID(scriptType);
        
        # give the beans to the xasdatawriter class to help define the folders/filenames 
        self.beanGroup = BeanGroup()
        self.beanGroup.setController(self.ExafsScriptObserver)
        self.beanGroup.setXmlFolder(self.experimentFolderName)
        self.beanGroup.setExperimentFolderName(self.experimentFullPath)
        self.beanGroup.setSample(self.sampleBean)
        self.beanGroup.setDetector(self.detectorBean)
        self.beanGroup.setOutput(self.outputBean)
        self.beanGroup.setScan(self.scanBean)
        
        
        # Calilbrate the spectrometer (set the offsets) before we do anything
        offsetStoreName = self.scanBean.getOffsetsStoreName()
        if offsetStoreName != None and offsetStoreName != "" :
            print "Applying offsets from store named",str(offsetStoreName)
            offsetsStore.applyfrom(offsetStoreName)
        else:
            print "Not changing the XES spectrometer calibration settings"
        
        # if get here then its an XES step scan
        self._runCorrectScanType(numRepetitions, validation,scan_unique_id)
        

    def _runCorrectScanType(self, numRepetitions, validation,scan_unique_id):

        if self.analyserAngle.isBusy():
            self.log( "XESBragg is moving. Waiting for it to finish...")
            self.analyserAngle.waitWhileBusy()
            self.log( "XESBragg move completed.")


        scanType = self.beanGroup.getScan().getScanType()
        # work out which type of XES scan to run
        if scanType == XesScanParameters.FIXED_XES_SCAN_XAS:
            self.log( "Starting XAS scan with fixed analyser energy...")
            self._doXASScan(numRepetitions, validation,scan_unique_id)
            return
        
        elif scanType == XesScanParameters.FIXED_XES_SCAN_XANES:
            self.log( "Starting XANES scan with fixed analyser energy...")
            self._doXASScan(numRepetitions, validation,scan_unique_id)
            return

        elif scanType == XesScanParameters.SCAN_XES_FIXED_MONO:
            self.log( "Starting XES scan with fixed mono...")
            print""
            self.log( "Output to",self.experimentFolderName)
            self.xes_args = [self.xes_energy, self.scanBean.getXesInitialEnergy(), self.scanBean.getXesFinalEnergy(), self.scanBean.getXesStepSize(), self.mono_energy, self.scanBean.getMonoEnergy()]
            
            # I20 always moves things back to initial positions after each scan. To save time, move mono to intial position here
            self.log( "Moving spectrometer to initial position of " + str(self.scanBean.getXesInitialEnergy()))
            self.xes_energy.moveTo(self.scanBean.getXesInitialEnergy())
            self.log( "Move done.")

        elif scanType == XesScanParameters.SCAN_XES_SCAN_MONO:
            self.log( "Starting 2D scan over XES and mono...")
            print""
            self.log( "Output to",self.experimentFolderName)

            ef_args = [self.xes_energy, self.scanBean.getXesInitialEnergy(), self.scanBean.getXesFinalEnergy(), self.scanBean.getXesStepSize()]
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
            
            # I20 always moves things back to initial positions after ecah scan. To save time, move mono to intial position here
            self.log( "Moving mono and spectrometer to initial positions...")
            self.xes_energy(self.scanBean.getXesInitialEnergy())
            self.mono_energy(self.scanBean.getMonoInitialEnergy())
            self.xes_energy.waitWhileBusy()
            self.mono_energy.waitWhileBusy()
            self.log( "Moves done.")

        else:
            raise "scan type in XES Scan Parameters bean/xml not acceptable"

        try:
            self._doLooping(self.beanGroup,"Xes",scan_unique_id, numRepetitions, self.experimentFullPath, self.experimentFolderName, self.loggingcontroller, self.sampleBean, self.scanBean, self.detectorBean, self.outputBean)
        finally:
            # make sure the plotter is switched off
            self.twodplotter.atScanEnd()
            
    # override the xas_scan method. Now instead of building the scan command from a XasScanParameter object, simply return the args built earlier
    def buildScanArguments(self, scanBean, sampleScannables, outputScannables, detectorList, signalParameters, loggingbean):
        args = list(self.xes_args)
        if sampleScannables != None:
            args += sampleScannables
        if outputScannables != None:
            args += outputScannables
        args += detectorList
        args += signalParameters
        args += [loggingbean]
        return args
            
    def _doXASScan(self,numRepetitions, validation,scan_unique_id):
        print""
        self.log( "Output to",self.experimentFolderName)

        # add xes_energy, analyserAngle to the defaults and then call the xas command
        xas_scanfilename = self.beanGroup.getScan().getScanFileName()

        self.log( "moving XES analyser stage to collect at", self.beanGroup.getScan().getXesEnergy())
        initialXESEnergy = self.xes_energy()
        self.xes_energy.waitWhileBusy()
        self.xes_energy(self.beanGroup.getScan().getXesEnergy())
        ScannableCommands.add_default([self.xes_energy,self.analyserAngle])
        
        try:
            self.outputPreparer.mode = "xes"
            self.xas(self.beanGroup.getSample(), xas_scanfilename, self.beanGroup.getDetector(), self.beanGroup.getOutput(), self.experimentFullPath, numRepetitions, validation)
        finally:
            self.log( "cleaning up scan defaults")
            self.outputPreparer.mode = "xas"
            ScannableCommands.remove_default([self.xes_energy,self.analyserAngle])
            self.xes_energy(initialXESEnergy)