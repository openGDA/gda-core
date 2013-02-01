from java.lang import InterruptedException
from java.lang import System
from xas_scan import XasScan
from exafs_environment import ExafsEnvironment
from BeamlineParameters import JythonNameSpaceMapping, FinderNameMapping

from gda.configuration.properties import LocalProperties
from gda.data.scan.datawriter import  XasAsciiDataWriter
from gda.device import DeviceException
from gda.exafs.scan import BeanGroup, BeanGroups
from gda.exafs.scan import RepetitionsProperties,ScanStartedMessage
from gda.exafs.scan import ExafsScanPointCreator,XanesScanPointCreator
from gda.factory import Finder
from gda.jython import ScriptBase
from gda.jython.commands import ScannableCommands
from gda.jython.scriptcontroller.event import ScanCreationEvent, ScanFinishEvent, ScriptProgressEvent
from gda.jython.scriptcontroller.logging import LoggingScriptController
from gda.jython.scriptcontroller.logging import XasLoggingMessage
from gda.jython.scriptcontroller.logging import XasProgressUpdater
from gda.scan import ScanBase, ContinuousScan, ConcurrentScan
from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.beans.exafs import XanesScanParameters
from uk.ac.gda.beans.exafs import XesScanParameters
from uk.ac.gda.beans.exafs import XasScanParameters
from uk.ac.gda.beans.exafs.i20 import I20SampleParameters


