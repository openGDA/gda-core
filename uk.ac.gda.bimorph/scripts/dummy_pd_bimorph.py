#@PydevCodeAnalysisIgnore
from gda.device.scannable import PseudoDevice
from gda.factory import Finder
from time import sleep
from gda.epics import CAClient
import time

class Bimorph(PseudoDevice ):
    """
    bimorph controller for Epics bimorph controller developed in house
    To create if not already in localStation.py:
        import pd_bimorph
        bm_hfm = pd_bimorph.Bimorph_HFM()
        bm_vfm = pd_bimorph.Bimorph_VFM()
        
    to scan from current to current plus 100 in steps of 10:
        scan bm_hfm bm_hfm() bm_hfm.getPosPlusIncrement(100) bm_hfm.getListOfValues(10)
    to scan slits as well using gonx from -1. to 1 in steps of .1 and measure i_pin  
        scan bm_hfm bm_hfm() bm_hfm.getPosPlusIncrement(10) bm_hfm.getListOfValues(5)  gonx -2. -1.5 .1 i_pin

    to plot i_pin against gonx:
        LocalProperties.set("gda.scan.useScanPlotSettings","True")
    to plot i_pin and only the first channel of the mirror:
        LocalProperties.set("gda.plot.ScanPlotSettings.YFieldIndicesInvisible","0")
    """
    def __init__(self, name, startChan,numofChans):
        PseudoDevice.__init__(self)
        self.numOfChans=numofChans
        self.startChan=startChan
        self.channelIndexes = tuple(range(startChan, startChan+numofChans))
        self.setName(name)
        inputNames=[]
        extraNames=[]
        self.pos=[]
        for i in range(self.numOfChans):
            inputNames.append("C"+`i`)
            self.pos.append(0.) 
        self.setInputNames(inputNames)
        self.setExtraNames(extraNames)
        self.configure()
        self.IAmBusy=False

    def configure(self):
        self.beamline=Finder.getInstance().find("Beamline")

    def rawIsBusy(self):
        return self.IAmBusy

    def rawGetPosition(self):
        return self.pos

    def rawAsynchronousMoveTo(self,new_position):
        self.IAmBusy=True
        self.pos = new_position
        self.IAmBusy=False

    def getPosPlusIncrement(self, increment):
        pos = self.rawGetPosition()
        newPos=[]
        for i in range(self.numOfChans):
            newPos.append(pos[i]+increment)
        return newPos

    def getListOfValues(self, value):
        pos = []
        for i in range(self.numOfChans):
            pos.append(value)
        return pos

    def moveOne(self, motor, new_position):
        self.IAmBusy=True
        try:
            self.beamline.setValue("Top",self.pvPrefix+`motor`+"D",new_position)
            sleep(4)
        except:
            pass
        self.IAmBusy=False

    def moveAll(self, new_position):
        newPos=self.getListOfValues(new_position)
        self.rawAsynchronousMoveTo(newPos)

    def incrementOne(self, motor, increment):
        pos=self.beamline.getValue(None,"Top",self.pvPrefix+`motor`+"DR")
        self.moveOne(motor, pos+increment)

    def incrementAll(self, increment):
        newPos=self.getPosPlusIncrement(increment)
        self.rawAsynchronousMoveTo(newPos)