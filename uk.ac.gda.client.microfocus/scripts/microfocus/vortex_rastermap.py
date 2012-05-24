#@PydevCodeAnalysisIgnore
from uk.ac.gda.client.microfocus.scan.datawriter import TwoWayMicroFocusWriterExtender,MicroFocusWriterExtender
from uk.ac.gda.beans import BeansFactory
from gda.factory import Finder
from gda.exafs.scan import BeanGroup
from exafsscripts.exafs.xas_scans import getDetectors
from gda.jython.commands.ScannableCommands import scan, add_default
from java.io import File
from java.lang import System
from gda.configuration.properties import LocalProperties
from jarray import array
from gda.data import PathConstructor
from gda.data.scan.datawriter import XasAsciiDataWriter
from gda.factory import Finder
from gda.scan import TrajectoryScanLine
from fast_scan import ScanPositionsTwoWay
from gda.device.scannable import ScannableUtils
from gda.data.scan.datawriter import TwoDScanRowReverser
from gda.data.scan.datawriter import XasAsciiNexusDatapointCompletingDataWriter

#from microfocus.map import redefineNexusMetadataForMaps
#import microfocus.microfocus_elements
rootnamespace = {}
def vortexRastermap (sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):
    """
    main map data collection command. 
    usage:-
    
    map sampleFileName scanFileName detectorFileName outputFileName [experiment name (_script)] [scan index (1)] [validation required (True)]
    
    """
    print "attempt to run raster map"
    #return
    print detectorFileName
    origScanPlotSettings = LocalProperties.check("gda.scan.useScanPlotSettings")
    
    if False:# Turn on debugging here
        print "Values sent to script:"
        print "sampleFileName", sampleFileName
        print "scanFileName", scanFileName
        print "detectorFileName", detectorFileName
        print "outputFileName", outputFileName
        print "folderName", folderName
        print "scanNumber", scanNumber
        print "validation", validation

    # Create the beans from the file names
    xmlFolderName = MicroFocusEnvironment().getScriptFolder() + folderName + "/"
    # Create the beans from the file names
    if(sampleFileName == None or sampleFileName == 'None'):
        sampleBean = None
    else:
        #sampleBean   = BeansFactory.getBeanObject(xmlFolderName, sampleFileName)
        sampleBean   = BeansFactory.getBeanObject(xmlFolderName, sampleFileName)
        
    scanBean     = BeansFactory.getBeanObject(xmlFolderName, scanFileName)
    detectorBean = BeansFactory.getBeanObject(xmlFolderName, detectorFileName)
    outputBean   = BeansFactory.getBeanObject(xmlFolderName, outputFileName)
     
    # give the beans to the xasdatawriter class to help define the folders/filenames 
    beanGroup = BeanGroup()
    beanGroup.setController(Finder.getInstance().find("ExafsScriptObserver"))
    beanGroup.setScriptFolder(xmlFolderName)
    beanGroup.setScannable(Finder.getInstance().find(scanBean.getXScannableName())) #TODO
    beanGroup.setExperimentFolderName(folderName)
    beanGroup.setScanNumber(scanNumber)
    if(sampleBean != None):
        beanGroup.setSample(sampleBean)
    beanGroup.setDetector(detectorBean)
    beanGroup.setOutput(outputBean)
    beanGroup.setValidate(validation)
    beanGroup.setScan(scanBean)
    XasAsciiDataWriter.setBeanGroup(beanGroup)
    
      
    # work out which detectors to use (they will need to have been configured already by the GUI)
    #sendinf None for scanbean , this does not set the dead time calculation energy for Xspress
    detectorList = getDetectors(detectorBean, outputBean, None) 
   
    # set up the sample 
    setupForVortexRaster(beanGroup)
    
    # extract any signal parameters to add to the scan command
    #signalParameters = getSignalList(outputBean)
    finder = Finder.getInstance()
    dataWriter = finder.find("DataWriterFactory")
    nx = ScannableUtils.getNumberSteps(Finder.getInstance().find(scanBean.getXScannableName()),scanBean.getXStart(), scanBean.getXEnd(),scanBean.getXStepSize()) + 1
    ny = ScannableUtils.getNumberSteps(Finder.getInstance().find(scanBean.getYScannableName()),scanBean.getYStart(), scanBean.getYEnd(),scanBean.getYStepSize()) + 1
    
  
    print "number of x points is ", str(nx)
    print "number of y points is ", str(ny)
   
    energyList = [scanBean.getEnergy()]
    zScannablePos = scanBean.getZValue()
    for energy in energyList:
        mfd = TwoWayMicroFocusWriterExtender(nx, ny, scanBean.getXStepSize(), scanBean.getYStepSize())
        globals()["microfocusScanWriter"] = mfd
        mfd.setPlotName("MapPlot")
        print " the detector is " 
        print detectorList
        if(detectorBean.getExperimentType() == "Transmission"):
            mfd.setSelectedElement("I0")
            mfd.setDetectors(array(detectorList, gda.device.Detector))
        else:   
            detectorType = detectorBean.getFluorescenceParameters().getDetectorType()
            #should get the bean file name from detector parametrs
            if(folderName != None):
                detectorBeanFileName =MicroFocusEnvironment().getScriptFolder()+File.separator +folderName +File.separator+detectorBean.getFluorescenceParameters().getConfigFileName()
            else:
                detectorBeanFileName =MicroFocusEnvironment().getScriptFolder()+detectorBean.getFluorescenceParameters().getConfigFileName()
            print detectorBeanFileName
            elements = showElementsList(detectorBeanFileName)
            ##this should be the element selected in the gui
            selectedElement = elements[0]
            mfd.setRoiNames(array(elements, java.lang.String))
           # if(detectorType == "Silicon"):
             #   detectorBeanFileName = System.getProperty("gda.config")+ "/templates/Vortex_Parameters.xml"
             #   mfd.setRoiNames(array(showElementsList(detectorBeanFileName), java.lang.String))
           #     selectedElement = "Pb"
           # else:
           #     detectorBeanFileName = System.getProperty("gda.config")+ "/templates/Xspress_Parameters.xml"
          #      mfd.setRoiNames(array(showElementsList(detectorBeanFileName), java.lang.String))
          #      selectedElement = "Pb"
            mfd.setDetectorBeanFileName(detectorBeanFileName)
            bean = BeansFactory.getBean(File(detectorBeanFileName))   
            detector = finder.find(bean.getDetectorName())   
            firstDetector = detectorList[0]
            detectorList=[]
            detectorList.append(finder.find("counterTimer01"))
            detectorList.append(detector)  
            mfd.setDetectors(array(detectorList, gda.device.Detector))     
            mfd.setSelectedElement(selectedElement)
            mfd.getWindowsfromBean()
        mfd.setEnergyValue(energy)
        mfd.setZValue(zScannablePos)
        #dataWriter.addDataWriterExtender(mfd)
        xScannable = finder.find(scanBean.getXScannableName())
        yScannable = finder.find(scanBean.getYScannableName())
        ##useFrames = LocalProperties.check("gda.microfocus.scans.useFrames")
        useFrames = False
        energyScannable = Finder.getInstance().find(scanBean.getEnergyScannableName())
        zScannable = Finder.getInstance().find(scanBean.getZScannableName())
        print "energy is ", str(energy)
        print "energy scannable is " 
        print energyScannable  
        print detectorList
        energyScannable.moveTo(energy) 
        zScannable.moveTo(zScannablePos)
        #=======================================================================
        # args=[yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),  xScannable, scanBean.getXStart(), scanBean.getXEnd(),  scanBean.getXStepSize(),energyScannable, zScannable]
        # args+= detectorList
        # if(useFrames):
        #    noOfXPoints = (scanBean.getXEnd() - scanBean.getXStart()) + 1
        #    counterTimer01.addFrameSet(noOfXPoints,0.0001,scanBean.getCollectionTime(),0,0,-1,0)
        # else:        
        #    args.append(scanBean.getCollectionTime())
        # print args
        #=======================================================================
        try:
            numberPoints = abs(scanBean.getXEnd()- scanBean.getXStart())/scanBean.getXStepSize()
            ##ContinuousScan(trajectoryX, scanBean.getXStart(), scanBean.getXEnd(), nx, scanBean.getRowTime(), [raster_xspress,]).runScan();##rowTIme in the Scan bean is in milliseconds
            if(detectorType == "Silicon"):
                print "Xmap Raster Scan"
                #tsl = TrajectoryScanLine([continuousSampleX, scanBean.getXStart(), scanBean.getXEnd(), scanBean.getXStepSize(), HTXmapMca, scanBean.getRowTime()/(1000.0 * nx)] )
                tsl = TrajectoryScanLine([continuousSampleX, ScanPositionsTwoWay(continuousSampleX,scanBean.getXStart(), scanBean.getXEnd(), scanBean.getXStepSize()),  HTXmapMca, scanBean.getRowTime()/(1000.0 * nx)] )
                tsl.setScanDataPointQueueLength(10000);tsl.setPositionCallableThreadPoolSize(1)
                #scan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),tsl,energyScannable, zScannable,realX])
                xmapRasterscan = ScannableCommands.createConcurrentScan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),tsl,energyScannable, zScannable])
                xmapRasterscan.getScanPlotSettings().setIgnore(1)
                xasWriter = XasAsciiNexusDatapointCompletingDataWriter()
                rowR = TwoDScanRowReverser()
                rowR.setNoOfColumns(nx)
                rowR.setNoOfRows(ny)
                rowR.setReverseOdd(True)
                xasWriter.setIndexer(rowR)
                xasWriter.addDataWriterExtender(mfd)
                xmapRasterscan.setDataWriter(xasWriter)
                xmapRasterscan.runScan()
            else:
                print "Xspress Raster Scan"
                xspressRasterscan = ScannableCommands.createConcurrentScan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),ContinuousScan(trajectoryX, scanBean.getXStart(), scanBean.getXEnd(), nx, scanBean.getRowTime(), [raster_counterTimer01, raster_xspress]),energyScannable, zScannable,realX])
                xspressRasterscan.getScanPlotSettings().setIgnore(1)
                xspressRasterscan.runScan()

        finally:
            scanEnd = time.asctime()
            if(origScanPlotSettings):
                LocalProperties.set("gda.scan.useScanPlotSettings", "true")
            else:
                LocalProperties.set("gda.scan.useScanPlotSettings", "false")
            #handle_messages.simpleLog("map start time " + str(scanStart))
            #handle_messages.simpleLog("map end time " + str(scanEnd))
            #dataWriter.removeDataWriterExtender(mfd)
            finish()

