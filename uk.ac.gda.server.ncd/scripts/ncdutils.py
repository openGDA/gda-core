from uk.ac.gda.server.ncd.subdetector import * 
from gda.device.scannable import ScannableMotionUnitsBase
from gdascripts.scannable.epics.PvManager import PvManager

def getDetectorByType(detsystem, dettype):
        for det in detsystem.getDetectors():
            if det.getDetectorType() == dettype:
                return det
        return None
    
def removeDetectorByName(detsystem, detname):
        deldet = None
        for det in detsystem.getDetectors():
            if det.getName() == detname:
                deldet = det
        if not deldet == None:
            detsystem.removeDetector(deldet) 

class DetectorMeta(ScannableMotionUnitsBase):
    def __init__(self, name, detsystem, type, meta, unit=None):
        self.setName(name)
        self.detsystem = detsystem
        self.type = type
        self.setInputNames([name])
        self.setExtraNames([])
        self.meta = meta
        if unit != None:
            self.setHardwareUnitString(unit);

    def rawGetPosition(self):
        try:
            dis = getDetectorByType(self.detsystem, self.type).getAttribute(self.meta)
        except:
            return [0.0]
        if dis <= 0:
            return [0.0]
        return [dis]
        
    def rawAsynchronousMoveTo(self,p):
        if p != None:
            p = float(p)
            if p <= 0.0:
                p = None
        getDetectorByType(self.detsystem, self.type).setAttribute(self.meta, p)

    def isBusy(self):
        return 0

class DetectorMetaWithPv(DetectorMeta, object):
    def __init__(self, name, detsystem, type, meta, unit=None, pv=None):
        super(DetectorMetaWithPv, self).__init__(name, detsystem, type, meta, unit)
        self.pv = None
        if pv is not None:
            self.pv = PvManager(pv[1:], pv[0])
            self.pv.configure()
            self.pv_conversion = 1

    def rawAsynchronousMoveTo(self, p):
        if p is not None:
            p = float(p)
            if p <= 0:
                p = None
        if self.pv:
            self.pv.caput(self.pv_conversion * p)
        super(DetectorMetaWithPv, self).rawAsynchronousMoveTo(p)
