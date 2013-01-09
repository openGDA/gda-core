import time, sys
import gda.factory.Finder
import uk.ac.gda.devices.bssc.beans.BSSCSessionBean
from gda.data.metadata import GDAMetadataProvider
import gda.jython.commands.ScannableCommands
from gda.commandqueue import JythonScriptProgressProvider

class BSSCRun:
    
    def __init__(self, beanFile):
        self.bean = uk.ac.gda.devices.bssc.beans.BSSCSessionBean.createFromXML(beanFile)
        self.bssc = gda.factory.Finder.getInstance().listAllLocalObjects("uk.ac.gda.devices.bssc.BioSAXSSampleChanger")[0]
        self.tfg = gda.factory.Finder.getInstance().listAllLocalObjects("gda.device.Timer")[0]
        self.detector = gda.factory.Finder.getInstance().listAllLocalObjects("uk.ac.gda.server.ncd.detectorsystem.NcdDetectorSystem")[0]
        self.cam = gda.factory.Finder.getInstance().find("bsaxscam")
        self.shutter = gda.factory.Finder.getInstance().find("shutter")
        self.progresscounter = 0
        self.sampleprogresscounter = 0
        self.overheadsteps = 5
        self.stepspersample = 9 
        self.samplevolume = 10
        self.totalSteps = self.overheadsteps + self.bean.getMeasurements().size() * self.stepspersample
        self.lastreportedmeasurement = None
        
    def reportProgress(self, message):
        self.progresscounter += 1
        if self.totalSteps < self.progresscounter:
            self.totalSteps = self.progresscounter
            print "max progress steps: %d" % self.totalSteps
        JythonScriptProgressProvider.sendProgress(100.0*self.progresscounter/self.totalSteps, "%s  (%3.1f%% done)" % (message, 100.0*self.progresscounter/self.totalSteps))
    
    def reportSampleProgress(self, measurement, message):
        if self.lastreportedmeasurement == measurement:
            self.sampleprogresscounter += 1
            if self.sampleprogresscounter > self.stepspersample:
                print "max steps per sample: %d" % self.sampleprogresscounter
        else:
            self.lastreportedmeasurement = measurement
            self.sampleprogresscounter = 1
        self.reportProgress("Sample: %s -- %s" % (measurement.getSampleName(), message))
    
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
    
    def setStorageTemperature(self):
        self.monitorAsynchronousMethod(self.bssc.waitTemperatureSample(self.bean.getSampleStorageTemperature()))
        #pass
    
    def setExposureTemperature(self, temperature):
        self.monitorAsynchronousMethod(self.bssc.waitTemperatureSEU((temperature)))
        #pass
    
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
        self.monitorAsynchronousMethod(self.bssc.recouperate(location.getPlate(), location.getRowAsInt(), location.getColumn()))
    
    def expose(self, duration):
        taskid = self.bssc.push(10.0, 5.0/duration)
        gda.jython.commands.ScannableCommands.staticscan([self.detector])
        #self.monitorAsynchronousMethod(taskid)
        
    def openShutter(self):
        self.shutter.asynchronousMoveTo("Open")
    
    def closeShutter(self):
        self.shutter.asynchronousMoveTo("Closed")
        
    def measureBuffer(self, titration, duration):
        if titration != None:
            self.reportSampleProgress(titration, "Sucking in Buffer")
            self.loadWell(titration.getBufferLocation())
            self.reportSampleProgress(titration, "Exposing Buffer")
            self.setTitle("Buffer for next and preceding sample measurement")
            self.expose(duration)
            self.reportSampleProgress(titration, "Cleaning after Buffer")
            self.clean()
   
    def measureSample(self, titration, duration):
        if titration != None:     
            self.reportSampleProgress(titration, "Sucking in Sample")
            self.loadWell(titration.getLocation())
            self.reportSampleProgress(titration, "Exposing Sample")
            self.setTitle("Sample: %s (Location %s)" % (titration.getSampleName(), titration.getLocation().toString()))
            self.expose(duration)
            if titration.isRecouperate():
                self.reportSampleProgress(titration, "Recouperating Sample and Cleaning")
                self.unloadIntoWell(titration.getLocation)
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
            return false
        if abs(t1.getExposureTemperature() - t2.getExposureTemperature()) > 0.1:
            return false
        if t1.getFrames() != t2.getFrames():
            return false
        if abs(t1.getTimePerFrame() - t2.getTimePerFrame()) > 0.001:
            return false
        return true
        
    def run(self):
        self.reportProgress("Initialising");
        self.checkDevice()
        self.reportProgress("Setting Storage Temperature")
        self.setStorageTemperature()
        self.reportProgress("Performing Courtesy Cell Wash")
        self.bssc.setViscosityLevel("high")
        self.bssc.setSampleType("yellow")
        self.clean()
        self.reportProgress("Opening Shutter")
        self.openShutter()
        lastTitration=None
        duration=None
        for titration in self.bean.getMeasurements():
            if lastTitration != None and not self.tritrationsCanUseSameBufferMeasurement(lastTitration, titration):
                # everything would be set up from previous sample then
                self.measureBuffer(lastTitration, duration)
            duration = self.setUpRobotAndDetector(titration)
            self.measureSample(titration, duration)
            lastTitration = titration
        self.measureBuffer(lastTitration, duration)
        self.reportProgress("Closing shutter")
        self.closeShutter()
        time.sleep(2)
        
