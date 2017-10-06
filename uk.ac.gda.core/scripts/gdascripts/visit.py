from gda.data import PathConstructor
from gda.factory import Finder
from gdascripts.scannable.detector.dummy.ImageReadingDummyDetector import ImageReadingDummyDetector
import os
import gov.aps.jca.TimeoutException  # @UnresolvedImport
import java.lang.IllegalStateException

class VisitSetter():
    
    def __init__(self, detector_adapters = []):
        self.detector_adapters = list(detector_adapters)
        
        
    def datadir(self, *args):
        if len(args) > 0:
            raise ValueError("The data directory now depends on the visit. Use the 'visit' command to change this.")
        return PathConstructor.createFromDefaultProperty()

    def visit(self, new_visit = None):
        if new_visit:
            Finder.getInstance().find("GDAMetadata").setMetadataValue("visit", new_visit)
            Finder.getInstance().find("GDAMetadata").setMetadataValue("defaultVisit", new_visit)

            if not os.path.exists(self.getVisitDirectory()):
                print "!!! Warning !!!  The directory '%s' does NOT exist! (pointing to it anyway though)" %self.getVisitDirectory()
            self.setDetectorDirectories()
        return Finder.getInstance().find("GDAMetadata").getMetadataValue("visit")

    def getVisitDirectory(self):
        return PathConstructor.createFromDefaultProperty()
    
    def setDetectorDirectories(self):
        for det in self.detector_adapters:
            try:
                det.setVisitDirectory(self.getVisitDirectory())
            except gov.aps.jca.TimeoutException:
                "EPICS TimeoutException: Failed to set directory on " + det.detector.name
            
    def addDetectorAdapter(self, adapter):
        self.detector_adapters.append(adapter)
            
    def __str__(self):
        s = ""
        s+= '%12s : %s\n' % ("visit", self.visit())
        s+= '%12s : %s\n' % ("datadir", self.getVisitDirectory())
        for det in self.detector_adapters:
            if det.report_path:
                try:
                    directory = det.getDirectory()
                except gov.aps.jca.TimeoutException:
                    directory = '< EPICS IOC unavailable >'
                s+= '%12s : %s\n' % (det.detector.name, directory)
        return s



class DetectorAdapter():
    
    def __init__(self, detector, subfolder=None, create_folder=False, toreplace=None, replacement=None, report_path=True):
        self.detector = detector
        self.subfolder = subfolder
        self.create_folder = create_folder
        self.toreplace = toreplace
        self.replacement = replacement
        self.report_path = report_path
        
    def setVisitDirectory(self, path):
        fullpath = os.path.join(path, self.subfolder) if self.subfolder else os.path.join(path)
        if self.create_folder:
            if not os.path.exists(fullpath):
                try:
                    os.makedirs(fullpath)
                except:
                    print "!!! Warning !!! Could not create directory: " + fullpath
        if self.toreplace:
            fullpath = fullpath.replace(self.toreplace, self.replacement)
        self.setDirectory(fullpath)
    
    def setDirectory(self, path):
        raise Exception("Not implemented")
    
    def getDirectory(self):
        raise Exception("Not implemented")

    
class PilatusAdapter(DetectorAdapter): #pil100k
    
    def setDirectory(self, path):
        self.detector.setFilepath(path + '/') # Required by epics
        
    def getDirectory(self):
        return self.detector.getFilepath()

class FileWritingDetectorAdapter(DetectorAdapter): # ADDetector, ADPco
    
    def setDirectory(self, path):
        try:
            self.detector.setFilePath(path + '/') # Required by epics
        except java.lang.IllegalStateException:
            print "!!!!! Could not connect to (EPICS) " + self.detector.name + ": path not set to : '" + path +"'"
        
    def getDirectory(self):
        return self.detector.getFilePath()


class IPPAdapter(DetectorAdapter): #ippimages, "/dls/b16/data", "N:")+"/ippimages"
    
    def setDirectory(self, windowspath):
        self.detector.setOutputFolderRoot(windowspath)
        
    def getDirectory(self):
        return self.detector.getOutputFolderRoot()


class ProcessingDetectorWrapperAdapter(DetectorAdapter):
    """Sets the root_datadir property only (does not determine where files are written)"""
    
    def setDirectory(self, path):
        self.detector.root_datadir = path
        
    def getDirectory(self):
        return self.detector.root_datadir
