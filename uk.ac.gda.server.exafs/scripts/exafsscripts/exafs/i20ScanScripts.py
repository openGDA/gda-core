from b18ScanScripts import XasScan
from BeamlineParameters import JythonNameSpaceMapping, FinderNameMapping
from uk.ac.gda.beans.exafs import XanesScanParameters
from uk.ac.gda.beans.exafs import XesScanParameters
from uk.ac.gda.beans.exafs import XasScanParameters
from uk.ac.gda.beans.exafs.i20 import I20SampleParameters

class I20XasScan(XasScan):
    
    
    def _doLooping(self,beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller):
        #
        # Insert sample environment looping logic here by subclassing
        #
        
        if beanGroup.getDetector().getExperimentType() == 'Fluorescence':
            self.setDetectorCorrectionParameters(beanGroup)
        
        if beanGroup.getSample().getSampleEnvironment() == I20SampleParameters.SAMPLE_ENV[1] :
            
            sampleStageParameters = beanGroup.getSample().getRoomTemperatureParameters()
            numSamples = sampleStageParameters.getNumberOfSamples()
            for i in range(0,numSamples):
                x = sampleStageParameters.getXs()[i]
                y = sampleStageParameters.getYs()[i]
                z = sampleStageParameters.getZs()[i]
                rotation = sampleStageParameters.getRotations()[i]
                roll = sampleStageParameters.getRolls()[i]
                pitch = sampleStageParameters.getPitches()[i]
                print "would now move sample stage to",x,y,z,rotation,roll,pitch
                #TODO add to metadata?
                self._doScan(beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller)
                
                
        else :
            self._doScan(beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller)
        
        
        
        # remove extra columns from ionchambers output
        jython_mapper = JythonNameSpaceMapping()
        jython_mapper.ionchambers.setOutputLogValues(False) 
        

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
 
    
        # get ion chmabers
        ct = jython_mapper.ionchambers
    
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
            print "Setting ionchambers dark current collectiom time to be",str(maxTime),"s"
            ct.setDarkCurrentCollectionTime(maxTime)

class I20XesScan:
    
    def __init__(self, loggingcontroller, detectorPreparer, samplePreparer, outputPreparer, beamlineReverter):
        self.detectorPreparer = detectorPreparer
        self.samplePreparer = samplePreparer
        self.outputPreparer = outputPreparer
        self.loggingcontroller = loggingcontroller
        self.beamlineReverter = beamlineReverter

        
    def __call__ (sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, numRepetitions= 1, validation=True):
    
        # Create the beans from the file names
        xmlFolderName = ExafsEnvironment().getScriptFolder() + folderName + "/"
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
        
        # update to terminal
        print "Starting xes scan..."
        print ""
        print "Output to",xmlFolderName
        print ""

        # give the beans to the xasdatawriter class to help define the folders/filenames 
        beanGroup = BeanGroup()
        beanGroup.setController(Finder.getInstance().find("ExafsScriptObserver"))
        beanGroup.setScriptFolder(xmlFolderName)
        beanGroup.setExperimentFolderName(folderName)
        beanGroup.setScanNumber(numRepetitions)
        beanGroup.setSample(sampleBean)
        beanGroup.setDetector(detectorBean)
        beanGroup.setOutput(outputBean)
        beanGroup.setScan(xesScanBean)

#    XasAsciiDataWriter.setBeanGroup(beanGroup)

        # work out which detectors to use (they will need to have been configured already by the GUI)
        detectorList = getDetectors(beanGroup.getDetector(), beanGroup.getOutput(), beanGroup.getScan()) 
#    print "detectors to be used:", str(detectorList)
    
    # set up the sample 
        setup(beanGroup)
    
    # extract any signal parameters to add to the scan command
    # TODO need to add signal parameters to the qexafs scan, if possible
        signalParameters = getSignalList(outputBean)
        
        #  SHOULD I ADD THESE LINES???
        # run the beamline specific preparers            
        self.detectorPreparer.prepare(beanGroup.getDetector(), beanGroup.getOutput(), xmlFolderName)
        sampleScannables = self.samplePreparer.prepare(beanGroup.getSample())
        outputScannables = self.outputPreparer.prepare(beanGroup.getOutput())

    
    # run the scan
    #if len(signalParameters) > 0:
