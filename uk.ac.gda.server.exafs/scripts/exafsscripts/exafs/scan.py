from BeamlineParameters import JythonNameSpaceMapping
from gda.jython.commands.GeneralCommands import run
from uk.ac.gda.beans import BeansFactory
from org.slf4j import LoggerFactory

class Scan:
    
    def __init__(self, loggingcontroller, detectorPreparer, samplePreparer, outputPreparer, beamlineReverter):
        self.detectorPreparer = detectorPreparer
        self.samplePreparer = samplePreparer
        self.outputPreparer = outputPreparer
        self.loggingcontroller = loggingcontroller
        self.beamlineReverter = beamlineReverter
        
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

    def _createBeans(self, xmlFolderName, sampleFileName, scanFileName, detectorFileName, outputFileName):
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