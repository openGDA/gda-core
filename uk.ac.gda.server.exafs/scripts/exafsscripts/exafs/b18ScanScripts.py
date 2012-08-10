from BeamlineParameters import JythonNameSpaceMapping

from exafsscripts.exafs.setupBeamline import setup,finish

from gda.configuration.properties import LocalProperties
from gda.data import PathConstructor
from gda.data.scan.datawriter import XasAsciiDataWriter, NexusExtraMetadataDataWriter
from gda.exafs.scan import ExafsScanPointCreator, XanesScanPointCreator
from gda.exafs.scan import BeanGroup, BeanGroups
from gda.exafs.scan import ScanStartedMessage
from gda.device.scannable import XasScannable
from gda.device.scannable import XasScannableWithDetectorFramesSetup
from gda.device.scannable import JEPScannable
from gda.exafs.scan import RepetitionsProperties
from gda.factory import Finder
from java.lang import InterruptedException
from gda.jython.commands.GeneralCommands import run
from gda.jython.scriptcontroller.event import ScanCreationEvent, ScanFinishEvent, ScriptProgressEvent
from gda.jython.scriptcontroller.logging import XasProgressUpdater
from gda.jython.scriptcontroller.logging import LoggingScriptController
from gda.jython.scriptcontroller.logging import XasLoggingMessage
from gda.jython import ScriptBase
from gda.scan import ScanBase, ContinuousScan, ConcurrentScan
from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.beans.exafs import XanesScanParameters
from gdascripts.messages.handle_messages import simpleLog
from gda.jython.scriptcontroller.logging import XasProgressUpdater


class Scan:
    
    def __init__(self, loggingcontroller, detectorPreparer, samplePreparer, outputPreparer, beamlineReverter):
        self.detectorPreparer = detectorPreparer
        self.samplePreparer = samplePreparer
        self.outputPreparer = outputPreparer
        self.loggingcontroller = loggingcontroller
        self.beamlineReverter = beamlineReverter
        
    def _createDetArray(self, names, scanBean=None):
        dets = []
        numDets = len(names)
        for i in range(0, numDets):
            name = str(names[i])
            exec("thisDetector = JythonNameSpaceMapping()." + names[i])
            if thisDetector == None:
                raise Exception("detector named " + name + " not found!")
            dets.append(thisDetector)
        return dets

    def _createBeans(self, xmlFolderName, sampleFileName, scanFileName, detectorFileName, outputFileName):
        # Create the beans from the file names
        if(sampleFileName == None):
            sampleBean = None
        else:
            sampleBean = BeansFactory.getBeanObject(xmlFolderName, sampleFileName)
        scanBean = BeansFactory.getBeanObject(xmlFolderName, scanFileName)
        detectorBean = BeansFactory.getBeanObject(xmlFolderName, detectorFileName)
        outputBean = BeansFactory.getBeanObject(xmlFolderName, outputFileName)
        return sampleBean, scanBean, detectorBean, outputBean
    
    def _runScript(self, scriptName):
        if scriptName != None and scriptName != "":
            scriptName = scriptName[scriptName.rfind("/") + 1:]
            run(scriptName)

    
