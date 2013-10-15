from BeamlineParameters import JythonNameSpaceMapping
from gda.jython.commands.GeneralCommands import run
from uk.ac.gda.beans import BeansFactory
from org.slf4j import LoggerFactory
from gda.exafs.scan import BeanGroup, BeanGroups

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