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
        self.scienta.getAdBase().setImageMode(2)
        # Pause to allow values to be set. Would be better to use setImageModeWait(ImageMode.CONTINUOUS)
        # however the enum constant isn't available in jython see GDA-6152
        sleep(0.25)

    def start(self):
        self.configure()
        self.scienta.getAdBase().startAcquiring()

    def stop(self):
        self.scienta.getAdBase().stopAcquiring()
#am=ARPESMonitor()