class I20XasScan(XasScan):
    
    def _doLooping(self,beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller):

        global jython_mapper
        jython_mapper = JythonNameSpaceMapping()
        finder = Finder.getInstance()
        
        # fluo detector, if in use
        if beanGroup.getDetector().getExperimentType() == 'Fluorescence':
            self.setDetectorCorrectionParameters(beanGroup)
            ScriptBase.checkForPauses()
            
        # ion chambers
        self.setUpIonChambers(beanGroup)
        
        # reference (filter) wheel
        if beanGroup.getSample().getUseSampleWheel():
            filter = beanGroup.getSample().getSampleWheelPosition()
            print "Moving filter wheel to",filter,"..."
            jython_mapper.filterwheel(filter)
            ScriptBase.checkForPauses()
            
        # I20 always moves things back to initial positions after ecah scan. To save time, move mono to intial position here
        scan =  beanGroup.getScan()
        energy_scannable_name = scan.getScannableName()
        energy_scannable = finder.find(energy_scannable_name)
        if energy_scannable != None and isinstance(scan,XasScanParameters):
            intialPosition = scan.getInitialEnergy()
            energy_scannable.asynchronousMoveTo(intialPosition)
            print "Moving",energy_scannable_name, "to initial position..."
        elif energy_scannable != None and isinstance(scan,XanesScanParameters): 
            intialPosition = scan.getRegions().get(0).getEnergy()
            energy_scannable.asynchronousMoveTo(intialPosition)
            print "Moving",energy_scannable_name, "to initial position..."

        # sample environments

        # room temperature (sample stage)
        try:
            if beanGroup.getDetector().getExperimentType() != 'XES' and beanGroup.getSample().getSampleEnvironment() == I20SampleParameters.SAMPLE_ENV[1] :
                sampleStageParameters = beanGroup.getSample().getRoomTemperatureParameters()
                numSamples = sampleStageParameters.getNumberOfSamples()
                for i in range(0,numSamples):
                    x = sampleStageParameters.getXs()[i]
                    y = sampleStageParameters.getYs()[i]
                    z = sampleStageParameters.getZs()[i]
                    rotation = sampleStageParameters.getRotations()[i]
                    roll = sampleStageParameters.getRolls()[i]
                    pitch = sampleStageParameters.getPitches()[i]
                    
                    finder = Finder.getInstance()
                    samx = finder.find("sample_x")
                    samy = finder.find("sample_y")
                    samz = finder.find("sample_z")
                    samrot = finder.find("sample_rot")
                    samroll = finder.find("sample_roll")
                    sampitch = finder.find("sample_pitch")
                    
                    if samx == None or samy ==None or samz == None or samrot == None or samroll == None or sampitch == None:
                        raise DeviceException("I20 scan script - could not find all sample stage motors!")
                    
                    
                    print "Moving sample stage to",x,y,z,rotation,roll,pitch,"..."
                    samx.asynchronousMoveTo(x)
                    samy.asynchronousMoveTo(y)
                    samz.asynchronousMoveTo(z)
                    samrot.asynchronousMoveTo(rotation)
                    samroll.asynchronousMoveTo(roll)
                    sampitch.asynchronousMoveTo(pitch)
                    samx.waitWhileBusy()
                    samy.waitWhileBusy()
                    samz.waitWhileBusy()
                    samrot.waitWhileBusy()
                    samroll.waitWhileBusy()
                    sampitch.waitWhileBusy()
                    print "Sample stage move complete.\n"
                    ScriptBase.checkForPauses()
                    
                    #TODO add to metadata?
                    
                    energy_scannable.waitWhileBusy()
                    self._doScan(beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller)
            else :
                energy_scannable.waitWhileBusy()
                self._doScan(beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller)
        finally:
            # remove extra columns from ionchambers output
            jython_mapper.ionchambers.setOutputLogValues(False) 
            ScriptBase.checkForPauses()
        
    def _beforeEachRepetition(self,beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller, repNum):
        times = []
        if isinstance(beanGroup.getScan(),XasScanParameters):
            times = ExafsScanPointCreator.getScanTimeArray(beanGroup.getScan())
        elif isinstance(beanGroup.getScan(),XanesScanParameters):
            times = XanesScanPointCreator.getScanTimeArray(beanGroup.getScan())
        if len(times) > 0:
            print "Setting detector frame times, using array of length",str(len(times)) + "..."
            jython_mapper = JythonNameSpaceMapping()
            jython_mapper.ionchambers.setTimes(times)
            ScriptBase.checkForPauses()
        return
        

    def setDetectorCorrectionParameters(self,beanGroup):
        jython_mapper = JythonNameSpaceMapping()
        
        scanObj = beanGroup.getScan()
        edgeEnergy = 0.0
        if isinstance(scanObj,XasScanParameters):
            edgeEnergy = scanObj.getEdgeEnergy()
            edgeEnergy /= 1000 # convert from eV to keV
        else :
            edgeEnergy = scanObj.getFinalEnergy() 
            edgeEnergy /= 1000 # convert from eV to keV
        jython_mapper.xspress2system.setDeadtimeCalculationEnergy(edgeEnergy)
 
    
    def setUpIonChambers(self,beanGroup):
        jython_mapper = JythonNameSpaceMapping()
    
        # determine max collection time
        scanBean = beanGroup.getScan()
        maxTime = 0;
        if isinstance(scanBean,XanesScanParameters):
            for region in scanBean.getRegions():
                if region.getTime() > maxTime:
                    maxTime = region.getTime()
                
        elif isinstance(scanBean,XasScanParameters):
            if scanBean.getEdgeTime() > maxTime:
                maxTime = scanBean.getEdgeTime()
            if scanBean.getExafsToTime() > maxTime:
                maxTime = scanBean.getExafsToTime()
            if scanBean.getExafsFromTime() > maxTime:
                maxTime = scanBean.getExafsFromTime()
            if scanBean.getExafsTime() > maxTime:
                maxTime = scanBean.getExafsTime()
            if scanBean.getPreEdgeTime() > maxTime:
                maxTime = scanBean.getPreEdgeTime()
    
        # set dark current time and handle any errors here
        if maxTime > 0:
            print "Setting ionchambers dark current collectiom time to",str(maxTime),"s."
            jython_mapper.ionchambers.setDarkCurrentCollectionTime(maxTime)
            jython_mapper.I1.setDarkCurrentCollectionTime(maxTime)

