from gda.device.scannable import ScannableBase
from org.slf4j import LoggerFactory

from gda.device.detector.countertimer import TfgScaler, TfgScalerWithFrames, TfgScalerWithDarkCurrent
from java.lang import Exception
import math
import datetime
from gda.jython import InterfaceProvider
from __builtin__ import False
from uk.ac.gda.server.exafs.scan import DetectorPreparer

print("\nRunning 'ionchamber-checker.py")

""" Scannable to be used as base that can be used in a scan but doesn't produce any output data.
    Override atScanStart atScanEnd etc with suitable implementations. atScanEnd is called if an exception is thrown during the scan
"""
class DefaultScannable(ScannableBase):
 
    def __init__(self, name):
        self.name = name
        self.inputNames = [name]
        self.setOutputFormat({});
        self.setInputNames({});
 
    def stop(self):
        self.atScanEnd()
 
    def atCommandFailure(self):
        self.atScanEnd()
 
    def isBusy(self):
        return False
 
    def rawAsynchronousMoveTo(self,new_position):
        pass
 
    def rawGetPosition(self):
        return None

class TestIonchamberReadout(DefaultScannable):
    def __init__(self, name):
        super(TestIonchamberReadout, self).__init__(name)
        self.logger = LoggerFactory.getLogger("TestIonchamberReadout")
        self.tfg = None
        self.timePerPoint = 1.0
        self.timeForFirstPoint = 0.5
        self.numPoints = 5
        self.maxNumDeviations = 3;
        self.dataName = "I0" # name of the data to be extracted (needs to match one of the values returned by tfg.getExtraNames())
        self.testResultString = "Not run yet"
        self.timeLastRun = "Not run yet"
        self.setExtraNames(["status"])
        self.fixIonchamberFunction = None
        
    def setTfg(self, tfg) :
        self.tfg = tfg

    def setNumPoints(self, numPoints):
        self.numPoints = numPoints
        
    def getNumPoints(self):
        return self.numPoints

    def setCollectionTime(self, collectionTime):
        self.timePerPoint = collectionTime
    
    def setFirstPointCollectionTime(self, firstPointCollectionTime):
        self.timeForFirstPoint = firstPointCollectionTime
    
    def setfixIonchamberFunction(self, function) :
        self.fixIonchamberFunction = function
        
    def atScanStart(self):
        self.runCheckAndTryToFix()
    
    def runCheckAndTryToFix(self):
        stateOk = self.runCheck()
        if not stateOk and self.fixIonchamberFunction != None :
            firstTestResult = self.testResultString
            funcName = self.fixIonchamberFunction.__name__
            self.printAndLog("Ionchamber state was not ok - running "+str(funcName)+" to try and fix the problem")
            self.fixIonchamberFunction()
            self.printAndLog("Running ion chamber check again...")
            self.runCheck()
            secondTestResult = self.testResultString
            self.testResultString = "Test result 1 : "+firstTestResult + ". Reconnected to DAServer. Test result 2 : "+secondTestResult
            

    def runCheck(self):
        """
        Run data consistency check.
        1) Run 'collectData' to collect frames of data from ionchambers
        2) Extract values for one of the scaler channels (e.g. I0, It, It - according to value of 'dataName')
        3) Run checkData function on the extracted data
        """
        self.printAndLog("Running ion chamber checker...")
        data = self.collectData()
        dataIndex = self.tfg.getExtraNames().index(self.dataName)
        if dataIndex == -1 :
            raise Exception("Unable to find data called "+self.dataName+" in output from "+self.tfg.getName())
    
        self.printAndLog("Extracting %s data from collected values"%(self.dataName))
        dataToCheck = []
        for d in data :
            dataToCheck.append(d[dataIndex])
        
        self.timeLastRun = self.getTimeDateString()
        stateOk = self.checkData(dataToCheck)
        self.testResultString = self.appendTimeString(self.testResultString, self.timeLastRun)
        return stateOk
    
    
    def collectData(self):
        """ Collect 'numPoints' frames of data on tfg. Frames have duration = timePerPoint apart
        from first point which has length = timeForFirstPoint.
        Returns an array of all collected frames from ionchambers (i.e. result returned by tfg.readout() method for each frame)
        """
        self.printAndLog("Collecting %d frames of data from %s"%(self.numPoints, self.tfg.getName()))

        tfgHasDarkCurrent = isinstance(self.tfg, TfgScalerWithDarkCurrent)
        tfgHasFrames = isinstance(self.tfg, TfgScalerWithFrames) 
        
        ## clear out any old settings
        if tfgHasFrames :
            self.tfg.clearFrameSets()
            
        ## Store the currently set value of darkCurrentRequired
        measureDarkCurrent = False
        if tfgHasDarkCurrent :
            measureDarkCurrent = self.tfg.isDarkCurrentRequired()
            self.tfg.setDarkCurrentRequired(False)
            
        ## Collect the frames of data
        data = []
        for count in range(self.numPoints) :
            t = self.timePerPoint
            if count == 0 :
                t = self.timeForFirstPoint
                
            self.tfg.setCollectionTime(t)
            sleep(0.1)
            self.tfg.collectData()
            sleep(t+0.1)
            readoutData = self.tfg.readout();
            self.printAndLog("%f secs : %s"%(t, str(readoutData)))
            data.append(readoutData)
                
             
        ## Restore original 'darkCurrentRequired' value  
        if tfgHasDarkCurrent:
            self.tfg.setDarkCurrentRequired(measureDarkCurrent)

        return data
            
    def checkData(self, data):
        """
        Check that values in the passed data array are 'consistent' :
        1) Compute average and std deviation using all values in the array the first value
        2) Compute 'expected' first data value - by scaling the avg value by ratio of 'timeForFirstPoint' to 'timePerPoint'
        3) Data is 'consistent' if the actual first value is within maxNumDeviations of the 'expected' first value.
        """
        subset = data[1:]
        stdDev = max(self.computeStdDev(subset), 1.0)
        maxAllowedDiff = stdDev*self.maxNumDeviations
        avg = sum(subset)/len(subset)
        expectedValue = avg*self.timeForFirstPoint/self.timePerPoint

        self.printAndLog("Average : %f , sigma : %s, max allowed diff : %f, expected first value : %f"%(avg, stdDev, maxAllowedDiff, expectedValue))
        if math.fabs(data[0] - expectedValue) > maxAllowedDiff :
            self.testResultString = "Possible problem - first data value (%f) is not within tolerance of expected value (%f)"%(data[0], expectedValue)
            self.printToTerminal(self.testResultString)
            self.logger.warn(self.testResultString)
            return False
        else :
            self.testResultString = "Data seems to be ok"
            self.printAndLog(self.testResultString)
            return True
            
    def getTimeDateString(self):
        return datetime.datetime.now().strftime("%H:%M:%S on %d-%m-%Y")
    
    def appendTimeString(self, message, timestring) :
        return message + " (check run at "+timestring+")"
    
     # Compute standard deviation of a list of numbers
    def computeStdDev(self, data):
        avg = sum(data)/len(data)
        sumSq = 0
        for val in data :
            sumSq += math.pow(val - avg, 2)
        
        return math.sqrt(sumSq/len(data))

    def printToTerminal(self, message):
        tp = InterfaceProvider.getTerminalPrinter()
        tp.print(message)

    def printAndLog(self, message) :
        self.printToTerminal(message)
        self.logger.info(message)
    
    # This is used in Ascii header
    def getPosition(self):
        return self.testResultString

## Create new AsciiMetadataConfig object
def createAsciiMetaDataEntry(label, values):
    from gda.data.scan.datawriter import AsciiMetadataConfig
    newEnt = AsciiMetadataConfig()  
    newEnt.setLabel(label)
    newEnt.setLabelValues(values)
    return newEnt
    
def addMetaDataEntry(asciiConfig, metadataConfig):
    ## Remove any previously added entry for 'ion chamber' like config
    removeMetaDataEntry(asciiConfig, "ion chamber")
    ## Add the new/updated entry
    print("Adding %s to %s"%(metadataConfig.getLabel(), asciiConfig.getName()))
    asciiConfig.getHeader().add(metadataConfig)
    
## Remove item from header of Ascii metadata config 
# (i.e. entry in config with label partially matching given label string)
def removeMetaDataEntry(asciiConfig, label):
    ind = -1
    headerList = asciiConfig.getHeader()
    for header in headerList : 
        if label.upper() in header.getLabel().upper() :
            ind = headerList.indexOf(header)
    if ind > -1 :
        #print("Removing item %d from %s"%(ind, asciiConfig.getName()))
        headerList.remove(ind)
    #else :
        #print("No item removed")