#        print "signal list:",str(signalParameters)
    
    # get the relevant objects from the namespace
        xes_energy = jython_mapper.XESEnergy
        mono_energy = jython_mapper.test
        analyserAngle = jython_mapper.XESBragg
    #L = jython_mapper.xtal_x
    
        scanType = xesScanBean.getScanType()
        args = []
    
        originalDataFormat = LocalProperties.get("gda.data.scan.datawriter.dataFormat")
    
        from gda.exafs.xes.XesUtils import XesMaterial
        type = 1
        if xesScanBean.getAnalyserType() == str("Si"):
            type = 0
        xes_energy.setMaterialType(type)
        xes_energy.setCut1Val(xesScanBean.getAnalyserCut0())
        xes_energy.setCut2Val(xesScanBean.getAnalyserCut1())
        xes_energy.setCut3Val(xesScanBean.getAnalyserCut2())

        if scanType == XesScanParameters.SCAN_XES_FIXED_MONO:
            print "Scanning the analyser scan with fixed mono"
            print "switching data output format to XesAsciiNexusDataWriter"
            LocalProperties.set("gda.data.scan.datawriter.dataFormat","XesAsciiNexusDataWriter")
            args += [xes_energy, xesScanBean.getXesInitialEnergy(), xesScanBean.getXesFinalEnergy(), xesScanBean.getXesStepSize(), mono_energy, xesScanBean.getMonoEnergy()]
    
        elif scanType == XesScanParameters.SCAN_XES_SCAN_MONO:
            print "Scanning over the analyser and mono energies"
            print "switching data output format to XesAsciiNexusDataWriter"
            LocalProperties.set("gda.data.scan.datawriter.dataFormat","XesAsciiNexusDataWriter")
            args += [mono_energy, xesScanBean.getMonoInitialEnergy(), xesScanBean.getMonoFinalEnergy(), xesScanBean.getMonoStepSize(), xes_energy, xesScanBean.getXesInitialEnergy(), xesScanBean.getXesFinalEnergy(), xesScanBean.getXesStepSize()]

        # create scannable which will control 2D plotting in this mode
            jython_mapper.twodplotter.setX_colName(xes_energy.getInputNames()[0])
            jython_mapper.twodplotter.setY_colName(mono_energy.getInputNames()[0])
            jython_mapper.twodplotter.setZ_colName(finder_mapper.xmapMca.getExtraNames()[0])
        # note that users will have to open a 'plot 1' view or use the XESPlot perspective for this to work

            detectorList = [jython_mapper.twodplotter] + detectorList
    
        elif scanType == XesScanParameters.FIXED_XES_SCAN_XAS:
            print "Doing an EXAFS scan with a fixed analyser energy"
        # add xes_energy, analyserAngle to the defaults and then call the xas command
            xas_scanfilename = xesScanBean.getScanFileName()

            print "moving XES analyser stage to collect at", xesScanBean.getXesEnergy()
            xes_energy(xesScanBean.getXesEnergy())
            add_default(xes_energy)
            add_default(analyserAngle)

            try:
                xas(sampleBean, xas_scanfilename, detectorBean, outputBean, folderName, numRepetitions, validation)
            finally:
                print "cleaning up scan defaults"
                remove_default(xes_energy)
                remove_default(analyserAngle)
            return
    
        elif scanType == XesScanParameters.FIXED_XES_SCAN_XANES:
            print "Doing a XANES scan with a fixed analyser energy"
            # add xes_energy, analyserAngle, to the signal parameters bean and then call the xanes command
            xanes_scanfilename = xesScanBean.getScanFileName()

            xes_energy(xesScanBean.getXesEnergy())
            print "moving XES analyser stage to collect at", xesScanBean.getXesEnergy()
            add_default(xes_energy)
            add_default(analyserAngle)

            try:
                xanes(sampleBean, xanes_scanfilename, detectorBean, outputBean, folderName, numRepetitions, validation)
            finally:
                print "cleaning up scan defaults"
                remove_default(xes_energy)
                remove_default(analyserAngle)
            return
        else:
            raise "scan type in XES Scan Parameters bean/xml not acceptable"
        
    # run the script held in outputparameters
        scriptName = beanGroup.getOutput().getBeforeScriptName()
        if scriptName != None and scriptName != "":
            InterfaceProvider.getCommandRunner().runScript(File(scriptName), None);

        args += [analyserAngle]
        args += detectorList 
        args += [xesScanBean.getXesIntegrationTime()]
        if len(signalParameters) > 0:
            args += signalParameters
#    print args 


    # now that the scan has been defined, run it in a loop

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

            # send out initial messages for logging and display to user
                outputFolder = outputBean.getAsciiDirectory()+ "/" + outputBean.getAsciiFileName()
                logmsg = XasLoggingMessage(scan_unique_id, scriptType, "Starting "+scriptType+" scan...", str(repetitionNumber), str("0%"),str(0),xesScanBean,outputFolder)
                loggingcontroller.update(None,logmsg)
                loggingcontroller.update(None,ScanStartedMessage(xesScanBean,detectorBean)) # informs parts of the UI about current scan
                loggingbean = XasProgressUpdater(loggingcontroller,logmsg)
                args += [loggingbean]
                try:
                    loggingcontroller.update(None, ScriptProgressEvent("Running scan"))
                    ScanBase.interrupted = False
                    if numRepetitions > 1:
                        print ""
                        print "Starting repetition", str(repetitionNumber),"of",numRepetitions
                    thisscan  = ConcurrentScan(args)
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

            # run post scan script
                scriptName = beanGroup.getOutput().getAfterScriptName()
                if scriptName != None and scriptName != "":
                    scriptName = scriptName[scriptName.rfind("/") + 1:]
                    run(scriptName)

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
            LocalProperties.set("gda.data.scan.datawriter.dataFormat", originalDataFormat)