class I20XesScan(XasScan):
    
    def __init__(self, loggingcontroller, detectorPreparer, samplePreparer, outputPreparer, beamlineReverter):
        self.detectorPreparer = detectorPreparer
        self.samplePreparer = samplePreparer
        self.outputPreparer = outputPreparer
        self.loggingcontroller = loggingcontroller
        self.beamlineReverter = beamlineReverter

        
    def __call__ (self,sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, numRepetitions= 1, validation=True):

        # Create the beans from the file names
        xmlFolderName = ExafsEnvironment().getXMLFolder() + folderName + "/"
        sampleBean = BeansFactory.getBeanObject(xmlFolderName, sampleFileName)
        xesScanBean = BeansFactory.getBeanObject(xmlFolderName, scanFileName)
        detectorBean = BeansFactory.getBeanObject(xmlFolderName, detectorFileName)
        outputBean = BeansFactory.getBeanObject(xmlFolderName, outputFileName)

        finder_mapper = FinderNameMapping()
        jython_mapper = JythonNameSpaceMapping()
        loggingcontroller = Finder.getInstance().find("XASLoggingScriptController")

        # create unique ID for this scan (all repetitions will share the same ID)
        scriptType = "Xes"
        scan_unique_id = LoggingScriptController.createUniqueID(scriptType);

        # give the beans to the xasdatawriter class to help define the folders/filenames 
        beanGroup = BeanGroup()
        beanGroup.setController(Finder.getInstance().find("ExafsScriptObserver"))
        beanGroup.setScriptFolder(xmlFolderName)
        beanGroup.setExperimentFolderName(folderName)
        beanGroup.setSample(sampleBean)
        beanGroup.setDetector(detectorBean)
        beanGroup.setOutput(outputBean)
        beanGroup.setScan(xesScanBean)
        
        # if get here then its an XES step scan
        self._doLooping(beanGroup,folderName,numRepetitions, validation,scan_unique_id)
        
    def _doLooping(self,beanGroup,folderName,numRepetitions, validation,scan_unique_id):

        finder_mapper = FinderNameMapping()
        jython_mapper = JythonNameSpaceMapping()
        loggingcontroller = Finder.getInstance().find("XASLoggingScriptController")
        
        # ion chambers
        self.setUpIonChambers(beanGroup)

        if beanGroup.getSample().getSampleEnvironment() == I20SampleParameters.SAMPLE_ENV_XES[1] :
            
            sampleStageParameters = beanGroup.getSample().getRoomTemperatureParameters()
            numSamples = sampleStageParameters.getNumberOfSamples()
            for i in range(0,numSamples):
                x = sampleStageParameters.getXs()[i]
                y = sampleStageParameters.getYs()[i]
                z = sampleStageParameters.getZs()[i]
                rotation = sampleStageParameters.getRotations()[i]
                finerotation = sampleStageParameters.getFineRotations()[i]
                
                finder = Finder.getInstance()
                samx = finder.find("sample_x")
                samy = finder.find("sample_y")
                samz = finder.find("sample_z")
                samrot = finder.find("sample_rot")
                samfinerot = finder.find("sample_fine_rot")
                
                if samx == None or samy ==None or samz == None or samrot == None or samfinerot == None:
                    raise DeviceException("I20 XES scan script - could not find all sample stage motors!")
                
                
                print "Moving sample stage to",x,y,z,rotation,finerotation,"..."
                samx.asynchronousMoveTo(x)
                samy.asynchronousMoveTo(y)
                samz.asynchronousMoveTo(z)
                samrot.asynchronousMoveTo(rotation)
                samfinerot.asynchronousMoveTo(finerotation)
                samx.waitWhileBusy()
                samy.waitWhileBusy()
                samz.waitWhileBusy()
                samrot.waitWhileBusy()
                samfinerot.waitWhileBusy()
                print "Sample stage move complete.\n"
                ScriptBase.checkForPauses()
                
                self._doScan(beanGroup,folderName,numRepetitions, validation,scan_unique_id)
                
        else:
            self._doScan(beanGroup,folderName,numRepetitions, validation,scan_unique_id)


    def _doScan(self,beanGroup,folderName,numRepetitions, validation,scan_unique_id):

        finder_mapper = FinderNameMapping()
        jython_mapper = JythonNameSpaceMapping()
        loggingcontroller = Finder.getInstance().find("XASLoggingScriptController")
        # now that the scan has been defined, run it in a loop
        
        
                # work out which detectors to use (they will need to have been configured already by the GUI)
        detectorList = self._getDetectors(beanGroup.getDetector(), beanGroup.getScan()) 

        # get the relevant objects from the namespace
        xes_energy = jython_mapper.XESEnergy
        mono_energy = jython_mapper.bragg1
        analyserAngle = jython_mapper.XESBragg
        #L = jython_mapper.xtal_x

        scanType = beanGroup.getScan().getScanType()
        args = []

#        originalDataFormat = LocalProperties.get("gda.data.scan.datawriter.dataFormat")

        from gda.exafs.xes.XesUtils import XesMaterial
        type = 1
        if beanGroup.getScan().getAnalyserType() == str("Si"):
            type = 0
        xes_energy.setMaterialType(type)
        xes_energy.setCut1Val(beanGroup.getScan().getAnalyserCut0())
        xes_energy.setCut2Val(beanGroup.getScan().getAnalyserCut1())
        xes_energy.setCut3Val(beanGroup.getScan().getAnalyserCut2())
        
        if scanType == XesScanParameters.SCAN_XES_FIXED_MONO:
            print "Starting XES scan with fixed mono..."
            print""
            print "Output to",xmlFolderName
