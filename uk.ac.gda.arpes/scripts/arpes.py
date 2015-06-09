from gda.commandqueue import JythonScriptProgressProvider
from gda.data.metadata import GDAMetadataProvider
import gda.factory.Finder
import gda.jython.commands.ScannableCommands
import sys
import time
import uk.ac.gda.arpes.beans.ARPESScanBean

class ARPESRun:

    def __init__(self, beanFile):
        self.bean = uk.ac.gda.arpes.beans.ARPESScanBean.createFromXML(beanFile)
        self.scienta = gda.factory.Finder.getInstance().listAllLocalObjects("uk.ac.gda.devices.vgscienta.VGScientaAnalyser")[0]
        self.progresscounter = 0
        self.totalSteps = 3
        self.lastreportedmeasurement = None

    def reportProgress(self, message):
        self.progresscounter += 1
        if self.totalSteps < self.progresscounter:
            self.totalSteps = self.progresscounter
            print "max progress steps: %d" % self.totalSteps
        JythonScriptProgressProvider.sendProgress(100.0 * self.progresscounter / self.totalSteps, "%s  (%3.1f%% done)" % (message, 100.0 * self.progresscounter / self.totalSteps))

    def checkDevice(self):
        # The idea of this method it to check that the analyser is ready to be configured or
        # acquired from.
        # Check if analyser is acquiring if so stop it.
        if(self.scienta.adBase.getDetectorState_RBV() == 1):
            print "Analyser was acquiring stop it"
            self.scienta.adBase.stopAcquiring()
            time.sleep(1.5)
        # Now check for other cases such as the error state and try to recover to idle.
        # This method is used as a workaround for the analyser getting into an error state hopefully
        # can eventually be fixed on the EPICS IOC end.
        # Check if analyser is in any state except idle and try to get into idle state
        if(self.scienta.adBase.getDetectorState_RBV() != 0):
            print "Analyser not in idle state"
            # Change to single frame mode
            self.scienta.adBase.setImageMode(0)
            # Set short acquire time
            self.scienta.adBase.setAcquireTime(0.100)
            # acquire to get one frame and recover from an error condition back to idle
            self.scienta.adBase.startAcquiring()
            time.sleep(1.5)
            # Check i recovering has worked if not tell users
            if(self.scienta.adBase.getDetectorState_RBV() != 0):
                print "Analyser is still not ready check EPICS!"
            else:
               print "Recovered to idle state. Analyser is ready"
        else:
            print "Analyser is ready"

    def setStorageTemperature(self):
        self.monitorAsynchronousMethod(self.bssc.waitTemperatureSample(self.bean.getSampleStorageTemperature()))
        # pass

    def setExposureTemperature(self, temperature):
        self.monitorAsynchronousMethod(self.bssc.waitTemperatureSEU((temperature)))
        # pass

    def setTitle(self, title):
        GDAMetadataProvider.getInstance().setMetadataValue("title", title)

    def run(self):
        self.reportProgress("Initialising")
        # Check the analyser is ready to be setup
        self.checkDevice()
        # Set the pass energy
        self.scienta.setPassEnergy(self.bean.getPassEnergy())
        # Set the lens mode
        self.scienta.setLensMode(self.bean.getLensMode())
        # Set fixed/swept
        self.scienta.setFixedMode(not self.bean.isSweptMode())
        # Set start stop and centre energy always set all even though start and stop are used in swept
        # and centre is used in fixed because the readback values are saved into the data file
        self.scienta.setStartEnergy(self.bean.getStartEnergy())
        self.scienta.setEndEnergy(self.bean.getEndEnergy())
        self.scienta.setCentreEnergy((self.bean.getEndEnergy() + self.bean.getStartEnergy()) / 2.0)
        # Always set the step even though its only used in swept mode
        self.scienta.setEnergyStep(self.bean.getStepEnergy() / 1000.0)
        # Set the exposure time and iterations
        self.scienta.setCollectionTime(self.bean.getTimePerStep())
        self.scienta.getCollectionStrategy().setMaxNumberOfFrames(self.bean.getIterations())

        # Check if its configure only
        if self.bean.isConfigureOnly():
            self.reportProgress("Setting Up Analyser")
            print "(Config only) Analyser is set up!"
            self.reportProgress("Finalising")
        else:  # not configure only run staticscan
            print "Analyser is set up!"
            self.reportProgress("Running Acquisition")
            gda.jython.commands.ScannableCommands.staticscan([self.scienta])
            self.reportProgress("Finalising")
