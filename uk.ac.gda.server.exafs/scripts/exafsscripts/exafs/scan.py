from org.slf4j import LoggerFactory
from uk.ac.gda.beans import BeansFactory

from gda.configuration.properties import LocalProperties
from gda.exafs.scan import BeanGroup, BeanGroups
from gda.jython.commands.GeneralCommands import run
from gda.jython import InterfaceProvider
from gda.data.scan.datawriter import NexusDataWriter, XasAsciiNexusDataWriter
from gdascripts.metadata.metadata_commands import meta_add,meta_ls,meta_rm,meta_clear
from gdascripts.parameters.beamline_parameters import JythonNameSpaceMapping
from gda.factory import Finder

class Scan:
    
    
    def __init__(self, detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, ExafsScriptObserver, XASLoggingScriptController, energy_scannable, ionchambers, includeSampleNameInNexusName=True):
        self.detectorPreparer = detectorPreparer
        self.samplePreparer = samplePreparer
        self.outputPreparer = outputPreparer
        self.commandQueueProcessor = commandQueueProcessor
        self.ExafsScriptObserver=ExafsScriptObserver
        self.XASLoggingScriptController=XASLoggingScriptController
        self.energy_scannable = energy_scannable
        self.ionchambers=ionchambers
        self.includeSampleNameInNexusName = includeSampleNameInNexusName
        self.sampleFilename= None
        self.scanFilename= None
        self.detectorFilename= None
        self.outputFilename= None
        
    def determineExperimentPath(self, experimentFullPath):
        experimentFullPath = experimentFullPath + "/"
        experimentFolderName = experimentFullPath[experimentFullPath.find("xml")+4:]
        return experimentFullPath, experimentFolderName
    
    def setXmlFileNames(self, sampleFileName, scanFileName, detectorFileName, outputFileName):
        self.sampleFileName= sampleFileName
        self.scanFileName= scanFileName
        self.detectorFileName= detectorFileName
        self.outputFileName= outputFileName
        
    def log(self,*msg):
        self.logger = LoggerFactory.getLogger("exafsscripts.exafs.scan")
        message = ""
        for part in msg:
            message += str(part) + " "
        print message
        self.logger.info(message)

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

    def _createBeans(self, experimentFullPath):
        if(self.sampleFileName == None):
            sampleBean = None
        else:
            sampleBean = BeansFactory.getBeanObject(experimentFullPath, self.sampleFileName)
        scanBean = BeansFactory.getBeanObject(experimentFullPath, self.scanFileName)
        detectorBean = BeansFactory.getBeanObject(experimentFullPath, self.detectorFileName)
        outputBean = BeansFactory.getBeanObject(experimentFullPath, self.outputFileName)
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
        
        if LocalProperties.check(NexusDataWriter.GDA_NEXUS_BEAMLINE_PREFIX):
            nexusFileNameTemplate = nexusSubFolder +"/%d_"+ sampleName+"_"+str(repetition)+".nxs"
            asciiFileNameTemplate = asciiSubFolder +"/%d_"+ sampleName+"_"+str(repetition)+".dat"
        else:
            if self.includeSampleNameInNexusName==True:
                nexusFileNameTemplate = nexusSubFolder +"/"+ sampleName+"_%d_"+str(repetition)+".nxs"
                asciiFileNameTemplate = asciiSubFolder +"/"+ sampleName+"_%d_"+str(repetition)+".dat"
            else:
                nexusFileNameTemplate = nexusSubFolder +"/"+ "%d_"+str(repetition)+".nxs"
                asciiFileNameTemplate = asciiSubFolder +"/"+ sampleName+"_%d_"+str(repetition)+".dat"

        # create XasAsciiNexusDataWriter object and give it the parameters
        dataWriter = XasAsciiNexusDataWriter()
        if (Finder.getInstance().find("metashop") != None):
            meta_add(self.detectorFileName, BeansFactory.getXMLString(detectorBean))
            meta_add(self.outputFileName, BeansFactory.getXMLString(outputBean))
            meta_add(self.sampleFileName, BeansFactory.getXMLString(sampleBean))
            meta_add(self.scanFileName, BeansFactory.getXMLString(scanBean))
            meta_add("xmlFolderName", experimentFullPath)
            xmlFilename = self._determineDetectorFilename(detectorBean)
            if ((xmlFilename != None) and (experimentFullPath != None)):
                detectorConfigurationBean = BeansFactory.getBeanObject(experimentFullPath, xmlFilename)
                meta_add("DetectorConfigurationParameters", BeansFactory.getXMLString(detectorConfigurationBean)) 
        else: 
            self.logger.info("Metashop not found")
            
        dataWriter.setDescriptions(descriptions);
        dataWriter.setNexusFileNameTemplate(nexusFileNameTemplate);
        dataWriter.setAsciiFileNameTemplate(asciiFileNameTemplate);
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
    
    def _getMyVisitID(self):
        return InterfaceProvider.getBatonStateProvider().getBatonHolder().getVisitID()
