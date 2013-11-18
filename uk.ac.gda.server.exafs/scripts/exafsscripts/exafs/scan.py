from org.slf4j import LoggerFactory
from uk.ac.gda.beans import BeansFactory

from gda.configuration.properties import LocalProperties
from gda.exafs.scan import BeanGroup, BeanGroups
from gda.jython.commands.GeneralCommands import run
from gda.data.scan.datawriter import NexusDataWriter, XasAsciiNexusDataWriter

from gdascripts.parameters.beamline_parameters import JythonNameSpaceMapping

class Scan:
    
    def __init__(self, detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, ExafsScriptObserver, XASLoggingScriptController, datawriterconfig, original_header, energy_scannable, ionchambers):
        self.detectorPreparer = detectorPreparer
        self.samplePreparer = samplePreparer
        self.outputPreparer = outputPreparer
        self.commandQueueProcessor = commandQueueProcessor
        self.ExafsScriptObserver=ExafsScriptObserver
        self.XASLoggingScriptController=XASLoggingScriptController
        self.datawriterconfig=datawriterconfig
        self.original_header = original_header
        self.energy_scannable = energy_scannable
        self.ionchambers=ionchambers
        
    def determineExperimentPath(self, experimentFullPath):
        experimentFullPath = experimentFullPath + "/"
        experimentFolderName = experimentFullPath[experimentFullPath.find("xml")+4:]
        return experimentFullPath, experimentFolderName
        
    def log(self,*msg):
        self.logger = LoggerFactory.getLogger("exafsscripts.exafs.scan")
        message = ""
        for part in msg:
            message += str(part) + " "
        print message
        self.logger.info(message)
        
    def _resetHeader(self):
        self.datawriterconfig.setHeader(self.original_header)

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

    def _createBeans(self, experimentFullPath, sampleFileName, scanFileName, detectorFileName, outputFileName):
        if(sampleFileName == None):
            sampleBean = None
        else:
            sampleBean = BeansFactory.getBeanObject(experimentFullPath, sampleFileName)
        scanBean = BeansFactory.getBeanObject(experimentFullPath, scanFileName)
        detectorBean = BeansFactory.getBeanObject(experimentFullPath, detectorFileName)
        outputBean = BeansFactory.getBeanObject(experimentFullPath, outputFileName)
        return sampleBean, scanBean, detectorBean, outputBean
    
    # from xas
    def _createBeanGroup(self, folderName, validation, controller, experimentFullPath, sampleBean, scanBean, detectorBean, outputBean):
        beanGroup = BeanGroup()
        beanGroup.setController(controller)
        beanGroup.setXmlFolder(experimentFullPath)
        beanGroup.setExperimentFolderName(folderName)
        outputBean.setAsciiFileName(sampleBean.getName())
        beanGroup.setValidate(validation)
        if (sampleBean != None):
            beanGroup.setSample(sampleBean)
        beanGroup.setDetector(detectorBean)
        beanGroup.setOutput(outputBean)
        beanGroup.setScan(scanBean)
        
        return beanGroup
    
    def _runScript(self, scriptName):
        if scriptName != None and scriptName != "":
            scriptName = scriptName[scriptName.rfind("scripts/") + 8:]
            run(scriptName)
            
    """
    Get the relevant datawriter config, create a datawriter and if it of the correct type then give it the config.
    Give the datawriter to the scan.
    """
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
        dataWriter.setXmlFileName(self._determineDetectorFilename(detectorBean))
        dataWriter.setDescriptions(descriptions);
        dataWriter.setNexusFileNameTemplate(nexusFileNameTemplate);
        dataWriter.setAsciiFileNameTemplate(asciiFileNameTemplate);
        # get the ascii file format configuration (if not set here then will get it from the Finder inside the Java class
        asciidatawriterconfig = self.outputPreparer.getAsciiDataWriterConfig(scanBean)
        if asciidatawriterconfig != None :
            dataWriter.setConfiguration(asciidatawriterconfig)
        thisscan.setDataWriter(dataWriter)
        return thisscan

    def _determineDetectorFilename(self,detectorBean):
        xmlFileName = None
        if detectorBean.getExperimentType() == "Fluorescence" :
            fluoresenceParameters = detectorBean.getFluorescenceParameters()
            xmlFileName = fluoresenceParameters.getConfigFileName()
        elif detectorBean.getExperimentType() == "XES" :
            fluoresenceParameters = detectorBean.getXesParameters()
            xmlFileName = fluoresenceParameters.getConfigFileName()
        return xmlFileName
