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
import gdaserver
import uuid
import json

SAMPLE_HOLD = True

class BSSCRun:
    def __init__(self, beanFile):
        self.__version__ = '1.01'
        finder = gda.factory.Finder.getInstance()
        find = finder.find
        self.simulate = False
        self.holdsample = SAMPLE_HOLD
        self.samplevolume = 35
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
        self.experiment_definition = gdaserver.experiment_definition
        self.metashop = gdaserver.metashop
        self.meta = GDAMetadataProvider.getInstance()

        #need to remove hardcoding
        find("sample_thickness").asynchronousMoveTo(1.6)

        self.progresscounter = 0
        self.overheadsteps = 5
        self.stepspersample = 8

        if self.simulate:
            print "running in simulation mode"
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
        if not self.simulate:
            return self.bssc.getTemperatureSampleStorage()
        else:
            return -300.0

    def getExposureTemperature(self):
        if not self.simulate:
            return self.bssc.getTemperatureSEU()
        else:
            return -300

    def setExposureTemperature(self, temperature):
        if not self.simulate:
            self.monitorAsynchronousMethod(self.bssc.waitTemperatureSEU((temperature)))

    def clean(self):
        if not self.simulate:
            self.monitorAsynchronousMethod(self.bssc.clean())

    def setupTfg(self, frames, tpf):
        self.tfg.clearFrameSets()
        # Dead time needs to be >=100 to allow for the 'fast' shutter to open fully before exposure
        self.tfg.addFrameSet(frames, 100, tpf * 1000, int('00000100', 2), int('11111111', 2), 0, 0);
        self.tfg.loadFrameSets()
        return frames * (tpf + 0.1)

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

    def expose(self, duration, move=False):
        if move:
            print "Moving the sample during collection"
            speed = 1
            push_volume = self.samplevolume-10
            taskid = self.bssc.push(self.samplevolume-10, 1)
            filename = self.doTheScan(self.scannables)
            self.monitorAsynchronousMethod(taskid)
        else:
            print "Sample static during collection"
            filename = self.doTheScan(self.scannables)
        return filename

    def openShutter(self):
        self.shutter.asynchronousMoveTo("Open")

    def closeShutter(self):
        self.shutter.asynchronousMoveTo("Close")

    def measureSample(self, titration, duration):
        if not self.simulate:
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

                #self.setTitle("Sample: %s (Location %s)" % (titration.getSampleName(), titration.getLocation().toString()))
                self.setTitle(titration.getSampleName())

                self.sampleName.setValue(titration.getSampleName())
                self.sampleConcentration.asynchronousMoveTo(titration.getConcentration())
                if titration.getKey() == "move":
                    move = True
                else:
                    move = False
                filename = self.expose(duration, move)
                pause()

                if titration.getRecouperateLocation() is not None:
                    self.reportProgress("Recouperating Sample to " + titration.getRecouperateLocation().toString())
                    self.unloadIntoWell(titration.getRecouperateLocation())
                return filename
        else:
            print 'ran the measureSample function'
            return 'dummy_file_name.nxs'

    def setUpRobotAndDetector(self, titration):
        if not self.simulate:
            self.reportProgress("Setting Up Robot")
            self.bssc.setViscosityLevel(titration.getViscosity())
            self.reportProgress("Checking Exposure Cell Temperature")
            self.setExposureTemperature(titration.getExposureTemperature())
            self.reportProgress("Setting Up Time Frame Generator")
            return self.setupTfg(titration.getFrames(), titration.getTimePerFrame())
        else:
            print 'Ran the setupRobotAndDetector function'
            return titration.getFrames() * ( titration.getTimePerFrame() + 0.1)

    def addExperimentDefinitionToNxs(self, add=True):
        if add:
            self.metashop.setMetaScannables([self.experiment_definition])
            print 'added experiment definition field to nxs files'
        else:
            self.experiment_definition( ['n/a', 'n/a', 'n/a'] )
            self.metashop.remove(self.experiment_definition)
            print 'removed experiment definition field from nxs files'

    def dictFromTitration(self, titration):
        output_dict = {}
        output_dict['plate'] = titration.getLocation().getPlate()
        output_dict['row'] = titration.getLocation().getRow()
        output_dict['column'] = titration.getLocation().getColumn()
        output_dict['sampleName'] = titration.getSampleName()
        output_dict['concentration'] = titration.getConcentration()
        output_dict['viscosity'] = titration.getViscosity()
        output_dict['molecularWeight'] = titration.getMolecularWeight()
        if titration.isBuffer():
            output_dict['isBuffer'] = True
            output_dict['buffers'] = None
        else:
            output_dict['isBuffer'] = False
            output_dict['buffers'] = titration.getBuffers()
        try:
            output_dict['recoup'] = True
            output_dict['recoupPlate'] = titration.getRecouperateLocation().getPlate()
            output_dict['recoupRow'] = titration.getRecouperateLocation().getRow()
            output_dict['recoupColumn'] = titration.getRecouperateLocation().getColumn()
        except:
            output_dict['recoup'] = False
            output_dict['recoupPlate'] = None
            output_dict['recoupRow'] = None
            output_dict['recoupColumn'] = None
        output_dict['timePerFrame'] = titration.getTimePerFrame()
        output_dict['frames'] = titration.getFrames()
        output_dict['exposureTemperature'] = titration.getExposureTemperature()
        output_dict['mode'] = titration.getMode()
        output_dict['key'] = titration.getKey()
        output_dict['visit'] = titration.getVisit()
        output_dict['username'] = titration.getUsername()
        output_dict['datafilename'] = titration.getDatafilename()
        return output_dict

    def jsonStringFromMeasurements(self, measurements):
        output_array = []
        for titration in measurements:
            output_array.append(self.dictFromTitration(titration))
        return json.dumps(output_array)

    def run(self, processing=True):
        try:
            self.reportProgress('Running BSSC script version '+self.__version__)
            self.reportProgress("Initialising");
            if not self.simulate:
                self.checkDevice()
                if self.getFastShutter():
                    self.setFastShutter('Close')
                self.armFastValve()
                self.openShutter()
                self.bssc.setSampleType("green")
            self.reportProgress("Opening Shutter")
            self.sample_environment('BSSC')
            self.addExperimentDefinitionToNxs()
            
            experiment_id = str(uuid.uuid4())
            for index, titration in enumerate(self.bean.getMeasurements()):
                self.experiment_definition( [ self.jsonStringFromMeasurements(self.bean.getMeasurements()), str(index), experiment_id ] )
                self.meta.setMetadataValue('visit', titration.getVisit())
                print  "\n== Running Titration " + titration.getSampleName() + " =="
                self.sample_type('buffer' if titration.buffer else 'sample+buffer')
                duration = self.setUpRobotAndDetector(titration)
                samplefile = self.measureSample(titration, duration)
                titration.datafilename = samplefile

                # if processing:
                #     self.processing.triggerProcessing(samplefile)
    
            self.reportProgress("\nClosing shutter")
            if not self.simulate:
                self.closeShutter()
            self.reportProgress("Performing Final Cell Wash")
            if not self.simulate:
                self.bssc.setViscosityLevel("high")
                self.clean()
            self.exportFinalBeans()
            self.sample_environment('Manual')
            self.sample_type('sample')
        finally:
            self.addExperimentDefinitionToNxs(False)
            print 'BSSC SCRIPT FINISHED NORMALLY'

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