def finish():
    command_server = Finder.getInstance().find("command_server")
    beam = command_server.getFromJythonNamespace("beam", None)
    detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
    remove_default(beam)
    remove_default(detectorFillingMonitor)
    
def setupForVortexRaster(beanGroup):
    rasterscan = beanGroup.getScan()
    print "collection time is " , str(rasterscan.getRowTime())   
    collectionTime = rasterscan.getRowTime()/ 1000.0
    print "1setting collection time to" , str(collectionTime)  
    command_server = Finder.getInstance().find("command_server")    
    topupMonitor = command_server.getFromJythonNamespace("topupMonitor", None)    
    beam = command_server.getFromJythonNamespace("beam", None)
    detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
    trajBeamMonitor = command_server.getFromJythonNamespace("trajBeamMonitor", None)
    print "setting collection time to" , str(collectionTime)
    if(not (topupMonitor == None)):        
        topupMonitor.setPauseBeforePoint(False)
        topupMonitor.setPauseBeforeLine(True)
        topupMonitor.setCollectionTime(collectionTime)
    if(not (beam == None)):
        add_default(beam)
        beam.setPauseBeforePoint(False)
        beam.setPauseBeforeLine(True)
    if(beanGroup.getDetector().getExperimentType() == "Fluorescence" and beanGroup.getDetector().getFluorescenceParameters().getDetectorType() == "Germanium" and not (detectorFillingMonitor == None)):
        add_default(detectorFillingMonitor)
        detectorFillingMonitor.setPauseBeforePoint(False)
        detectorFillingMonitor.setPauseBeforeLine(True)
        detectorFillingMonitor.setCollectionTime(collectionTime)
    if(not (trajBeamMonitor == None)):
        trajBeamMonitor.setActive(True)
        
    ##set the file name for the output parameters
    outputBean=beanGroup.getOutput()
    sampleParameters = beanGroup.getSample()
    outputBean.setAsciiFileName(sampleParameters.getName())
    print "Setting the ascii file name as " ,sampleParameters.getName()    
    att1 = sampleParameters.getAttenuatorParameter1()
    att2 = sampleParameters.getAttenuatorParameter2()
    pos([rootnamespace['D7A'], att1.getSelectedPosition(), rootnamespace['D7B'], att2.getSelectedPosition()])
    configFluoDetector(beanGroup)
    LocalProperties.set("gda.scan.useScanPlotSettings", "true")
    redefineNexusMetadataForMaps(beanGroup)
    finder.find("RCPController").openPesrpective("uk.ac.gda.microfocus.ui.MicroFocusPerspective")
    
class MicroFocusEnvironment:
    testScriptFolder=None
    def getScriptFolder(self):
        if MicroFocusEnvironment.testScriptFolder != None:
            return MicroFocusEnvironment.testScriptFolder
        dataDirectory = PathConstructor.createFromDefaultProperty()
        return dataDirectory + "/xml/"

    testScannable=None
    def getScannable(self):
        if MicroFocusEnvironment.testScannable != None:
            return MicroFocusEnvironment.testScannable
        # The scannable name is defined in the XML when not in testing mode.
        # Therefore the scannable argument is omitted from the bean
  
        return None
