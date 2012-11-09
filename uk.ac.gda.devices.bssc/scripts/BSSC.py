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
        self.progresscounter = 0
        self.sampleprogresscounter = 0
        self.overheadsteps = 4
        self.stepspersample = 9 
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
    
    def loadSample(self, location):
        self.monitorAsynchronousMethod(self.bssc.fill(location.getPlate(), location.getRowAsInt(), location.getColumn(), 10.0))
    
    def loadBuffer(self, location):
        self.monitorAsynchronousMethod(self.bssc.fill(location.getPlate(), location.getRowAsInt(), location.getColumn(), 10.0))
    
    def expose(self, duration):
        taskid = self.bssc.push(10.0, 5.0/duration)
        gda.jython.commands.ScannableCommands.staticscan([self.detector])
        #self.monitorAsynchronousMethod(taskid)
    
    def run(self):
        self.reportProgress("Initialising");
        self.checkDevice()
        self.reportProgress("Setting Storage Temperature")
        self.setStorageTemperature()
        self.reportProgress("Performing Courtesy Cell Wash")
        self.bssc.setViscosityLevel("high")
        self.bssc.setSampleType("yellow")
        self.clean()
        for titration in self.bean.getMeasurements():
            self.reportSampleProgress(titration, "Setting Up Robot")
            self.bssc.setViscosityLevel(titration.getViscosity())
            if titration.isYellowSample():
                self.bssc.setSampleType("yellow")
            else:
                self.bssc.setSampleType("green")
            self.reportSampleProgress(titration, "Checking Exposure Cell Temperature")
            self.setExposureTemperature(titration.getExposureTemperature())
            self.reportSampleProgress(titration, "Setting Up Time Frame Generator")
            duration = self.setupTfg(titration.getFrames(), titration.getTimePerFrame())
            self.reportSampleProgress(titration, "Sucking in Sample")
            self.loadSample(titration.getLocation())
            self.reportSampleProgress(titration, "Exposing Sample")
            self.setTitle("Sample: %s (Location %s)" % (titration.getSampleName(), titration.getLocation().toString()))
            self.expose(duration)
            self.reportSampleProgress(titration, "Cleaning after Sample")
            self.clean()
            #self.reportSampleProgress(titration, "Sucking in Buffer")
            #self.loadBuffer(titration.getLocation())
            #self.reportSampleProgress(titration, "Exposing Buffer")
            #self.setTitle("Buffer: %s (for Sample %s at Location %s)" % (titration.getBufferName(), titration.getSampleName(), titration.getLocation().toString()))
            #self.expose(duration)
            #self.reportSampleProgress(titration, "Cleaning after Buffer")
            #self.clean()
        self.reportProgress("Finalising")
        time.sleep(2)
