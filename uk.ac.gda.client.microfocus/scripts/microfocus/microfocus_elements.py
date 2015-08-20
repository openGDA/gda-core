import sys

from uk.ac.gda.util.beans.xml import XMLHelpers
from java.io import File
from java.lang import System

# def getNumberOfDetectors(detectorFileName): 
#     detectorBean=0
#     try:
#         print detectorFileName
#         detectorBean = BeansFactory.getBean(File(detectorFileName));
#         print detectorBean
#         numberOfDetectorElements = detectorBean.getDetectorList().size()
#         return numberOfDetectorElements
#     except:
#         #must be Detector Parameters file
#         print sys.exc_info()[0]
#     return 0


def showElementsList(detectorFileName): 
    regionsList=[]
    detectorBean=0
    try:
        detectorBean = XMLHelpers.getBean(File(detectorFileName));
        numberOfDetectorElements = detectorBean.getDetectorList().size()
        print "Number of detector elements:" , numberOfDetectorElements        
        for i in range(numberOfDetectorElements):
            regionsList.append(detectorBean.getDetector(i).getRegionList())
    except:
        #must be Detector Parameters file
        print sys.exc_info()[0]
        return getElementNamesfromIonChamber(detectorBean)
    referenceList=[]
    elementsList=[]
    for roi in regionsList[0]:
        referenceList.append(roi.getRoiName())
    elementsList = referenceList
    for i in range(1, numberOfDetectorElements):
        regions = regionsList[i]
        roiList =[]
        for roi in regions:
            roiList.append(roi.getRoiName().encode())
        for reference in referenceList:
            try:
                roiList.index(reference)
            except:
                elementsList.remove(reference)
        referenceList = elementsList
    print "Selected elements:"
    for each in elementsList:
        print "\t",each
    return elementsList
       
def getElementNamesfromIonChamber(detectorBean):
    ionChambersList = detectorBean.getTransmissionParameters().getIonChamberParameters()
    elementsStr = []
    print str(len(ionChambersList))
    for ion in ionChambersList:
        print "ion name is" + ion.getName()
        elementsStr.append(ion.getName())
        print elementsStr
    return elementsStr
# def showElementsListString(detectorFileName):   
#     eleStr = showElementsList(detectorFileName)
#     retStr=""
#     for s in eleStr:
#         retStr  = retStr +  s + ","
#     return retStr
def plotSpectrum(detNo, x,y):
    try:
        writerObj = globals()["microfocusScanWriter"]
        writerObj.plotSpectrum(detNo,y,x)
    except Exception, e:
        print "exception treating : "+e.__str__()
        print "unable to plot spectrum"
def getXY(x,y):
    try:
        writerObj = globals()["microfocusScanWriter"]
        return writerObj.getXY(x,y)
    except:
        ##TODO use logger
        print "unable to get XY"
        
def displayMap(selectedElement):
    try:
        writerObj = globals()["microfocusScanWriter"]
        writerObj.displayPlot(selectedElement)
        return True
    except:
        ##TODO use logger
        print "unable to displayMap"
        return False
    
# def getSampleXYZPositions():
#     xpos = sc_MicroFocusSampleX.getPosition()
#     ypos = sc_MicroFocusSampleY.getPosition()
#     zpos = sc_sample_z.getPosition()
#     return str(xpos) + "," + str(ypos)+ ","+str(zpos)

# def getAttenuatorPositions(at1Name, at2Name):
#     at1 = finder.find(at1Name)
#     at2 = finder.find(at2Name)
#     at1Pos = at1.getPosition()
#     at2Pos = at2.getPosition()
#     return str(at1Pos) +  "," + str(at2Pos)