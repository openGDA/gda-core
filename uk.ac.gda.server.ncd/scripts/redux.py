from gda.analysis import DataSet, ScanFileHolder
from uk.ac.diamond.scisoft.analysis import SDAPlotter
from gda.analysis.io import NexusLoader
from uk.ac.diamond.scisoft.analysis.roi import SectorROI, MaskingBean
from uk.ac.diamond.scisoft.analysis.plotserver import GuiParameters
from uk.ac.gda.server.ncd.subdetector import * 
import ncdutils
import math
import os.path

# this code is dead. irakli has replaced the interesting part with java code

class NcdRedux:
    
    def __init__(self, detsystem):
        self.detsystem = detsystem
	self.settings = {}
	self.options = ["norm", "bg", "sect", "mask", "inv", "aver", "slope", "intercept", "length", "disttobeamstop", "cameralength"]
    
    def is2D(self, det):
        dim = det.getDataDimensions().tolist()
        if len(dim) == 2 and not 1 in dim:
            return True
        return False
        
    def ncdredconf(self, detector, **kwords):
        """
            if default None passed in no change is made
            default is everything off
        """
        
        if detector.lower() == "Waxs".lower():
            detector="Waxs"
        else:
            detector="Saxs"
            
        if not detector in self.settings:
            self.settings[detector] = {}
        for i in self.options:
            if not i in self.settings[detector]:
                self.settings[detector][i] = False
            if i in kwords:
                if kwords[i] == None:
                    self.settings[detector][i] = False
                else:
                    self.settings[detector][i] = kwords[i]
        
        realdet=ncdutils.getDetectorByType(self.detsystem, detector.upper())
        realdetname = realdet.getName()
        ncdutils.removeDetectorByName(self.detsystem, detector+" chain")
        
        saxsrc=ReductionChain(detector+" chain", realdetname)
        saxsrclist=saxsrc.getChain()
        
        panel=detector+" Plot"
        beanbag=SDAPlotter.getGuiBean(panel)
        if beanbag == None:
            beanbag = {}
        
        sroi = None
        if self.is2D(realdet):
            if GuiParameters.ROIDATA in beanbag:
                roi = beanbag[GuiParameters.ROIDATA]
                if isinstance(roi, SectorROI):
                    sroi = roi
                    radii = sroi.getRadii()
                    self.settings[detector]["length"]=int(math.ceil(radii[1]-radii[0]))
                    self.settings[detector]["disttobeamstop"]=radii[0]

        length =  self.settings[detector]["length"]
        slope =  self.settings[detector]["slope"]
        intercept =  self.settings[detector]["intercept"]
        if slope == 0 or slope == False:
            axisds=None
        else:
            axis=[]
            pis = realdet.getPixelSize()*1000
            d2b = self.settings[detector]["disttobeamstop"]
            for i in range(length):
                axis.append(float((i+d2b)*pis*slope+intercept))
            axisds=DataSet("qaxis", axis)
            
        mask =  self.settings[detector]["mask"]
        sect =  self.settings[detector]["sect"]
        if sroi != None:
            if sect or isinstance(axisds, DataSet):
                    start = sroi.getPoint()
                    realdet.setAttribute("beam_center_x", start[0])
                    realdet.setAttribute("beam_center_y", start[1])
            else:
                    realdet.setAttribute("beam_center_x", None)
                    realdet.setAttribute("beam_center_y", None)
                    
        cameralength = self.settings[detector]["cameralength"]
        if cameralength != False:
            realdet.setAttribute("distance", cameralength)
        else:
            realdet.setAttribute("distance", None)
            
        norm = self.settings[detector]["norm"]
        if (norm != False):
            saxsnorm=Normalisation(detector+"norm","ignored")
            saxsnorm.setCalibChannel(1)
            saxsnorm.setCalibName(ncdutils.getDetectorByType(self.detsystem, "CALIB").getName())
            saxsrclist.add(saxsnorm)
            
        bg =  self.settings[detector]["bg"]
        if bg != False: 
            if os.path.isfile(bg):
                saxsbgs=BackgroundSubtraction(detector+"bg","ignored")
                sfh=ScanFileHolder()
                if (norm>=0):
                    upstream=detector+"norm.data"
                else:
                    upstream=realdetname+".data"
                sfh.load(NexusLoader(bg,[upstream]))
                ds=sfh.getAxis(upstream)
                saxsbgs.setBackground(ds)
                saxsrclist.add(saxsbgs)
            else:
                print "background file \"%s\" does not exist." % bg
        

        if (sect):
            if sroi != None:
                    saxssect=SectorIntegration(detector+"sect","ignored")
                    saxssect.setROI(sroi)
                    if isinstance(axisds, DataSet):
                        saxssect.setqAxis(axisds)
                    if (mask):
                        if not GuiParameters.MASKING in beanbag:
                            print "no mask defined for %s" % panel
                        else:
                            mb=beanbag[GuiParameters.MASKING]
                            if not isinstance(mb, MaskingBean):
                                print "no mask defined for %s" % panel
                            else:
                                saxssect.setMask(mb.getMask())
                    saxsrclist.add(saxssect)
            else:
                print "sector integration requested but no ROI found"
                
        inv =  self.settings[detector]["inv"]
        if (inv):
            saxsinvariant=Invariant(detector+"invariant", "ignored")
            saxsrclist.add(saxsinvariant)
            
        aver =  self.settings[detector]["aver"]
        if (aver):
            saxsaver=Average(detector+"average", "ignored")
            saxsrclist.add(saxsaver)
        self.detsystem.addDetector(saxsrc)
