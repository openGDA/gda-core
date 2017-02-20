import time

import gda.factory.Finder
import gda.jython.commands.ScannableCommands
from org.slf4j import LoggerFactory
import uk.ac.gda.arpes.beans.ARPESScanBean

logger = LoggerFactory.getLogger(__name__ + '.py')

class ARPESRun:

    def __init__(self, beanFile):
        self.bean = uk.ac.gda.arpes.beans.ARPESScanBean.createFromXML(beanFile)
        self.scienta = gda.factory.Finder.getInstance().find('analyser')
        self.progresscounter = 0
        self.totalSteps = 3
        self.lastreportedmeasurement = None
        logger.debug('Initialised ARPESRun with file: ' + beanFile + ', and analyser: ' + self.scienta.name)

    def checkDevice(self):
        # The idea of this method it to check that the analyser is ready to be configured or
        # acquired from.
        # Check if analyser is acquiring if so stop it.
        if(self.scienta.adBase.getDetectorState_RBV() == 1):
            logger.warn("Analyser was acquiring. Stopping it.")
            print "Analyser was acquiring. Stopping it."
            self.scienta.adBase.stopAcquiring()
            time.sleep(1.5)
        # Now check for other cases such as the error state and try to recover to idle.
        # This method is used as a workaround for the analyser getting into an error state hopefully
        # can eventually be fixed on the EPICS IOC end.
        # Check if analyser is in any state except idle and try to get into idle state
        if(self.scienta.adBase.getDetectorState_RBV() != 0):
            logger.error("Analyser was not in idle state - Trying to recover...")
            print "Analyser was not in idle state - Trying to recover..."
            # Change to single frame mode
            self.scienta.adBase.setImageMode(0)
            # Set short acquire time
            self.scienta.adBase.setAcquireTime(0.100)
            # acquire to get one frame and recover from an error condition back to idle
            self.scienta.adBase.startAcquiring()
            time.sleep(1.5)
            # Check i recovering has worked if not tell users
            if(self.scienta.adBase.getDetectorState_RBV() != 0):
                msg = "Analyser recovery failed, check EPICS!"
                logger.error(msg)
                print msg
                raise Exception(msg)
            else:
                logger.info("Analyser was recovered to idle state.")
                print "Analyser was recovered to idle state."
        else:
            logger.debug("Analyser is ready")
            print "Analyser is ready"


    def run(self):
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
        
        # Temporary remove iterations as VGScientaAnalyserCamOnly only doesn't support it yet
        # This won't work properly on i05 HR as it is this needs resolving!
        #self.scienta.getCollectionStrategy().setMaxNumberOfFrames(self.bean.getIterations())

        # Check if its configure only
        if self.bean.isConfigureOnly():
            logger.info("(Config only) Analyser is set up!")
            print "(Config only) Analyser is set up!"
        else:  # not configure only run staticscan
            logger.info("Analyser is set up! Running scan...")
            print "Analyser is set up! Running scan..."
            gda.jython.commands.ScannableCommands.staticscan([self.scienta])
