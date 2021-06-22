import time

import gda.factory.Finder
import gda.jython.commands.ScannableCommands
from org.slf4j import LoggerFactory
import uk.ac.gda.arpes.beans.ARPESScanBean
import uk.ac.diamond.daq.pes.api.AcquisitionMode

logger = LoggerFactory.getLogger(__name__ + '.py')

class ARPESRun:

    def __init__(self, beanFile):
        self.bean = uk.ac.gda.arpes.beans.ARPESScanBean.createFromXML(beanFile)
        self.analyser = gda.factory.Finder.find('analyser')
        self.progresscounter = 0
        self.totalSteps = 3
        self.lastreportedmeasurement = None
        logger.debug('Initialised ARPESRun with file: ' + beanFile + ', and analyser: ' + self.analyser.name)

    def checkDevice(self):
        # The idea of this method it to check that the analyser is ready to be configured or
        # acquired from.
        # Check if analyser is acquiring if so stop it.
        if(self.analyser.getDetectorState() == 1):
            logger.warn("Analyser was acquiring. Stopping it.")
            print "Analyser was acquiring. Stopping it."
            self.analyser.stop()
            time.sleep(1.5)
        # Now check for other cases such as the error state and try to recover to idle.
        # This method is used as a workaround for the analyser getting into an error state hopefully
        # can eventually be fixed on the EPICS IOC end.
        # Check if analyser is in any state except idle and try to get into idle state
        if(self.analyser.getDetectorState() != 0):
            logger.error("Analyser was not in idle state - Trying to recover...")
            print "Analyser was not in idle state - Trying to recover..."
            # Change to single frame mode
            self.analyser.setupAcquisitionMode(uk.ac.diamond.daq.pes.api.AcquisitionMode.FIXED)
            self.analyser.setSingleImageMode()
            # Set short acquire time
            self.analyser.setCollectionTime(0.100)
            # acquire to get one frame and recover from an error condition back to idle
            self.analyser.startAcquiring()
            time.sleep(1.5)
            # Check i recovering has worked if not tell users
            if(self.analyser.getDetectorState() != 0):
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
        # Set acquisition mode
        self.analyser.setupAcquisitionMode(self.bean.getAcquisitionMode())
        # Set the pass energy
        self.analyser.setPassEnergy(self.bean.getPassEnergy())
        # Set the lens mode
        self.analyser.setLensMode(self.bean.getLensMode())
        # Start stop and centre energy are always set  even though start and stop are used in swept
        # and centre is used in fixed because the readback values are saved into the data file
        self.analyser.setStartEnergy(self.bean.getStartEnergy())
        self.analyser.setEndEnergy(self.bean.getEndEnergy())
        self.analyser.setCentreEnergy((self.bean.getEndEnergy() + self.bean.getStartEnergy()) / 2.0)
        
        # Set energy step size if in swept mode
        if self.bean.getAcquisitionMode() == uk.ac.diamond.daq.pes.api.AcquisitionMode.SWEPT:     
            self.analyser.setEnergyStep(self.bean.getStepEnergy() / 1000.0)
        
        # Set the exposure time and iterations
        self.analyser.setCollectionTime(self.bean.getTimePerStep())
        self.analyser.setIterations(self.bean.getIterations())
                
        if self.bean.getAcquisitionMode() == uk.ac.diamond.daq.pes.api.AcquisitionMode.DITHER:     
            print "Dither mode selected"
            self.analyser.setNumberOfDitherSteps(self.bean.getDitherSteps())

        # Check if its configure only
        if self.bean.isConfigureOnly():
            logger.info("(Config only) Analyser is set up!")
            print "(Config only) Analyser is set up!"
        else:  # not configure only run staticscan
            logger.info("Analyser is set up! Running scan...")
            print "Analyser is set up! Running scan..."
            gda.jython.commands.ScannableCommands.staticscan([self.analyser])