class XasScan(Scan):
    
    def _defineBeanGroup(self, folderName, validation, controller, xmlFolderName, sampleBean, scanBean, detectorBean, outputBean):

        originalGroup = BeanGroup()
        originalGroup.setController(controller)
        originalGroup.setScriptFolder(xmlFolderName)
        originalGroup.setScannable(Finder.getInstance().find(scanBean.getScannableName())) #TODO
        originalGroup.setExperimentFolderName(folderName)
        if (sampleBean != None):
            originalGroup.setSample(sampleBean)
        originalGroup.setDetector(detectorBean)
        outputBean.setAsciiFileName(sampleBean.getName())
        originalGroup.setOutput(outputBean)
        originalGroup.setValidate(validation)
        originalGroup.setScan(scanBean)
        return originalGroup
    
    def _configureScannable(self,beanGroup):
        
        xas_scannable = XasScannable()
        xas_scannable.setName("xas_scannable")
        xas_scannable.setEnergyScannable(Finder.getInstance().find(beanGroup.getScan().getScannableName()))
        return xas_scannable
        

    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, numRepetitions= 1, validation=True):
        ScanBase.interrupted = False
        controller = Finder.getInstance().find("ExafsScriptObserver")
        
        # Create the beans from the file names
        xmlFolderName = ExafsEnvironment().getXMLFolder() + folderName + "/"
        sampleBean, scanBean, detectorBean, outputBean = self._createBeans(xmlFolderName, sampleFileName, scanFileName, detectorFileName, outputFileName)

        # create unique ID for this scan (all repetitions will share the same ID)
        scriptType = "Exafs"
        if isinstance(scanBean, XanesScanParameters):
            scriptType = "Xanes"
        scan_unique_id = LoggingScriptController.createUniqueID(scriptType);
        
        # update to terminal
        print "Starting",scriptType,detectorBean.getExperimentType(),"scan over scannable '"+scanBean.getScannableName()+"'..."
        print ""
        print "Output to",xmlFolderName
        print ""

        # give the beans to the xasdatawriter class to help define the folders/filenames 
        beanGroup = self._defineBeanGroup(folderName, validation, controller, xmlFolderName, sampleBean, scanBean, detectorBean, outputBean)
        
        self._doLooping(beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller)

    def _doLooping(self,beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller):
        #
        # Insert sample environment looping logic here by subclassing
        #
        self._doScan(beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller)
        
    def _doScan(self,beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller):
        
        # reset the properties used to control repetition behaviour
        LocalProperties.set(RepetitionsProperties.HALT_AFTER_REP_PROPERTY,"false")
        LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
        LocalProperties.set(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY,str(numRepetitions))
        repetitionNumber = 0

        try:
            while True:
                repetitionNumber+= 1
                beanGroup.setScanNumber(repetitionNumber)
                XasAsciiDataWriter.setBeanGroup(beanGroup)
        
                # create the list of scan points
                points = ()
                if isinstance(beanGroup.getScan(), XanesScanParameters):
                    points = XanesScanPointCreator.calculateEnergies(beanGroup.getScan())
                else:
                    points = ExafsScanPointCreator.calculateEnergies(beanGroup.getScan())
                    
                # create the scannable to control energy and time
                xas_scannable = self._configureScannable(beanGroup)
    
                # send out initial messages for logging and display to user
                outputFolder = beanGroup.getOutput().getAsciiDirectory()+ "/" + beanGroup.getOutput().getAsciiFileName()
                logmsg = XasLoggingMessage(scan_unique_id, scriptType, "Starting "+scriptType+" scan...", str(repetitionNumber + 1), str("0%"),str(0),beanGroup.getScan(),outputFolder)
                self.loggingcontroller.update(None,logmsg)
                self.loggingcontroller.update(None,ScanStartedMessage(beanGroup.getScan(),beanGroup.getDetector())) # informs parts of the UI about current scan
                loggingbean = XasProgressUpdater(self.loggingcontroller,logmsg)
    
                # work out which detectors to use (they will need to have been configured already by the GUI)
                detectorList = self._getDetectors(beanGroup.getDetector(), beanGroup.getScan()) 
                xas_scannable.setDetectors(detectorList)
                
                # work out extra scannables to include
                signalParameters = self._getSignalList(beanGroup.getOutput())
                
                # run the beamline specific preparers            
                self.detectorPreparer.prepare(beanGroup.getDetector(), beanGroup.getOutput(), xmlFolderName)
                sampleScannables = self.samplePreparer.prepare(beanGroup.getSample())
                outputScannables = self.outputPreparer.prepare(beanGroup.getOutput())
               
                # run the before scan script
                self._runScript(beanGroup.getOutput().getBeforeScriptName())
     
                # build the scan command arguments
                args = [xas_scannable, points]
                if sampleScannables != None:
                    args += sampleScannables
                if outputScannables != None:
                    args += outputScannables
                args += detectorList 
                args += signalParameters
                args += [loggingbean]
                
                # run the scan
                try:
                    controller.update(None, ScriptProgressEvent("Running scan"))
                    ScanBase.interrupted = False
                    if numRepetitions > 1:
                        print ""
                        print "Starting repetition", str(repetitionNumber),"of",numRepetitions
                    thisscan = ConcurrentScan(args)
                    controller.update(None, ScanCreationEvent(thisscan.getName()))
                    thisscan.runScan()
                except InterruptedException, e:
                    if LocalProperties.get(RepetitionsProperties.SKIP_REPETITION_PROPERTY) == "true":
                        LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
                        ScanBase.interrupted = False
                        # check if a panic stop has been issued, so the whole script should stop
                        if ScriptBase.isInterrupted():
                            raise e
                        # only wanted to skip this repetition, so absorb the exception and continue the loop
                        if numRepetitions > 1:
                            print "Repetition", str(repetitionNumber),"skipped."
                    else:
                        print e
                        raise # any other exception we are not expecting so raise whatever this is to abort the script
                        
                #update observers
                controller.update(None, ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK));
                    
                # run the after scan script
                self._runScript(beanGroup.getOutput().getAfterScriptName())
                
                #check if halt after current repetition set to true
                if numRepetitions > 1 and LocalProperties.get(RepetitionsProperties.HALT_AFTER_REP_PROPERTY) == "true":
                    print "The repetition loop was requested to be halted, so this scan has ended after", str(repetitionNumber), "repetition(s)."
                    break
                
                #check if the number of repetitions has been altered and we should now end the loop
                numRepsFromProperty = int(LocalProperties.get(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY))
                if numRepsFromProperty != numRepetitions and numRepsFromProperty <= (repetitionNumber):
                    print "The number of repetitions has been reset to",str(numRepsFromProperty), ". As",str(repetitionNumber),"repetitions have been completed this scan will now end."
                    break
                elif numRepsFromProperty <= (repetitionNumber):
                    break
                    
        finally:    
            # repetition loop completed, so reset things
            if (self.beamlineReverter != None):
                self.beamlineReverter.scanCompleted() #NexusExtraMetadataDataWriter.removeAllMetadataEntries() for I20
            LocalProperties.set("gda.scan.useScanPlotSettings", "false")
            LocalProperties.set("gda.plot.ScanPlotSettings.fromUserList", "false")
            
            #remove added metadata from default metadata list to avoid multiple instances of the same metadata
            jython_mapper = JythonNameSpaceMapping()
            if (jython_mapper.original_header != None):
                original_header=jython_mapper.original_header[:]
                Finder.getInstance().find("datawriterconfig").setHeader(original_header)
    
    def _getSignalList(self, outputParameters):
        signalList = []
        for signal in outputParameters.getCheckedSignalList():
             scannable = JEPScannable.createJEPScannable(signal.getLabel(), signal.getScannableName(), signal.getDataFormat(), signal.getName(), signal.getExpression())
             signalList.append(scannable)
        return signalList
            
    def _getDetectors(self, detectorBean, scanBean):
        expt_type = detectorBean.getExperimentType()
        detectorList = []
        if expt_type == "Transmission":
            for group in detectorBean.getDetectorGroups():
                if group.getName() == detectorBean.getTransmissionParameters().getDetectorType():
                    return self._createDetArray(group.getDetector(), scanBean)
        elif expt_type == "Soft X-Rays":
            for group in detectorBean.getDetectorGroups():
                if group.getName() == detectorBean.getSoftXRaysParameters().getDetectorType():
                    return self._createDetArray(group.getDetector(), scanBean)    
        elif expt_type == "XES":
            for group in detectorBean.getDetectorGroups():
                if group.getName() == "XES":
                    # TO DO also need to include the I1 detector and create a new I1/FF detector object
                    return self._createDetArray(group.getDetector(), scanBean)
        else:
            for group in detectorBean.getDetectorGroups():
                if group.getName() == detectorBean.getFluorescenceParameters().getDetectorType():
                    #print detectorBean.getFluorescenceParameters().getDetectorType(), "experiment"
                    return self._createDetArray(group.getDetector(), scanBean)

   
