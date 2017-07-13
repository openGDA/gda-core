import datetime, time, sys, os
from java.util import HashMap
from gda.data import PathConstructor
import gda.factory.Finder
from uk.ac.gda.devices.bssc.beans import BSSCSessionBean
from gda.data.metadata import GDAMetadataProvider
import gda.jython.commands.ScannableCommands
from gda.jython import InterfaceProvider
from gda.commandqueue import JythonScriptProgressProvider
from gda.jython.commands.GeneralCommands import pause
from tfgsetup import fs
from cStringIO import StringIO
from gdascripts.pd.epics_pds import DisplayEpicsPVClass, SingleEpicsPositionerClass

SAMPLE_HOLD = True

class BSSCRun:
    def __init__(self, beanFile):
        self.__version__ = '1.00'
        finder = gda.factory.Finder.getInstance()
        find = finder.find
        self.holdsample = SAMPLE_HOLD
        self.samplevolume = 20
        self.beanFile = beanFile
        self.bean = BSSCSessionBean.createFromXML(beanFile)
        self.bssc = finder.listAllLocalObjects("uk.ac.gda.devices.bssc.BioSAXSSampleChanger")[0]
        self.tfg = finder.listAllLocalObjects("gda.device.Timer")[0]
        self.detector = finder.listAllLocalObjects("uk.ac.gda.server.ncd.detectorsystem.NcdDetectorSystem")[0]
        self.shutter = find("shutter")
        self.bsscscannable = find("bsscscannable")
        self.processing = find("bssc_proc")
        #self.energy = float(find("dcm_energy").getPosition())
        self.sampleConcentration = find("sample_concentration")
        self.sampleName = find("samplename")
        self.sample_type = find('sample_type')
        self.sample_environment = find('sample_environment')

        self.meta = GDAMetadataProvider.getInstance()

        #need to remove hardcoding
        find("sample_thickness").asynchronousMoveTo(1.6)

        self.progresscounter = 0
        self.overheadsteps = 5
        self.stepspersample = 8

        self.isSimulation = hasattr(self.bssc, 'DUMMY')
        if self.isSimulation:
            self.scannables = [self.detector, self.bsscscannable]
        else:
            self.cam = gda.factory.Finder.getInstance().find("bsaxscam")
            self.scannables = [self.detector, self.bsscscannable, self.cam]

        currentVisit = GDAMetadataProvider.getInstance().getMetadataValue("visit")
        self.totalSteps = self.overheadsteps + self.bean.getMeasurements().size() * self.stepspersample

    def getFastShutter(self):
        old_stdout = sys.stdout
        sys.stdout = mystdout = StringIO()
        fs()
        sys.stdout = old_stdout
        if mystdout.getvalue() == 'fs: Open\n':
            self.reportProgress('Fast shutter is open')
            return True
        else:
            self.reportProgress('Fast shutter is closed')
            return False

    def setFastShutter(self, command='Open'):
        if command in ['Open', 'Close']:
            self.reportProgress('Setting fast shutter to '+command)
            fs(command)
            
        else:
            self.reportProgress('setFastValve function requires either Close or Open as input')

    def armFastValve(self):
        try:
            fv1.getPosition()
        except:
            fv1 = SingleEpicsPositionerClass('fv1', 'BL21B-VA-FVALV-01:CON', 'BL21B-VA-FVALV-01:STA', 'BL21B-VA-FVALV-01:STA', 'BL21B-VA-FVALV-01:CON', 'mm', '%d')
        if not fv1.getPosition() == 3.0:
            fv1(3.0)


    def setHoldSample(self, holdsample):
        if type(holdsample) == type(True):
            self.holdsample = holdsample
        else:
            self.reportProgress("ERROR: setHoldSample input should be either True or False")

    def reportProgress(self, message):
        self.progresscounter += 1
        if self.totalSteps < self.progresscounter:
            self.totalSteps = self.progresscounter
            print "max progress steps: %d" % self.totalSteps
        ourmessage = "%s  (%3.1f%% done)" % (message, 100.0 * self.progresscounter / self.totalSteps)
        print ourmessage
        JythonScriptProgressProvider.sendProgress(100.0 * self.progresscounter / self.totalSteps, ourmessage)

    def reportSampleProgress(self, measurement, message):
        self.reportProgress("%s -- %s %s" % (message, measurement.getSampleName(), measurement.getLocation()))

    def monitorAsynchronousMethod(self, taskid):
        time.sleep(0.5)
        while self.bssc.isTaskRunning(taskid):
            time.sleep(0.2)
        try:
            info = self.bssc.checkTaskResult(taskid)
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
        self.tfg.addFrameSet(frames, 50, tpf * 1000, int('00000100', 2), int('11111111', 2), 0, 0);
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
        speed = self.samplevolume / duration
        if speed >= 5 and speed <= 6000:
            # simulation reports these limits
            taskid = self.bssc.push(self.samplevolume, speed)
            filename = self.doTheScan(self.scannables)
            self.monitorAsynchronousMethod(taskid)
        else:
            print "sample speed %5.1f outside allowed range, will do static exposure" % speed
            filename = self.doTheScan(self.scannables)
        return filename

    def openShutter(self):
        self.shutter.asynchronousMoveTo("Open")

    def closeShutter(self):
        self.shutter.asynchronousMoveTo("Close")

    def measureSample(self, titration, duration):
        if titration != None:
            self.reportProgress("Cleaning before Sample")
            pause()
            self.clean()
            self.reportProgress("Sucking in Sample")
            self.loadWell(titration.getLocation())
            if self.holdsample:
                self.reportProgress("Turning on sample position feedback")
                self.bssc.setLiquidPositionFixed(self.holdsample)
            else:
                self.reportProgress("Sample position feedback is off.")
            self.reportProgress("Exposing Sample")

            self.setTitle("Sample: %s (Location %s)" % (titration.getSampleName(), titration.getLocation().toString()))

            self.sampleName.setValue(titration.getSampleName())
            self.sampleConcentration.asynchronousMoveTo(titration.getConcentration())

            filename = self.expose(duration)
            pause()

            if titration.getRecouperateLocation() is not None:
                self.reportProgress("Recouperating Sample to " + titration.getRecouperateLocation().toString())
                self.unloadIntoWell(titration.getRecouperateLocation())
            return filename

    def setUpRobotAndDetector(self, titration):
        self.reportProgress("Setting Up Robot")
        self.bssc.setViscosityLevel(titration.getViscosity())
        self.reportProgress("Checking Exposure Cell Temperature")
        self.setExposureTemperature(titration.getExposureTemperature())
        self.reportProgress("Setting Up Time Frame Generator")
        return self.setupTfg(titration.getFrames(), titration.getTimePerFrame())

    def run(self, processing=True):
        self.reportProgress('Running BSSC script version '+self.__version__)
        self.reportProgress("Initialising");
        self.checkDevice()
        if self.getFastShutter():
            self.setFastShutter('Close')
        self.bssc.setSampleType("green")
        self.reportProgress("Opening Shutter")
        self.armFastValve()
        self.openShutter()
        self.sample_environment('BSSC')
        for titration in self.bean.getMeasurements():
            self.meta.setMetadataValue('visit', titration.getVisit())
            print  "\n== Running Titration " + titration.getSampleName() + " =="
            self.sample_type('buffer' if titration.buffer else 'sample+buffer')
            duration = self.setUpRobotAndDetector(titration)
            samplefile = self.measureSample(titration, duration)
            titration.datafilename = samplefile
            # if processing:
            #     self.processing.triggerProcessing(samplefile)

        self.reportProgress("\nClosing shutter")
        self.closeShutter()
        self.reportProgress("Performing Final Cell Wash")
        self.bssc.setViscosityLevel("high")
        self.clean()
        self.exportFinalBeans()
        self.sample_environment('Manual')
        self.sample_type('sample')

    def exportFinalBeans(self):
        """The samples are updated to include the datafile paths

        export readonly version of .biosaxs file to keep record of experiment"""
        fname = self.beanFile.split(os.path.sep)[-1].rsplit('.', 1)[0]
        now = datetime.datetime.now().strftime('_%Y%m%d_%H%M%S')

        user_visit = InterfaceProvider.getBatonStateProvider().getBatonHolder().getVisitID()
        prop = PathConstructor.getDefaultPropertyName()
        path = PathConstructor.createFromProperty(prop, HashMap({'visit': user_visit}))

        full_path = os.path.join(path, fname + now + '.biosaxs')
        BSSCSessionBean.writeToXML(self.bean, full_path)
        os.chmod(full_path, 0444)

