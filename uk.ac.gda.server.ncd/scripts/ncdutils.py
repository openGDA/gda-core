from uk.ac.gda.server.ncd.subdetector import * 
        
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