class QexafsScan(Scan):
    
    def __init__(self,loggingcontroller, detectorPreparer, samplePreparer, outputPreparer, energy_scannable, ion_chambers_scannable):
        Scan.__init__(self, loggingcontroller,detectorPreparer, samplePreparer, outputPreparer,None)
        self.energy_scannable = energy_scannable
        self.ion_chambers_scannable = ion_chambers_scannable
        #self.cirrus = cirrus
        #self.sample_temperature = sample_temperature
        #self.sample_temperature2 = sample_temperature2
            
    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, numRepetitions= -1, validation=True):
        xmlFolderName = ExafsEnvironment().getXMLFolder() + folderName + "/"

        sampleBean, scanBean, detectorBean, outputBean = self._createBeans(xmlFolderName, sampleFileName, scanFileName, detectorFileName, outputFileName) 
        controller = Finder.getInstance().find("ExafsScriptObserver")
        outputBean.setAsciiFileName(sampleBean.getName())
        beanGroup = self._createBeanGroup(controller, xmlFolderName, folderName, sampleBean, detectorBean, outputBean, scanBean)
        # work out which detectors to use (they will need to have been configured already by the GUI)
        detectorList = self._getQEXAFSDetectors(detectorBean, outputBean, scanBean) 
        print "detectors to be used:", str(detectorList)
        
        
        # send initial message to the log
        from gda.jython.scriptcontroller.logging import LoggingScriptController
        from gda.jython.scriptcontroller.logging import XasLoggingMessage
        loggingcontroller   = Finder.getInstance().find("XASLoggingScriptController")
        scriptType = "Qexafs"
        unique_id  = LoggingScriptController.createUniqueID(scriptType);
        outputFolder = outputBean.getAsciiDirectory()+ "/" + outputBean.getAsciiFileName()

        #print "output folder=", outputFolder
        
                # update to terminal
        print "Starting Qexafs scan..."
        print ""
        print "Output to",xmlFolderName
        print ""

        
        # reset the properties used to control repetition behaviour
        LocalProperties.set(RepetitionsProperties.HALT_AFTER_REP_PROPERTY,"false")
        LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
        LocalProperties.set(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY,str(numRepetitions))
        repetitionNumber = 0
        
        try:
            while True:
                repetitionNumber+= 1

                # set up the sample 
                self.detectorPreparer.prepare(detectorBean, outputBean, xmlFolderName)
                self.samplePreparer.prepare(sampleBean)
                self.outputPreparer.prepare(outputBean)
                
                beanGroup.setScanNumber(repetitionNumber)
                XasAsciiDataWriter.setBeanGroup(beanGroup)
        
                # no signal parameters
                if len(outputBean.getCheckedSignalList()) > 0:
                    print "Signal parameters not available with QEXAFS"
                if self.energy_scannable == None:
                    raise "No object for controlling energy during QEXAFS found! Expected qexafs_energy (or scannable1 for testing)"
            
                initial_energy = scanBean.getInitialEnergy()
                final_energy = scanBean.getFinalEnergy()
                step_size = scanBean.getStepSize()
                import math
                numberPoints = int(math.ceil((final_energy-initial_energy)/step_size))
            
                self._runScript(outputBean.getBeforeScriptName())
            
                scan_time = scanBean.getTime()
                
                # send out initial messages
                logmsg = XasLoggingMessage(unique_id, scriptType, "Starting "+scriptType+" scan...", str(repetitionNumber), str("0%"),str(0),str(scanBean.getTime()) + "s",outputFolder)
                loggingcontroller.update(None,logmsg)
                loggingcontroller.update(None,ScanStartedMessage(scanBean,detectorBean))
                loggingbean = XasProgressUpdater(loggingcontroller,logmsg)
                # difference to step scan is that the loggingbean cannot simply be added to the scan, so instead we have to manually call 
                #atScanStart, atScanEnd and atCommandFailure in this script
    
            
                print "running QEXAFS scan:", self.energy_scannable.getName(), scanBean.getInitialEnergy(), scanBean.getFinalEnergy(), numberPoints, scan_time, detectorList
                controller.update(None, ScriptProgressEvent("Running QEXAFS scan"))
                thisscan = ContinuousScan(self.energy_scannable , scanBean.getInitialEnergy(), scanBean.getFinalEnergy(), numberPoints, scan_time, detectorList)
                controller.update(None, ScanCreationEvent(thisscan.getName()))
                try:
                    if numRepetitions > 1:
                        print ""
                        print "Starting repetition", str(repetitionNumber),"of",numRepetitions
                    loggingbean.atScanStart()
                    thisscan.runScan()  
                    controller.update(None, ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK));
                    loggingbean.atScanEnd()            
                except InterruptedException, e:
                    loggingbean.atCommandFailure()
                    if LocalProperties.get(RepetitionsProperties.SKIP_REPETITION_PROPERTY) == "true":
                        LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
                        ScanBase.interrupted = False
                        # check if a panic stop has been issued, so the whole script should stop
                        if ScriptBase.isInterrupted():
                            raise e
                        # only wanted to skip this repetition, so absorb the exception and continue the loop
                        if numRepetitions > 1:
                            print "Repetition", str(repetitionNumber),"skipped."
                    else:
                        print e
                        raise # any other exception we are not expecting so raise whatever this is to abort the script
                except:
                    loggingbean.atCommandFailure()
                    raise
                    
                # run the after scan script
                self._runScript(outputBean.getAfterScriptName())
                
                #check if halt after current repetition set to true
                if numRepetitions > 1 and LocalProperties.get(RepetitionsProperties.HALT_AFTER_REP_PROPERTY) == "true":
                    print "The repetition loop was requested to be halted, so this scan has ended after", str(repetitionNumber), "repetition(s)."
                    break
                
                #check if the number of repetitions has been altered and we should now end the loop
                numRepsFromProperty = int(LocalProperties.get(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY))
                if numRepsFromProperty != numRepetitions and numRepsFromProperty <= (repetitionNumber):
                    print "The number of repetitions has been reset to",str(numRepsFromProperty), ". As",str(repetitionNumber),"repetitions have been completed this scan will now end."
                    break
                elif numRepsFromProperty <= (repetitionNumber):
                    break
       
        finally:    
            # repetition loop completed, so reset things
            if (self.beamlineReverter != None):
                self.beamlineReverter.scanCompleted() #NexusExtraMetadataDataWriter.removeAllMetadataEntries() for I20
            LocalProperties.set("gda.scan.useScanPlotSettings", "false")
            LocalProperties.set("gda.plot.ScanPlotSettings.fromUserList", "false")
            
            #remove added metadata from default metadata list to avoid multiple instances of the same metadata
            if (jython_mapper.original_header != None):
                jython_mapper = JythonNameSpaceMapping()
                original_header=jython_mapper.original_header[:]
                Finder.getInstance().find("datawriterconfig").setHeader(original_header)

 
    def _getNumberOfFrames(self, detectorBean, scanBean):
         # work out the number of frames to collect
        numberPoints = 0
        if detectorBean.getExperimentType() == "Fluorescence" and detectorBean.getFluorescenceParameters().getDetectorType() == "Germanium":
            maxPoints = self.ion_chambers_scannable.maximumReadFrames()
            if scanBean.isChooseNumberPoints():
                return maxPoints
            else:
                numberPoints = scanBean.getNumberPoints()
                if numberPoints > maxPoints:
                    raise "Too many frames for the given detector configuration."
                return numberPoints
        # for ion chambers only use a default value
        elif scanBean.isChooseNumberPoints():
            print "using default number of frames: 1000"
            return 1000 # a default value as user has selected max possible but is only using the ion chambers (so max will be very high)
        # have a limit anyway of 4096
        else:
            numberPoints = scanBean.getNumberPoints()
            if numberPoints > 4096:
                    raise "Too many frames for the given detector configuration."
            return numberPoints
 
    def _getQEXAFSDetectors(self, detectorBean, outputBean, scanBean):
        expt_type = detectorBean.getExperimentType()
        detectorList = []
        if expt_type == "Transmission":
            return self._createDetArray(["qexafs_counterTimer01"], scanBean)
        else:
            if detectorBean.getFluorescenceParameters().getDetectorType() == "Silicon":
                return self._createDetArray(["qexafs_counterTimer01", "qexafs_xmap", "VortexQexafsFFI0"], scanBean)
            else:
                return self._createDetArray(["qexafs_counterTimer01", "qexafs_xspress", "QexafsFFI0"], scanBean)
        
        

    def _createBeanGroup(self, controller, xmlFolderName, folderName, sampleBean, detectorBean, outputBean, scanBean):
        # give the beans to the xasdatawriter class to help define the folders/filenames 
        beanGroup = BeanGroup()
        beanGroup.setController(controller)
        beanGroup.setScriptFolder(xmlFolderName)
        beanGroup.setExperimentFolderName(folderName)
        #beanGroup.setScanNumber(scanNumber)
        beanGroup.setSample(sampleBean)
        beanGroup.setDetector(detectorBean)
        beanGroup.setOutput(outputBean)
        beanGroup.setScan(scanBean)
        return beanGroup
    #===========================================================================
    # def _control_cirrus_detector(self, bean, initial_energy, final_energy, scan_time):
    #    
    #    masses = []
    #    masses.append(bean.isMass2())
    #    masses.append(bean.isMass4())
    #    masses.append(bean.isMass12())
    #    masses.append(bean.isMass14())
    #    masses.append(bean.isMass15())
    #    masses.append(bean.isMass16())
    #    masses.append(bean.isMass17())
    #    masses.append(bean.isMass18())
    #    masses.append(bean.isMass28())
    #    masses.append(bean.isMass32())
    #    masses.append(bean.isMass36())
    #    masses.append(bean.isMass40())
    #    masses.append(bean.isMass44())
    #    masses.append(bean.isMass64())
    #    masses.append(bean.isMass69())
    #    
    #    self.initial_energy=initial_energy
    #    self.final_energy=final_energy
    #    self.scan_time = scan_time
    #    
    #    i=0
    #    massList = []
    #    mList= [2,4,12,14,15,16,17,18,28,32,36,40,44,64,69]
    #    for mass in masses:
    #        if mass is True:
    #            massList.append(mList[i])
    #        i+=1
    #    if len(massList)>0:
    #        print str(massList)
    #        self.cirrus.setMasses(massList)
    #    else:
    #        massList=self.cirrus.getMasses()
    #    
    #    print "Number of cirrus reads=" + str(int(self.scan_time/bean.getInterval()))
    #    
    #    mythread = CollectCirrusData(int(self.scan_time/bean.getInterval()), bean.getInterval(), PathConstructor.createFromProperty("gda.device.cirrus.datadir"), self.cirrus, self.sample_temperature, self.sample_temperature2, self.energy_scannable, self.initial_energy, self.final_energy, massList)
    #    mythread.start()
    #===========================================================================

                
