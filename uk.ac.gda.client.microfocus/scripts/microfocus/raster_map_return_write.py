from jarray import array
from map import Map

from gda.data.scan.datawriter import TwoDScanRowReverser
from gda.data.scan.datawriter import XasAsciiNexusDatapointCompletingDataWriter
from gda.factory import Finder
from gda.jython.commands import ScannableCommands
from gda.scan import TrajectoryScanLine
from uk.ac.gda.client.microfocus.util import ScanPositionsTwoWay


#
# Faster raster
#
class RasterMapReturnWrite(Map):
    
    def __init__(self, xspressConfig, vortexConfig, d7a, d7b, counterTimer01, rcpController, ExafsScriptObserver,outputPreparer, detectorPreparer, raster_xmap, traj1PositionReader, traj3PositionReader, traj1tfg, traj1xmap,traj3tfg, traj3xmap, traj1SampleX, traj3SampleX, raster_xspress):
        self.xspressConfig = xspressConfig
        self.vortexConfig = vortexConfig
        self.d7a=d7a
        self.d7b=d7b
        self.counterTimer01=counterTimer01
        self.rcpController = rcpController
        self.ExafsScriptObserver = ExafsScriptObserver
        self.outputPreparer = outputPreparer
        self.detectorPreparer = detectorPreparer
        self.raster_xmap=raster_xmap

        self.traj1PositionReader=traj1PositionReader
        self.traj3PositionReader=traj3PositionReader
        self.trajPositionReader=traj1PositionReader

        self.traj1SampleX=traj1SampleX
        self.traj3SampleX=traj3SampleX
        self.trajSampleX=traj1SampleX
        self.traj1tfg=traj1tfg
        self.traj3tfg=traj3tfg
        self.trajtfg=traj1tfg
        self.traj1xmap=traj1xmap
        self.traj3xmap=traj3xmap
        self.trajxmap=traj1xmap
        
        self.raster_xspress = raster_xspress
        
        self.beamEnabled = True
        self.finder = Finder.getInstance()
        self.mfd = None
        self.detectorBeanFileName=""
        
    def setStage(self, stage):
        if stage==1:
            self.trajSampleX = self.traj1SampleX
            self.trajPositionReader = self.traj1PositionReader
            self.trajtfg=self.traj1tfg
            self.trajtfg.setTtlSocket(1)
            self.trajxmap=self.traj1xmap
        elif stage==3:
            self.trajSampleX = self.traj3SampleX
            self.trajPositionReader = self.traj3PositionReader
            self.trajtfg=self.traj3tfg
            self.trajtfg.setTtlSocket(2)
            self.trajxmap=self.traj3xmap
        else:
            print "please enter 1 or 3 as a parameter where 1 is the small stage and 3 is the large stage"

    def _runMap(self,beanGroup, xScannable, yScannable, zScannable, detectorList,scanNumber,experimentFolderName,experimentFullPath,nx,ny):
        scanBean = beanGroup.getScan()
        detectorBean = beanGroup.getDetector()
        detectorType = detectorBean.getFluorescenceParameters().getDetectorType()
        
        if detectorBean.getExperimentType() != "Fluorescence" or detectorType != "Silicon":
            print "*** Faster maps may only be performed using the Xmap Vortex detector! ***"
            print "*** Change detector type in XML or mapping mode by typing map.disableFasterRaster()"
            return
        
        point_collection_time = scanBean.getRowTime() / nx
        self.trajtfg.setIntegrateBetweenPoints(True)
        self.trajxmap.setIntegrateBetweenPoints(True)
        self.trajtfg.setCollectionTime(point_collection_time)
        self.trajxmap.setCollectionTime(point_collection_time)
        self.trajxmap.setScanNumberOfPoints(nx)
        sptw= ScanPositionsTwoWay(self.trajSampleX,scanBean.getXStart(), scanBean.getXEnd(), scanBean.getXStepSize())
        tsl = TrajectoryScanLine([self.trajSampleX, sptw,  self.trajtfg, self.trajxmap, scanBean.getRowTime()/(nx)] )
        tsl.setScanDataPointQueueLength(10000)
        tsl.setPositionCallableThreadPoolSize(10)
        xmapRasterscan = ScannableCommands.createConcurrentScan([yScannable, scanBean.getYStart(), scanBean.getYEnd(),  scanBean.getYStepSize(),tsl, self.trajPositionReader])
        xmapRasterscan.getScanPlotSettings().setIgnore(1)
        xasWriter = XasAsciiNexusDatapointCompletingDataWriter()
        rowR = TwoDScanRowReverser()
        rowR.setNoOfColumns(nx)
        rowR.setNoOfRows(ny)
        rowR.setReverseOdd(True)
        xasWriter.setIndexer(rowR)
        xasWriter.addDataWriterExtender(self.mfd)
        xmapRasterscan.setDataWriter(xasWriter)
        self.finder.find("elementListScriptController").update(None, self.detectorBeanFileName);
        self.log("Starting two-directional raster map...")
        xmapRasterscan.runScan()
