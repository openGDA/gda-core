from gda.device.detector.areadetector.v17 import ImageMode
import gda.factory.Finder
from time import sleep

class ARPESMonitor:
    ''' This class is designed to allow continuous acquisition for using a electron analyser
    alignment of the experiment
    '''

    def __init__(self):
        self.scienta = gda.factory.Finder.getInstance().listAllLocalObjects("uk.ac.gda.devices.vgscienta.VGScientaAnalyser")[0]
        self.configure()

    def configure(self):
        # Change into fixed mode for alignment
        self.scienta.setFixedMode(True)
        self.scienta.getNdProc().getPluginBase().setArrayCounter(0);
        self.scienta.getNdProc().setResetFilter(1);
        # Set exposures to 1 and change to continuous acquisition
        self.scienta.getAdBase().setNumExposures(1)
        self.scienta.getAdBase().setImageModeWait(ImageMode.CONTINUOUS)
        sleep(0.25) # Allow time for values to be set

    def start(self):
        self.configure()
        self.scienta.getAdBase().startAcquiring()

    def stop(self):
        # Zero supplies includes stopping the analyser
        self.scienta.zeroSupplies()
#am=ARPESMonitor()