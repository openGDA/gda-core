import time, sys
import gda.factory.Finder
import uk.ac.gda.devices.bssc.beans.BSSCSessionBean
from gda.data.metadata import GDAMetadataProvider
import gda.jython.commands.ScannableCommands
from gda.commandqueue import JythonScriptProgressProvider
from uk.ac.gda.devices.bssc.ispyb import BioSAXSDBFactory, BioSAXSISPyBUtils

class BSSCRun:
    
    def __init__(self, beanFile):
        self.samplevolume = 10
        self.bean = uk.ac.gda.devices.bssc.beans.BSSCSessionBean.createFromXML(beanFile)
        self.bssc = gda.factory.Finder.getInstance().listAllLocalObjects("uk.ac.gda.devices.bssc.BioSAXSSampleChanger")[0]
        self.tfg = gda.factory.Finder.getInstance().listAllLocalObjects("gda.device.Timer")[0]
        self.detector = gda.factory.Finder.getInstance().listAllLocalObjects("uk.ac.gda.server.ncd.detectorsystem.NcdDetectorSystem")[0]
        self.cam = gda.factory.Finder.getInstance().find("bsaxscam")
        self.shutter = gda.factory.Finder.getInstance().find("shutter")
        self.progresscounter = 0
        self.overheadsteps = 5
        self.stepspersample = 8
        self.stepsperbuffer = 3
        self.energy = float(GDAMetadataProvider.getInstance().getMetadataValue("instrument.monochromator.energy"))
        self.totalSteps = self.overheadsteps + self.bean.getMeasurements().size() * self.stepspersample + (self.bean.getMeasurements().size() + 1) * self.stepsperbuffer
        lastTitration=None
        self.ispyb = BioSAXSDBFactory.makeAPI()
        self.visit = self.ispyb.getSessionForVisit(GDAMetadataProvider.getInstance().getMetadataValue("visit"))
        for titration in self.bean.getMeasurements():
            if lastTitration != None and not self.tritrationsCanUseSameBufferMeasurement(lastTitration, titration):
                self.totalSteps += self.stepsperbuffer
        self.lastreportedmeasurement = None
        try: 
            self.isSimulation = True
            # in simulation temperature control does not work
            self.bssc.getTemperatureSampleStorage()
            self.isSimulation = False
        except: 
            print "Temperature control is not working, will run at ambient conditions."
        if self.isSimulation:
            self.scannables = [self.detector]
        else:
            self.scannable = [self.detector, self.cam]
        
    def reportProgress(self, message):
        self.progresscounter += 1
        if self.totalSteps < self.progresscounter:
            self.totalSteps = self.progresscounter
            print "max progress steps: %d" % self.totalSteps
        ourmessage = "%s  (%3.1f%% done)" % (message, 100.0*self.progresscounter/self.totalSteps)
        print ourmessage
        JythonScriptProgressProvider.sendProgress(100.0*self.progresscounter/self.totalSteps, ourmessage)
        
    def reportSampleProgress(self, measurement, message):
        self.reportProgress("%s -- %s %s" % (message, measurement.getSampleName(), measurement.getLocation()))
    
    def monitorAsynchronousMethod(self, taskid):
        time.sleep(0.5)
        while self.bssc.isTaskRunning(taskid):
            time.sleep(0.2)
        try:
            info = self.bssc.checkTaskResult(taskid)
            print info
        except:
            self.reportProgress("ABORT -- robot operation failed : %s " % sys.exc_info()[0])
            time.sleep(2)
            raise
        time.sleep(0.1)
        
    def checkDevice(self):
        self.bssc.setEnableVolumeDetectionInWell(True)
        print "Plates in Sample Changer: ", self.bssc.getPlatesIDs()
    
    def getStorageTemperature(self):
        if not self.isSimulation:
            return self.bssc.getTemperatureSampleStorage()
        else:
            return -300.0
            
    def setStorageTemperature(self):
        if not self.isSimulation:
            self.monitorAsynchronousMethod(self.bssc.waitTemperatureSample(self.bean.getSampleStorageTemperature()))
    
    def getExposureTemperature(self):
        if not self.isSimulation:
            return self.bssc.getTemperatureSEU()
        else:
            return -300

    def setExposureTemperature(self, temperature):
        if not self.isSimulation:
            self.monitorAsynchronousMethod(self.bssc.waitTemperatureSEU((temperature)))
    
    def clean(self):
        self.monitorAsynchronousMethod(self.bssc.clean())
    
    def setupTfg(self, frames, tpf):
        self.tfg.clearFrameSets()
        self.tfg.addFrameSet(frames, 50, tpf * 1000, 0, 251, 0, 0);
        self.tfg.loadFrameSets()
        return frames * (tpf + 0.05)
    
    def setTitle(self, title):
        GDAMetadataProvider.getInstance().setMetadataValue("title", title)
    
    def loadWell(self, location):
        self.monitorAsynchronousMethod(self.bssc.fill(location.getPlate(), location.getRowAsInt(), location.getColumn(), self.samplevolume))

    def unloadIntoWell(self, location):
        self.monitorAsynchronousMethod(self.bssc.recuperate(location.getPlate(), location.getRowAsInt(), location.getColumn()))
    
    def doTheScan(self, scannables):
        scan = gda.scan.StaticScan(scannables)
        scan.runScan()
        return scan.getDataWriter().getCurrentFileName() 
    
    def expose(self, duration):
        speed = self.samplevolume/duration
        if speed >= 5 and speed <= 6000: 
            # simulation reports these limits
            taskid = self.bssc.push(self.samplevolume, speed)
            filename = self.doTheScan(self.scannables)
            self.monitorAsynchronousMethod(taskid)
        else:
            print "sample speed %5.1f outside allowed range, will do static exposure" % speed
            filename = self.doTheScan([self.detector])
        return filename
            
    def openShutter(self):
        self.shutter.asynchronousMoveTo("Open")
    
    def closeShutter(self):
        self.shutter.asynchronousMoveTo("Closed")
        
    def measureBuffer(self, titration, duration):
        if titration != None:
            self.reportSampleProgress(titration, "Sucking in Buffer from %s" % titration.getBufferLocation())
            self.loadWell(titration.getBufferLocation())
            self.reportSampleProgress(titration, "Exposing Buffer")
            self.setTitle("Buffer for next and preceding sample measurement")
            filename = self.expose(duration)
            self.reportSampleProgress(titration, "Cleaning after Buffer")
            id = self.ispyb.createBufferMeasurement(self.visit, titration.getBufferLocation().getPlate(), titration.getBufferLocation().getRowAsInt(), titration.getBufferLocation().getColumn(), self.getStorageTemperature(), self.getExposureTemperature(), titration.getFrames(), titration.getTimePerFrame(), 0.0, self.samplevolume, self.energy, titration.getViscosity(), filename, "/entry1/Pilatus2M/data")
            self.ispyb.createMeasurementToDataCollection(self.datacollection, id)
            self.clean()
   
    def measureSample(self, titration, duration):
        if titration != None:     
            self.reportSampleProgress(titration, "Sucking in Sample")
            self.loadWell(titration.getLocation())
            self.reportSampleProgress(titration, "Exposing Sample")
            self.setTitle("Sample: %s (Location %s)" % (titration.getSampleName(), titration.getLocation().toString()))
            filename = self.expose(duration)
            id = self.ispyb.createSampleMeasurement(self.visit, titration.getLocation().getPlate(), titration.getLocation().getRowAsInt(), titration.getLocation().getColumn(), titration.getSampleName(), titration.getConcentration(), self.getStorageTemperature(), self.getExposureTemperature(), titration.getFrames(), titration.getTimePerFrame(), 0.0, self.samplevolume, self.energy, titration.getViscosity(), filename, "/entry1/Pilatus2M/data")
            self.ispyb.createMeasurementToDataCollection(self.datacollection, id)
            if titration.isRecouperate() and not titration.isYellowSample():
                self.reportSampleProgress(titration, "Recuperating Sample and Cleaning")
                self.unloadIntoWell(titration.getLocation())
            else:
                self.reportSampleProgress(titration, "Cleaning after Sample")
            self.clean()

    def setUpRobotAndDetector(self, titration):
            self.reportSampleProgress(titration, "Setting Up Robot")
            self.bssc.setViscosityLevel(titration.getViscosity())
            if titration.isYellowSample():
                self.bssc.setSampleType("yellow")
            else:
                self.bssc.setSampleType("green")
            self.reportSampleProgress(titration, "Checking Exposure Cell Temperature")
            self.setExposureTemperature(titration.getExposureTemperature())
            self.reportSampleProgress(titration, "Setting Up Time Frame Generator")
            return self.setupTfg(titration.getFrames(), titration.getTimePerFrame())
        
    def tritrationsCanUseSameBufferMeasurement(self, t1, t2):
        if not t1.getBufferLocation().equals(t2.getBufferLocation()):
            return False
        if abs(t1.getExposureTemperature() - t2.getExposureTemperature()) > 0.1:
            return False
        if t1.getFrames() != t2.getFrames():
            return False
        if abs(t1.getTimePerFrame() - t2.getTimePerFrame()) > 0.001:
            return False
        return True
        
    def run(self):
        self.reportProgress("Initialising");
        self.checkDevice()
        self.reportProgress("Setting Storage Temperature")
        self.setStorageTemperature()
        self.reportProgress("Performing Courtesy Cell Wash")
        self.bssc.setViscosityLevel("high")
        self.bssc.setSampleType("yellow")
        self.clean()
        
        self.datacollection = self.ispyb.createSaxsDataCollection(self.visit)
        
        self.reportProgress("Opening Shutter")
        self.openShutter()
        lastTitration=None
        duration=None
        for titration in self.bean.getMeasurements():
            if lastTitration != None and not self.tritrationsCanUseSameBufferMeasurement(lastTitration, titration):
                # everything would be set up from previous sample then
                self.measureBuffer(lastTitration, duration)
            duration = self.setUpRobotAndDetector(titration)
            self.measureBuffer(titration, duration)
            self.measureSample(titration, duration)
            lastTitration = titration
        self.measureBuffer(lastTitration, duration)
        self.reportProgress("Closing shutter")
        self.closeShutter()
        time.sleep(2)
        BioSAXSISPyBUtils.dumpCollectionReport(self.datacollection)
        self.ispyb.disconnect()
        
