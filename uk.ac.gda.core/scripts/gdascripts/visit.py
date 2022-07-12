from gda.jython import InterfaceProvider, JythonServerFacade
from gda.factory import Finder
from gda.observable import IObserver
from gda.data.metadata import GDAMetadataProvider
from gda.jython.batoncontrol import BatonChanged
import os
import gov.aps.jca.TimeoutException  # @UnresolvedImport
import java.lang.IllegalStateException # @UnresolvedImport
from org.slf4j import LoggerFactory
from __builtin__ import isinstance

class VisitSetter(IObserver):
    
    def __init__(self, detector_adapters = []):
        self.detector_adapters = list(detector_adapters)
        JythonServerFacade.getInstance().addBatonChangedObserver(self)
        
    def datadir(self, *args):
        if len(args) > 0:
            raise ValueError("The data directory now depends on the visit. Use the 'visit' command to change this.")
        return InterfaceProvider.getPathConstructor().createFromDefaultProperty()

    def visit(self, new_visit = None):
        if new_visit:
            Finder.find("GDAMetadata").setMetadataValue("visit", new_visit)
            Finder.find("GDAMetadata").setMetadataValue("defaultVisit", new_visit)

            if not os.path.exists(self.getVisitDirectory()):
                print "!!! Warning !!!  The directory '%s' does NOT exist! (pointing to it anyway though)" %self.getVisitDirectory()
            self.setDetectorDirectories()
        return Finder.find("GDAMetadata").getMetadataValue("visit")

    def getVisitDirectory(self):
        return InterfaceProvider.getPathConstructor().createFromDefaultProperty()
    
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

    def update(self, source, data):
        """The visit can be changed when a client connects and acquires the baton
        the detector directories should be updated when this happens"""
        if isinstance(data, BatonChanged):
            current_visit = GDAMetadataProvider.getInstance().getMetadataEntry("visit")
            if current_visit is not None:
                print("Setting det dirs after baton change event")
                self.setDetectorDirectories()


class DetectorAdapter():
    
    def __init__(self, detector, subfolder=None, create_folder=False, toreplace=None, replacement=None, report_path=True):
        self.detector = detector
        self.subfolder = subfolder
        self.create_folder = create_folder
        self.toreplace = toreplace
        self.replacement = replacement
        self.report_path = report_path
        self.logger = LoggerFactory.getLogger("%s:%s" % (self.__class__.__name__, detector.getName()))

    def setVisitDirectory(self, path):
        fullpath = os.path.join(path, self.subfolder) if self.subfolder else os.path.join(path)
        self.logger.debug("setVisitDirectory({}) with subfolder={}, create_folder={}, toreplace={} & replacement={} so raw fullpath={}",
             path, self.subfolder, self.create_folder, self.toreplace, self.replacement, fullpath)

        if self.create_folder:
            if not os.path.exists(fullpath):
                try:
                    os.makedirs(fullpath)
                except:
                    self.logger.warn("Could not create folder {}", fullpath)
                    print "!!! Warning !!! Could not create directory: " + fullpath

        if self.toreplace:
            oldpath=fullpath
            fullpath=fullpath.replace(self.toreplace, self.replacement)
            if fullpath == oldpath:
                self.logger.warn("No substitution made on {} with toreplace={} & replacement={}",
                    fullpath, self.toreplace, self.replacement)

        self.logger.debug("setVisitDirectory({}) -> {}", path, fullpath)
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
