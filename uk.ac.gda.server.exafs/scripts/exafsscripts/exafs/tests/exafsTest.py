from ch.qos.logback.classic.joran import JoranConfigurator
from gda.util import TestUtils
from gda.configuration.properties import LocalProperties
from gda.device.scannable import PseudoDevice

from org.slf4j import LoggerFactory
from time import sleep
from java.io import ByteArrayInputStream, File
from exafsscripts.exafs.exafsScan import ExafsEnvironment, xasEx, xas, xanesEx
from gda.exafs.scan import BeanGroup, XasScan #@UnresolvedImport
from gda.jython import MockJythonServerFacade, MockJythonServer,\
    InterfaceProvider
import java
import os.path
import unittest
#to run as a Jython Unit Test within an eclipse project ensure the projects PyDev-PYTHONPATH setting contains the folder 
#with the class files or the jar. Also include gda.jar which plus in all other jars via its manifest

LocalProperties=LocalProperties ## silly statement but it does get ride of PyDev Error on LocalProperties method names
TestFileFolder = "scripts/gdascripts/exafs/tests/testfiles/"
class TestScannable(PseudoDevice):
    """Device to allow control and readback of X value"""
    def __init__(self, name):
        PseudoDevice.__init__(self) #@UndefinedVariable
        self.setName(name)
        self.setInputNames([name])
        self.setOutputFormat(["%s"])
        self.X = 0.
        
    def isBusy(self):
        return 0

    def getPosition(self):
        return self.X

    def asynchronousMoveTo(self,new_position):
        sleep(.05)
        self.X = new_position    


class XASTest(unittest.TestCase):
    def idOfTest(self):
        parts = self.id().split(".")
        return "exafsTest.XASTest." + parts[len(parts)-1]
    
    def getActualOutputPath(self):
        return TestUtils.OUTPUT_FOLDER_PREFIX + "scripts/gdascripts/exafs/" + self.idOfTest()

    def getExpectedOutputPath(self):
        return TestFileFolder + self.idOfTest()

    def setUp(self):
        if not os.path.isfile(TestFileFolder+"SampleParameters_Valid.xml"):
            self.fail("Tests must be run with default directory set so that " + TestFileFolder+"SampleParameters_Valid.xml" + " exists")

        outputPath = self.getActualOutputPath()
        TestUtils.makeScratchDirectory(outputPath)

        loggerContext = LoggerFactory.getILoggerFactory();
        joranConfigurator  =  JoranConfigurator();
        loggerContext.shutdownAndReset();
        joranConfigurator.setContext(loggerContext);
        f = '<?xml version="1.0" encoding="UTF-8"?>' + \
        '<configuration>' + \
        '<appender name="DebugFILE" class="ch.qos.logback.core.FileAppender">' + \
        '<File>' +  outputPath +"/log.txt" + '</File>'+ \
        '<layout class="ch.qos.logback.classic.PatternLayout"><pattern>%-5level %logger %ex - %m%n</pattern></layout></appender>' + \
        '<logger name="gda"><level value="INFO"/></logger>' + \
        '<root><level value="ALL"/><appender-ref ref="DebugFILE"/></root></configuration>'
        joranConfigurator.doConfigure(ByteArrayInputStream(java.lang.String(f).getBytes()))

        ExafsEnvironment.testScannable=TestScannable("test")
        ExafsEnvironment.testScriptFolder=TestFileFolder
        mockJythonServerFacade = MockJythonServerFacade()
        mockJythonServer = MockJythonServer()
        InterfaceProvider.setCommandRunnerForTesting(mockJythonServerFacade)
        InterfaceProvider.setCurrentScanControllerForTesting(mockJythonServerFacade)
        InterfaceProvider.setTerminalPrinterForTesting(mockJythonServerFacade)
        InterfaceProvider.setScanStatusHolderForTesting(mockJythonServerFacade)
        InterfaceProvider.setJythonNamespaceForTesting(mockJythonServerFacade)
        InterfaceProvider.setCurrentScanHolderForTesting(mockJythonServer)
        InterfaceProvider.setJythonServerNotiferForTesting(mockJythonServer)
        InterfaceProvider.setDefaultScannableProviderForTesting(mockJythonServer)
        InterfaceProvider.setScanDataPointProviderForTesting(mockJythonServerFacade)
        InterfaceProvider.setAuthorisationHolderForTesting(mockJythonServerFacade)
        LocalProperties.set("gda.data.scan.datawriter.datadir",outputPath+"/Data")
        LocalProperties.set("gda.data.scan.datawriter.dataFormat","NexusDataWriter")
        LocalProperties.set("gda.data.numtracker",outputPath + "/numTracker")

    def tearDown(self):
        pass

    def _testXAS(self):
        xasEx(None,"SampleParameters_Valid.xml", "ScanParameters_Valid.xml","DetectorParameters_withTransmission","OutputParameters",1)

    def _testXASx2(self):
        xas("SampleParameters_Valid.xml", "ScanParameters_Valid.xml","DetectorParameters_withTransmission","OutputParameters",2)

    def _testXASMulti(self):
        xas("multiscan")
        
    def _testAdvanced(self):
        beanGroup = BeanGroup()
        beanGroup.setController(None)
        beanGroup.setScriptFolder(ExafsEnvironment.testScriptFolder)
        beanGroup.setScannable(ExafsEnvironment.testScannable)
        beanGroup.setScan("ScanParameters_Valid.xml")
        beanGroup.setSample("SampleParameters_Valid.xml")
        beanGroup.setDetector("DetectorParameters_withTransmission")
        beanGroup.setOutput("OutputParameters")
        
        xasScan = XasScan(beanGroup)
        cryostatParameters =  xasScan.getSampleParameters().getCryostatParameters()
        for temp in range(100,200,50):
            cryostatParameters.setTemperature(temp)
            xasScan.getSampleParameters().setCryostatParameters(cryostatParameters);
            xasScan.setOutputFileName(`temp`+"K")
            xasScan.runScan(1)

    def _testXANES(self):
        xanesEx(None,"SampleParameters_Valid.xml", "XanesScanParameters.xml","DetectorParameters_withTransmission","OutputParameters",1)
        self.assertEquals( 
           File(self.getExpectedOutputPath()+ "/Data/FeKedge_1.dat"), 
           File(self.getActualOutputPath() + "/Data/ascii/FeKedge_1.dat"));                
        
def suite():
    return unittest.TestLoader().loadTestsFromTestCase(XASTest)      
if __name__ == '__main__':
    unittest.TextTestRunner(verbosity=2).run(suite())