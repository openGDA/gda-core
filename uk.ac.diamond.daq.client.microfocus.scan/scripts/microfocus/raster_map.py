from map import Map

from gda.configuration.properties import LocalProperties
from gda.factory import Finder
from gda.device.detector.xspress import XspressDetector, ResGrades
from gda.jython.commands import ScannableCommands
from gda.scan import ContinuousScan
from uk.ac.gda.beans import BeansFactory

from java.io import File

class RasterMap(Map):
    def __init__(self, xspressConfig, vortexConfig, d7a, d7b, kb_vfm_x, counterTimer01, rcpController, ExafsScriptObserver,outputPreparer,detectorPreparer, traj1ContiniousX, traj3ContiniousX, raster_counterTimer01, raster_xmap, traj1PositionReader, traj3PositionReader, raster_xspress, raster_xspress3 , cid, trajBeamMonitor):
        self.xspressConfig = xspressConfig
        self.vortexConfig = vortexConfig
        self.d7a=d7a
        self.d7b=d7b
        self.kb_vfm_x = kb_vfm_x
        self.counterTimer01=counterTimer01
        self.rcpController = rcpController
        self.ExafsScriptObserver=ExafsScriptObserver
        self.outputPreparer = outputPreparer
        self.detectorPreparer = detectorPreparer

        self.traj1ContiniousX=traj1ContiniousX
        self.traj3ContiniousX=traj3ContiniousX
        self.trajContiniousX=traj1ContiniousX
        self.raster_counterTimer01=raster_counterTimer01
        self.raster_xmap=raster_xmap
        self.traj1PositionReader = traj1PositionReader
        self.traj3PositionReader = traj3PositionReader
        self.trajPositionReader = traj1PositionReader
        self.raster_xspress=raster_xspress
        self.raster_xspress3=raster_xspress3

        self.mfd = None
        self.finder = Finder.getInstance()
        
        self.cid = cid
        self.trajBeamMonitor = trajBeamMonitor
        
        self.includeRealPositionReader = True
        
    def setIncludeRealPositionReader(self, include=True):
        self.includeRealPositionReader = include

    def setStage(self, stage):
        Map.setStage(self,stage)
        if stage==1:
            self.trajContiniousX = self.traj1ContiniousX
            self.trajPositionReader = self.traj1PositionReader
            self.raster_counterTimer01.setTtlSocket(1)
        elif stage==3:
            self.trajContiniousX = self.traj3ContiniousX
            self.trajPositionReader = self.traj3PositionReader
            # RJW 11/2/14 could have a different TTL socket and then do not have to switch the cables...
            # it was like this but everyone forgot and so kept swapping the cable anyway... 
            self.raster_counterTimer01.setTtlSocket(1)

        else:
            print "please enter 1 or 3 as a parameter where 1 is the small stage and 3 is the large stage"
            
            
#     def getDetectors(self, detectorBean, scanBean):
#         detectorType = detectorBean.getFluorescenceParameters().getDetectorType()
#         dets = [self.raster_counterTimer01, self.cid]
#         if(detectorType == "Silicon"):
#             dets += [self.raster_xmap]
#         elif(detectorType == "Xspress3"):
#             dets += [self.raster_xspress3]
#         else:
#             dets += [self.raster_xspress]
#             
#         return dets

#         expt_type = detectorBean.getExperimentType()
#         if expt_type == "Transmission":
#             for group in detectorBean.getDetectorGroups():
#                 if group.getName() == detectorBean.getTransmissionParameters().getDetectorType():
#                     return self._createDetArray(group.getDetector(), scanBean)
#         else:
#             for group in detectorBean.getDetectorGroups():
#                 if group.getName() == detectorBean.getFluorescenceParameters().getDetectorType():
#                     detArray = self._createDetArray(group.getDetector(), scanBean)
#  
#                     if detectorBean.getFluorescenceParameters().isCollectDiffractionImages():
#                         if self.cmos != None:
#                             print "Using cmos"
#                             detArray += [self.cmos]
#                     
#                     return detArray
#         raise Exception("Detector list not found!")
    
    
    def _runMap(self,beanGroup, xScannable, yScannable, zScannable, detectorList,scanNumber,experimentFolderName,experimentFullPath,nx,ny):
        detectorBean = beanGroup.getDetector()
        scanBean = beanGroup.getScan()
        sampleBean = beanGroup.getSample()
        outputBean=beanGroup.getOutput()
        
        numberPoints = abs(scanBean.getXEnd()- scanBean.getXStart())/scanBean.getXStepSize() + 1.0

        detectorType = detectorBean.getFluorescenceParameters().getDetectorType()
        
        collectionTime = float(scanBean.getRowTime())/float(numberPoints)
        
        self.counterTimer01.setCollectionTime(collectionTime)
        
        self.log("Row time: " + str(float(scanBean.getRowTime())))
        self.log("Number points: "+ str(float(numberPoints)))
        
        
        # For the very few times a year that the cid is used,
        # then uncomment this line (and comment out the line below)
        # to add the cid detector to the raster maps
