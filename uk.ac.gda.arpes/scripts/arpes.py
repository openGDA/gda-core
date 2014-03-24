import time, sys
import gda.factory.Finder
import uk.ac.gda.arpes.beans.ARPESScanBean
from gda.data.metadata import GDAMetadataProvider
import gda.jython.commands.ScannableCommands
from gda.commandqueue import JythonScriptProgressProvider

class APRESRun:
    
    def __init__(self, beanFile):
        self.bean = uk.ac.gda.arpes.beans.ARPESScanBean.createFromXML(beanFile)
        self.scienta = gda.factory.Finder.getInstance().listAllLocalObjects("uk.ac.gda.devices.vgscienta.VGScientaAnalyser")[0]
        self.progresscounter = 0
        self.totalSteps = 5
        self.lastreportedmeasurement = None
        
    def reportProgress(self, message):
        self.progresscounter += 1
        if self.totalSteps < self.progresscounter:
            self.totalSteps = self.progresscounter
            print "max progress steps: %d" % self.totalSteps
        JythonScriptProgressProvider.sendProgress(100.0*self.progresscounter/self.totalSteps, "%s  (%3.1f%% done)" % (message, 100.0*self.progresscounter/self.totalSteps))
        
    def checkDevice(self):
        pass
    
    def setStorageTemperature(self):
        self.monitorAsynchronousMethod(self.bssc.waitTemperatureSample(self.bean.getSampleStorageTemperature()))
        #pass
    
    def setExposureTemperature(self, temperature):
        self.monitorAsynchronousMethod(self.bssc.waitTemperatureSEU((temperature)))
        #pass
    
    def setTitle(self, title):
        GDAMetadataProvider.getInstance().setMetadataValue("title", title)
    
    def run(self):
        self.reportProgress("Initialising")
        self.checkDevice()
        self.scienta.setPassEnergy(self.bean.getPassEnergy())
        self.scienta.setLensMode(self.bean.getLensMode())
        if self.bean.isSweptMode():
            self.scienta.setFixedMode(False)
            self.scienta.setStartEnergy(self.bean.getStartEnergy())
            self.scienta.setEndEnergy(self.bean.getEndEnergy())
            self.scienta.setEnergyStep(self.bean.getStepEnergy()/1000.0)
        else:
            self.scienta.setFixedMode(True)
            self.scienta.setCentreEnergy((self.bean.getEndEnergy()+self.bean.getStartEnergy())/2.0)
        self.scienta.setCollectionTime(self.bean.getTimePerStep())
        self.scienta.getCollectionStrategy().setMaxNumberOfFrames(self.bean.getIterations())
        #set temperature
        #set photonenergy
        if self.bean.isConfigureOnly():
            self.reportProgress("Setting Up Analyser")
            self.reportProgress("Setting Up Analyser")
            print "analyser set up"
        else:
            self.reportProgress("Running Acquisition")
            gda.jython.commands.ScannableCommands.staticscan([self.scienta])
            self.reportProgress("Finalising")
        time.sleep(2)