class ExafsEnvironment:
    testScriptFolder = None
    
    def getXMLFolder(self):
        if ExafsEnvironment.testScriptFolder != None:
            return ExafsEnvironment.testScriptFolder
        dataDirectory = PathConstructor.createFromDefaultProperty()
        return dataDirectory + "/xml/"

    testScannable = None
    
    def getScannable(self):
        if ExafsEnvironment.testScannable != None:
            return ExafsEnvironment.testScannable
        # The scannable name is defined in the XML when not in testing mode.
        # Therefore the scannable argument is omitted from the bean
        return None

from threading import Thread
from gda.data import PathConstructor;
import time

#===============================================================================
# class CollectCirrusData(Thread):
#        
#    def __init__(self, itterations, interval, filename, cirrus, sample_temperature, sample_temperature2, energy_scannable, initial_energy, final_energy, mList):
#        Thread.__init__(self)
#        self.itterations=itterations
#        self.interval=interval
#        from gda.data import NumTracker;
#        numTracker = NumTracker("tmp")
#        thisFileNumber = numTracker.getCurrentFileNumber()+1;
#        print filename + "/" + str(thisFileNumber) + "_cirrus.txt"
#        self.filename=filename + str(thisFileNumber) + "_cirrus.txt"
#        self.cirrus=cirrus
#        self.sample_temperature = sample_temperature
#        self.sample_temperature2 = sample_temperature2
#        self.initial_energy=initial_energy
#        self.final_energy=final_energy
#        self.energy_scannable=energy_scannable
#        self.mList = mList
#        
#    def run(self):
#        timeout = 20
#        t=0
#        while int(self.energy_scannable()) not in range(self.initial_energy-10, self.initial_energy+10):
#            t+=1
#            if t==timeout:
#                break
#        
#        timeCounter=0
#        
#        f = open(self.filename, 'w')
#        f.write("time    ")
#        for m in self.mList:
#            f.write(str(m) + "    ")
#        f.write("temperature")
#        f.write("\n")
#        
#        for itt in range(self.itterations):
#            print "writing cirrus data"
#            self.cirrus.collectData()
#            data = self.cirrus.readout().toString().split('\t')
#            f.write(str(timeCounter)+"    ")
#            for d in data:
#                f.write(str(d)+"    ")
#            f.write(str(self.sample_temperature()) + "\n")
#            f.write(str(self.sample_temperature2()) + "\n")
#            timeCounter+=self.interval
#            time.sleep(self.interval)
#            
#        f.close()    
#===============================================================================