#         dets = [self.raster_counterTimer01, self.cid]
        dets = [self.raster_counterTimer01]
        
        if(detectorType == "Silicon"):
            dets += [self.raster_xmap]
        elif(detectorType == "Xspress3"):
            dets += [self.raster_xspress3]
        else:
            dets += [self.raster_xspress]
             
        self.log("Detectors used:"+str(dets))
#         dets = self._getDetectors(detectorBean, scanBean)
#         self.log("Detectors used:"+str(dets))
        
        cs = ContinuousScan(self.trajContiniousX, scanBean.getXStart(), scanBean.getXEnd(), nx, scanBean.getRowTime(), dets) 
        
        outerScanArgs = [yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(), self.trajBeamMonitor, cs]
        if self.includeRealPositionReader :
            print "including real positions"
            outerScanArgs = outerScanArgs + [self.trajPositionReader]
        else:
            print "Not including real positions"

        if detectorBean.getFluorescenceParameters().isCollectDiffractionImages():
            if self.cmos != None:
                print "Using cmos"
                outerScanArgs += [self.cmos]
        
        theScan = ScannableCommands.createConcurrentScan(outerScanArgs)
        theScan.getScanPlotSettings().setIgnore(1)
    
        sampleName = sampleBean.getName()
        descriptions = sampleBean.getDescriptions()
        theScan = self._setUpDataWriter(theScan,scanBean,detectorBean,sampleBean,outputBean,sampleName,descriptions,scanNumber,experimentFolderName,experimentFullPath)
    
        self.finder.find("elementListScriptController").update(None, self.detectorBeanFileName);
        self.log("Starting " + detectorType + " raster map...")
        theScan.runScan()

    def _setupForMap(self, beanGroup):
        rasterscan = beanGroup.getScan()
        collectionTime = rasterscan.getRowTime()
        command_server = self.finder.find("command_server")
        
        self.log("Starting raster scan...")
        
        if (LocalProperties.get("gda.mode") == 'live'):
            topupMonitor = command_server.getFromJythonNamespace("topupMonitor", None)    
            beamMonitor = command_server.getFromJythonNamespace("beamMonitor", None)
            detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None)
            if topupMonitor != None:
                topupMonitor.setPauseBeforePoint(False)
                topupMonitor.setPauseBeforeLine(True)
                topupMonitor.setCollectionTime(collectionTime)
            if beamMonitor != None:
                beamMonitor.setPauseBeforePoint(False)
                beamMonitor.setPauseBeforeLine(False)
            
            # test and repeat before lines
            self.trajBeamMonitor.setPauseBeforeLine(True)
            self.trajBeamMonitor.setPauseBeforePoint(False)
            self.trajBeamMonitor.setPauseBeforeScan(False)
            
            if(beanGroup.getDetector().getExperimentType() == "Fluorescence" and beanGroup.getDetector().getFluorescenceParameters().getDetectorType() == "Germanium"):
                self.finder.find("command_server").addDefault(detectorFillingMonitor);
                detectorFillingMonitor.setPauseBeforePoint(False)
                detectorFillingMonitor.setPauseBeforeLine(True)
           
                fullFileName = beanGroup.getXmlFolder() + beanGroup.getDetector().getFluorescenceParameters().getConfigFileName()
                bean = BeansFactory.getBean(File(fullFileName));
                bean.setReadoutMode(XspressDetector.READOUT_MCA);
                bean.setResGrade(ResGrades.NONE);
                elements = bean.getDetectorList();
                for element in elements: 
                    rois = element.getRegionList();
                    element.setWindow(rois.get(0).getRoiStart(), rois.get(0).getRoiEnd())
                BeansFactory.saveBean(File(fullFileName), bean)
                
        self._setupFromSampleParameters(beanGroup)
       
        self.rcpController.openPerspective("uk.ac.gda.microfocus.ui.MicroFocusPerspective")