#            print "switching data output format to XesAsciiNexusDataWriter"
#            LocalProperties.set("gda.data.scan.datawriter.dataFormat","XesAsciiNexusDataWriter")
            args += [xes_energy, beanGroup.getScan().getXesInitialEnergy(), beanGroup.getScan().getXesFinalEnergy(), beanGroup.getScan().getXesStepSize(), mono_energy, beanGroup.getScan().getMonoEnergy()]
    
        elif scanType == XesScanParameters.SCAN_XES_SCAN_MONO:
            print "Starting 2D scan over XES and mono..."
            print""
            print "Output to",xmlFolderName
#            print "switching data output format to XesAsciiNexusDataWriter"
#            LocalProperties.set("gda.data.scan.datawriter.dataFormat","XesAsciiNexusDataWriter")

             # create scannable which will control 2D plotting in this mode
            jython_mapper.twodplotter.setX_colName(xes_energy.getInputNames()[0])
            jython_mapper.twodplotter.setY_colName(mono_energy.getInputNames()[0])
            jython_mapper.twodplotter.setZ_colName(finder_mapper.xmapMca.getExtraNames()[0])
            # note that users will have to open a 'plot 1' view or use the XESPlot perspective for this to work

            ef_args = [xes_energy, beanGroup.getScan().getXesInitialEnergy(), beanGroup.getScan().getXesFinalEnergy(), beanGroup.getScan().getXesStepSize()]
            e0_args = [mono_energy, beanGroup.getScan().getMonoInitialEnergy(), beanGroup.getScan().getMonoFinalEnergy(), beanGroup.getScan().getMonoStepSize()]
            
            if beanGroup.getScan().getLoopChoice() == XesScanParameters.LOOPOPTIONS[0]:
                args += ef_args + e0_args
            else:
                args += e0_args + ef_args
            args += [jython_mapper.twodplotter]

        elif scanType == XesScanParameters.FIXED_XES_SCAN_XAS:
            print "Starting XAS scan with fixed analyser energy..."
            print""
            print "Output to",xmlFolderName

            # add xes_energy, analyserAngle to the defaults and then call the xas command
            xas_scanfilename = beanGroup.getScan().getScanFileName()

            print "moving XES analyser stage to collect at", beanGroup.getScan().getXesEnergy()
            initialXESEnergy = xes_energy()
            xes_energy(beanGroup.getScan().getXesEnergy())
            ScannableCommands.add_default([xes_energy,analyserAngle])
            

            try:
                self.outputPreparer.mode = "xes"
                jython_mapper.xas(beanGroup.getSample(), xas_scanfilename, beanGroup.getDetector(), beanGroup.getOutput(), folderName, numRepetitions, validation)
            finally:
                print "cleaning up scan defaults"
                self.outputPreparer.mode = "xas"
                ScannableCommands.remove_default([xes_energy,analyserAngle])
                xes_energy(initialXESEnergy)
            return

        elif scanType == XesScanParameters.FIXED_XES_SCAN_XANES:
            print "Starting XANES scan with fixed analyser energy..."
            print""
            print "Output to",xmlFolderName
            # add xes_energy, analyserAngle, to the signal parameters bean and then call the xanes command
            xanes_scanfilename = beanGroup.getScan().getScanFileName()

            print "moving XES analyser stage to collect at", beanGroup.getScan().getXesEnergy()
            initialXESEnergy = xes_energy()
            xes_energy(beanGroup.getScan().getXesEnergy())
            ScannableCommands.add_default([xes_energy,analyserAngle])

            try:
                self.outputPreparer.mode = "xes"
                jython_mapper.xanes(beanGroup.getSample(), xanes_scanfilename, beanGroup.getDetector(), beanGroup.getOutput(), folderName, numRepetitions, validation)
            finally:
                print "cleaning up scan defaults"
                self.outputPreparer.mode = "xas"
                ScannableCommands.remove_default([xes_energy,analyserAngle])
                xes_energy(initialXESEnergy)
            return
        else:
            raise "scan type in XES Scan Parameters bean/xml not acceptable"

        # run the before scan script
        self._runScript(beanGroup.getOutput().getBeforeScriptName())

        args += [analyserAngle]
        args += detectorList 
        args += [beanGroup.getScan().getXesIntegrationTime()]
        signalParameters = self._getSignalList(beanGroup.getOutput())
        if len(signalParameters) > 0:
            args += signalParameters

        # reset the properties used to control repetition behaviour
        LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY,"false")
        LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
        LocalProperties.set(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY,str(numRepetitions))
        repetitionNumber = 0
        timeRepetitionsStarted = System.currentTimeMillis();

        scriptType = "Xes"
        # set the dark current and integration time for all detectors
        itime = beanGroup.getScan().getXesIntegrationTime()
        for det in detectorList:
            det.setCollectionTime(itime)
        
        try:
            while True:
                repetitionNumber+= 1
                beanGroup.setScanNumber(repetitionNumber)
                XasAsciiDataWriter.setBeanGroup(beanGroup)

            # send out initial messages for logging and display to user
                outputFolder = beanGroup.getOutput().getAsciiDirectory()+ "/" + beanGroup.getOutput().getAsciiFileName()
                #print "Starting "+scriptType+" scan...", str(repetitionNumber)
                initialPercent = str(int((float(repetitionNumber - 1) / float(numRepetitions)) * 100)) + "%" 
                logmsg = XasLoggingMessage(scan_unique_id, scriptType, "Starting "+scriptType+" scan...", str(repetitionNumber), str(numRepetitions), initialPercent,str(0),str(0),beanGroup.getScan(),outputFolder)
                loggingcontroller.update(None,logmsg)
                loggingcontroller.update(None,ScanStartedMessage(beanGroup.getScan(),beanGroup.getDetector())) # informs parts of the UI about current scan
                loggingbean = XasProgressUpdater(loggingcontroller,logmsg,timeRepetitionsStarted)
                args += [loggingbean]
                try:
                    loggingcontroller.update(None, ScriptProgressEvent("Running scan"))
                    ScanBase.interrupted = False
                    if numRepetitions > 1:
                        print ""
                        print "Starting repetition", str(repetitionNumber),"of",numRepetitions
                    thisscan = ConcurrentScan(args)
                    thisscan = self._setUpDataWriter(thisscan,beanGroup)
                    loggingcontroller.update(None, ScanCreationEvent(thisscan.getName()))
                    thisscan.runScan()
                except InterruptedException, e:
                    if LocalProperties.get(RepetitionsProperties.SKIP_REPETITION_PROPERTY) == "true":
                        LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
                        ScanBase.interrupted = False
                    # only wanted to skip this repetition, so absorb the exception and continue the loop
                        if numRepetitions > 1:
                            print "Repetition", str(repetitionNumber),"skipped."
                    else:
                        print e
                        raise # any other exception we are not expecting so raise whatever this is to abort the script
                       
                #update observers
                loggingcontroller.update(None, ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK));

                # run the after scan script
                self._runScript(beanGroup.getOutput().getAfterScriptName())

                #check if halt after current repetition set to true
                if numRepetitions > 1 and LocalProperties.get(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY) == "true":
                    print "Paused scan after repetition",str(repetitionNumber),". To resume the scan, press the Start button in the Command Queue view. To abort this scan, press the Skip Task button."
                    LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY,"false")
                    Finder.getInstance().find("commandQueueProcessor").pause(500);
                    ScriptBase.checkForPauses()
                
                #check if the number of repetitions has been altered and we should now end the loop
                numRepsFromProperty = int(LocalProperties.get(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY))
                if numRepsFromProperty != numRepetitions and numRepsFromProperty <= (repetitionNumber):
                    print "The number of repetitions has been reset to",str(numRepsFromProperty), ". As",str(repetitionNumber),"repetitions have been completed this scan will now end."
                    break
                elif numRepsFromProperty <= (repetitionNumber):
                    break
                numRepetitions = numRepsFromProperty
        finally:
#            LocalProperties.set("gda.data.scan.datawriter.dataFormat", originalDataFormat)
            # make sure the plotter is switched off
            jython_mapper.twodplotter.atScanEnd()
            jython_mapper.ionchambers.setOutputLogValues(False) 
            XasAsciiDataWriter.setBeanGroup(None)
            

    def setUpIonChambers(self,beanGroup):
        jython_mapper = JythonNameSpaceMapping()
        # get ion chmabers
        ct = jython_mapper.I1
        ct.setDarkCurrentCollectionTime(beanGroup.getScan().getXesIntegrationTime())

