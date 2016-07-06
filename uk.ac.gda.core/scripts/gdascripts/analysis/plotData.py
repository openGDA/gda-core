#test of writing data from mca to the Data Vector
import math

from org.eclipse.january.dataset import DatasetFactory
from gda.analysis import ScanFileHolder
from gda.analysis.io import IFileLoader
from uk.ac.diamond.scisoft.analysis.io import DataHolder

from gda.factory import Finder
from gda.device.epicsdevice import ReturnType #@UnresolvedImport
class XYDataLoader(IFileLoader):
    def __init__(self,xName,xData,yName, yData):
        self.xName = xName
        self.xData = xData        
        self.yName = yName
        self.yData = yData
        pass
    def loadFile(self):
        dataHolder = DataHolder()
        xDataSet = DatasetFactory.zeros(1,len(self.xData),self.xData)
        xDataSet.setName(self.xName)
        dataHolder.addDataSet(xDataSet.getName(), xDataSet);
        yDataSet = DatasetFactory.zeros(1,len(self.yData),self.yData)
        yDataSet.setName(self.yName)
        dataHolder.addDataSet(yDataSet.getName(), yDataSet);
        return dataHolder

def getScanFileHolderXY(xName,xDataRaw,yName, yDataRaw):
    if len(xDataRaw) != len(yDataRaw):
        raise "len(xDataRaw) != len(yDataRaw)"
    #convert to list from array
    xData = []
    yData = []
    for i in range(len(yDataRaw)):
        xData.append(xDataRaw[i])
        yData.append(yDataRaw[i])
    scanFileHolder = ScanFileHolder()
    scanFileHolder.load(XYDataLoader(xName,xData,yName, yData))
    return scanFileHolder

def getScanFileHolderY(xName,Name,Data):
    xData = []
    for i in range(len(Data)):
        xData.append(i)    
    return getScanFileHolderXY(xName,xData,Name, Data)  
    
def plotXY(xName,xDataRaw,yName, yDataRaw):
    """Function to plot an array of data on a data vector"""
    scanFileHolder = getScanFileHolderXY(xName,xDataRaw,yName, yDataRaw)   
    scanFileHolder.plot(xName, yName)
    return scanFileHolder


def plotY(xName,Name,Data):
    """Function to plot an array of data on a data vector"""
    scanFileHolder = getScanFileHolderY(xName,Name,Data)   
    scanFileHolder.plot(xName, Name)
    return scanFileHolder

def testXY(numOfPoint=1000):
    xData = range(1,numOfPoint)
    yData = range(1,numOfPoint)
    for x in xData:
        yData[x-1] = math.cos(x % 360 * 2.0 * math.pi /360.)
    return plotXY("x",xData,"y",yData)

def testMCA():
    beamline = Finder.getInstance().find("Beamline")
    return plotY("Point","Counts",beamline.getValue(ReturnType.DBR_NATIVE,"","-EA-DET-01:MCA-01:mca3"))
