# Copy of branches/8.14/configurations/diamond/i19/scripts/EpicsGigECamera.py
# from r39512
from gdascripts.scannable.epics.PvManager import PvManager
from time import sleep
import time
from gda.device.detector import PseudoDetector
from gda.device.Detector import BUSY, IDLE
from gda.analysis import ScanFileHolder
from org.eclipse.january.dataset import DatasetFactory
from gda.analysis.io import PNGSaver

# areaDetector settings required for this to work:
#     "CAM" tab
#         "Image" section
#             Colour Mode: Mono
#         "Acquisition" section
#             # Images: 1
#             Image Mode: Single
#             Trigger Mode: Freerun (NOT Software!)
#     "ARR" tab
#         "Callback" section
#             Enable: Enable

def unsign2(x):
    x = float(x)
    if x<0:
        return x+256.
    else:
        return x

class EpicsGigECamera(PseudoDetector):

    #TODO: Known bug: only works when epics zoom turned off (read the zoom value)
    def __init__(self, name, pvroot, filepath=None,
                 stdNotArr=True, reconnect=True, verbose=False,
                 acquireToCagetDelay_s = 0.1):
        # BL07I-DI-PHDGN-06:CAM:DATA
        self.name = name
        self.extraNames = []
        self.outputFormat = []
        self.level = 9
        self.verbose = verbose
        self.acquireToCagetDelay_s = acquireToCagetDelay_s
        stdArr = "STD" if stdNotArr else "ARR"
        pvs = { "DATA":stdArr+":ArrayData", "START":"CAM:Acquire",
                "WIDTH":"CAM:ArraySizeX_RBV", "HEIGHT":"CAM:ArraySizeY_RBV",
                "COLLECTTIME":"CAM:AcquireTime" }
        if reconnect:
            pvs["RECONNECT"]="CAM:RESET.PROC"
            
        self.pvs = PvManager(pvs, pvroot)
        self.pvs.configure()
        
        self.filepath = filepath
        self.ds = None
        
        self.disableStop = False
        
    def createsOwnFiles(self):
        return False

    def setCollectionTime(self, t):
        #self.pvs["COLLECTTIME"].caput(int(t))
        self.pvs["COLLECTTIME"].caput(float(t))

    def getCollectionTime(self):
        return float(self.pvs['COLLECTTIME'].caget())
    
    def setDisableStop(self, disable):
        self.disableStop = disable
    
    def stop(self):
        if self.verbose:
            print "%s.stop()" % self.name
        
        if not self.disableStop:
            self.pvs["START"].caput(0)
    
    def collectData(self):
        """
>>>pos cam1 1.5
cam1det.collectData() acquiring...
cam1det.collectData() (1.604000s) ended
cam1det.readout() getting...
cam1det.readout() (0.340000s) sign correction...
cam1det.readout() (6.910000s) creating DataSet...
cam1det.readout() (0.352000s) saving...
cam1det.readout() (0.581000s) ended
Move completed: cam1 : t: 1.500000 path: 122201.0
"""
        if self.verbose:
            print "%s.collectData() acquiring..." % self.name
            t = time.time()
        
        self.ds = None
        
        self.pvs["START"].caput(1)
        sleep(self.getCollectionTime()+self.acquireToCagetDelay_s)
        
        if self.verbose:
            dt, t = time.time()-t, time.time()
            print "%s.collectData() (%fs) ended" % (self.name, dt)
    
    def getStatus(self):
        return IDLE
    
    def readout(self):
        if self.ds is None:
            if self.verbose:
                print "%s.readout() getting..." % self.name
                t = time.time()
            
            rawdata = self.pvs['DATA'].cagetArray()
            
            if self.verbose:
                dt, t = time.time()-t, time.time()
                print "%s.readout() (%fs) sign correction..." % (self.name, dt)
            
            data = map(unsign2, rawdata )
            
            if self.verbose:
                dt, t = time.time()-t, time.time()
                print "%s.readout() (%fs) creating DataSet..." % (self.name, dt)
            
            self.ds = DatasetFactory.createFromObject(data, [int(float(self.pvs['HEIGHT'].caget())), int(float(self.pvs['WIDTH'].caget()))])
            
            if self.verbose:
                dt, t = time.time()-t, time.time()
                print "%s.readout() (%fs) saving..." % (self.name, dt)
            
            if self.filepath is not None:
                self.saveImage(time.strftime("%Y%m%d%H%M%S.png", time.localtime()))
            
            if self.verbose:
                dt, t = time.time()-t, time.time()
                print "%s.readout() (%fs) ended" % (self.name, dt)
        
        return self.ds
        
    def setFilepath(self, filepath):
        self.filepath=filepath
        
    def getFilepath(self):
        return self.filepath
    
    def saveImage(self, filename):
        path = self.filepath + filename
        if self.ds is None:
            raise Exception("Epics GigE Camera %s has not acquired an image" % self.name)
        else:
            sfh = ScanFileHolder()
            sfh.addDataSet("Image",self.ds)
            sfh.save(PNGSaver(path))
